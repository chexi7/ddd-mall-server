package com.ddd.mall.infrastructure.persistence.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 领域对象反射重建工具（仅供基础设施层 Converter 使用）
 * <p>
 * DDD 要求聚合根/实体不暴露 public setter，所有状态变更必须通过行为方法驱动。
 * 但仓储从数据库加载聚合根时（重建），需要把持久化的数据"设回去"——这属于技术实现细节，
 * 不应污染领域模型的封装性。
 * <p>
 * 本工具类封装反射逻辑，让 Converter 能重建领域对象而无需领域类暴露任何 setter。
 * 这与 JPA EntityManager.merge()、MyBatis-Plus 反射设值的原理完全一致——框架
 * 本身就是通过反射来填充领域对象字段，而不是要求领域对象提供 public setter。
 * <p>
 * 参考：
 * - 码如云 DDD 文章："禁止 @Setter 和 @Data，聚合根使用 @Getter + @NoArgsConstructor(access = PRIVATE)"
 * - cap4j 框架：代码生成插件强制删除 @Setter/@Data，实体只能通过行为方法变更状态
 * - cap4j 的 Aggregate.Default._wrap() 机制：框架通过 _wrap() 将 JPA 实体设入聚合根，
 *   业务代码不直接操作内部状态
 */
public final class DomainObjectReconstructor {

    private DomainObjectReconstructor() {
    }

    /**
     * 通过反射重建领域对象
     * <p>
     * 1. 反射调用 protected/private 无参构造函数创建实例
     * 2. 遍历 fieldValues，反射设置字段值（包括父类字段如 Entity.id、AggregateRoot.version）
     * 3. 对 static/final 字段自动跳过（如 List<OrderItem> items 保留默认初始化）
     * 4. 对 null 值自动跳过（数据库中为 NULL 的字段保持默认值）
     *
     * @param clazz       领域对象类型
     * @param fieldValues 字段名 → 字段值的映射（字段名必须与类中声明的字段名一致）
     * @return 重建后的领域对象实例
     */
    public static <T> T reconstruct(Class<T> clazz, Map<String, Object> fieldValues) {
        try {
            // 1. 反射调用无参构造函数创建实例
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            T instance = constructor.newInstance();

            // 2. 反射设置字段值
            for (Map.Entry<String, Object> entry : fieldValues.entrySet()) {
                String fieldName = entry.getKey();
                Object value = entry.getValue();
                if (value != null) {
                    setField(instance, fieldName, value);
                }
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("反射重建领域对象失败: " + clazz.getSimpleName() + ", 原因: " + e.getMessage(), e);
        }
    }

    /**
     * 设置 id 和 version（仅供 RepositoryImpl.save() 使用）
     * <p>
     * JPA 保存后返回的 DO 对象包含数据库生成的 id 和乐观锁 version，
     * 需要同步回领域对象。以前通过 setId()/setVersion()（public setter）完成，
     * 现在改为反射设值。
     *
     * @param domainObject 领域对象实例
     * @param id           数据库生成的 ID
     * @param version      乐观锁版本号
     */
    public static void setIdAndVersion(Object domainObject, Long id, Long version) {
        if (id != null) {
            setField(domainObject, "id", id);
        }
        if (version != null) {
            setField(domainObject, "version", version);
        }
    }

    /**
     * 反射设置字段值（向上遍历类层次查找字段，包括父类）
     * <p>
     * 聚合根的 id 字段声明在 Entity 基类，version 字段声明在 AggregateRoot 基类，
     * 必须向上遍历才能找到。
     */
    private static void setField(Object instance, String fieldName, Object value) {
        try {
            Field field = findField(instance.getClass(), fieldName);
            if (field == null) {
                throw new NoSuchFieldException("字段 " + fieldName + " 在 " + instance.getClass().getSimpleName() + " 及其父类中未找到");
            }

            // 跳过 static 和 final 字段
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) {
                return;
            }

            field.setAccessible(true);
            field.set(instance, value);
        } catch (Exception e) {
            throw new RuntimeException("反射设置字段失败: " + instance.getClass().getSimpleName() + "." + fieldName + ", 原因: " + e.getMessage(), e);
        }
    }

    /**
     * 向上遍历类层次查找字段
     */
    private static Field findField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
}