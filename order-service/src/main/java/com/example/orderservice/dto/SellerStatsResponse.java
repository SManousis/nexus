package com.example.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

@Value
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class SellerStatsResponse {
    List<ProductStat> bestSellingProducts;
    BigDecimal totalRevenue;
    int orderCount;

    @JsonCreator
    public SellerStatsResponse(
            @JsonProperty("bestSellingProducts") List<ProductStat> bestSellingProducts,
            @JsonProperty("totalRevenue") BigDecimal totalRevenue,
            @JsonProperty("orderCount") int orderCount) {
        this.bestSellingProducts = bestSellingProducts;
        this.totalRevenue = totalRevenue;
        this.orderCount = orderCount;
    }
}
