package com.ddd.mall.infrastructure.persistence.query;

import com.ddd.mall.domain.product.Product;
import com.ddd.mall.domain.product.query.ProductPageResult;
import com.ddd.mall.domain.product.query.ProductQueryPort;
import com.ddd.mall.infrastructure.persistence.ProductJpaRepository;
import com.ddd.mall.infrastructure.persistence.converter.ProductConverter;
import com.ddd.mall.infrastructure.persistence.dataobject.ProductDO;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ProductQueryPortImpl implements ProductQueryPort {

    private final ProductJpaRepository jpaRepository;

    @Override
    public ProductPageResult findPage(int page, int size, String status, String category, String keyword) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);

        String domainStatus = toDomainStatus(status);
        String kw = keyword == null ? null : keyword.trim();

        PageRequest pageable = PageRequest.of(safePage - 1, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<ProductDO> spec = (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            if (domainStatus != null && !domainStatus.isBlank()) {
                preds.add(cb.equal(root.get("status"), domainStatus));
            }
            if (category != null && !category.isBlank()) {
                preds.add(cb.equal(root.get("category"), category));
            }
            if (kw != null && !kw.isBlank()) {
                Predicate nameLike = cb.like(cb.lower(root.get("name")), "%" + kw.toLowerCase() + "%");
                Predicate descLike = cb.like(cb.lower(root.get("description")), "%" + kw.toLowerCase() + "%");
                preds.add(cb.or(nameLike, descLike));
            }
            return preds.isEmpty() ? cb.conjunction() : cb.and(preds.toArray(Predicate[]::new));
        };
        Page<ProductDO> result = jpaRepository.findAll(spec, pageable);
        List<Product> content = result.getContent().stream().map(ProductConverter::toDomain).collect(Collectors.toList());
        return new ProductPageResult(content, result.getTotalElements());
    }

    private static String toDomainStatus(String statusApi) {
        if (statusApi == null || statusApi.isBlank()) {
            return null;
        }
        return switch (statusApi) {
            case "PUBLISHED" -> "ON_SALE";
            case "UNPUBLISHED" -> "OFF_SALE";
            default -> statusApi;
        };
    }

    @Override
    public List<Product> findOnSaleProductsOrdered() {
        return jpaRepository.findByStatus("ON_SALE").stream()
                .map(ProductConverter::toDomain)
                .sorted(Comparator.comparing(Product::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> findAllCategories() {
        return jpaRepository.findAll().stream()
                .map(ProductDO::getCategory)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public long countTotal() {
        return jpaRepository.count();
    }
}