package com.raldes.productservice.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {
    private String name;
    private String description;
    @Setter(onMethod_ = { @JsonSetter("price") })
    @Getter(onMethod_ = { @JsonGetter("price") })
    private BigDecimal prices;
}
