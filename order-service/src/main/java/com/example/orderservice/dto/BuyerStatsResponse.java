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
public class BuyerStatsResponse {
    List<ProductStat> topProducts;
    List<ProductStat> mostBoughtProducts;
    BigDecimal totalSpent;
    int orderCount;

    @JsonCreator
    public BuyerStatsResponse(
            @JsonProperty("topProducts") List<ProductStat> topProducts,
            @JsonProperty("mostBoughtProducts") List<ProductStat> mostBoughtProducts,
            @JsonProperty("totalSpent") BigDecimal totalSpent,
            @JsonProperty("orderCount") int orderCount) {
        this.topProducts = topProducts;
        this.mostBoughtProducts = mostBoughtProducts;
        this.totalSpent = totalSpent;
        this.orderCount = orderCount;
    }
}
