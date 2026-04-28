package com.ddd.mall.infrastructure.persistence.converter;

import com.ddd.mall.domain.member.Address;
import com.ddd.mall.domain.member.Member;
import com.ddd.mall.infrastructure.persistence.dataobject.MemberDO;
import com.ddd.mall.infrastructure.persistence.reflect.DomainObjectReconstructor;

import java.util.LinkedHashMap;
import java.util.Map;

public class MemberConverter {

    public static Member toDomain(MemberDO d) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", d.getId());
        fields.put("version", d.getVersion());
        fields.put("username", d.getUsername());
        fields.put("password", d.getPassword());
        fields.put("nickname", d.getNickname());
        fields.put("phone", d.getPhone());
        fields.put("createdAt", d.getCreatedAt());
        if (d.getProvince() != null) {
            fields.put("address", new Address(d.getProvince(), d.getCity(), d.getDistrict(), d.getAddressDetail(), d.getZipCode()));
        }

        Member m = DomainObjectReconstructor.reconstruct(Member.class, fields);
        m.clearDomainEvents();
        return m;
    }

    public static MemberDO toDO(Member m) {
        MemberDO d = new MemberDO();
        d.setId(m.getId());
        d.setVersion(m.getVersion());
        d.setUsername(m.getUsername());
        d.setPassword(m.getPassword());
        d.setNickname(m.getNickname());
        d.setPhone(m.getPhone());
        d.setCreatedAt(m.getCreatedAt());

        Address addr = m.getAddress();
        if (addr != null) {
            d.setProvince(addr.getProvince());
            d.setCity(addr.getCity());
            d.setDistrict(addr.getDistrict());
            d.setAddressDetail(addr.getDetail());
            d.setZipCode(addr.getZipCode());
        }
        return d;
    }
}