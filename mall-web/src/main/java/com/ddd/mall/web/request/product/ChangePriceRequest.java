package com.ddd.mall.web.request.product;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 修改商品价格请求参数
 */
@Getter
@Setter
public class ChangePriceRequest {
    /**
     * 新价格
     */
    @NotNull(message = "价格不能为空")
    @Positive(message = "价格必须大于0")
    private BigDecimal newPrice;
}
