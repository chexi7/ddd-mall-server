package com.ddd.mall.infrastructure.persistence.converter;

import com.ddd.mall.domain.member.Address;
import com.ddd.mall.domain.member.Member;
import com.ddd.mall.infrastructure.persistence.dataobject.MemberDO;

public class MemberConverter {

    public static Member toDomain(MemberDO d) {
        Member m = new Member(d.getUsername(), d.getPassword(), d.getNickname());
        m.setId(d.getId());
        m.setVersion(d.getVersion());
        m.setPhone(d.getPhone());
        m.setCreatedAt(d.getCreatedAt());
        m.clearDomainEvents();

        if (d.getProvince() != null) {
            m.setAddress(new Address(d.getProvince(), d.getCity(), d.getDistrict(), d.getAddressDetail(), d.getZipCode()));
        }
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
