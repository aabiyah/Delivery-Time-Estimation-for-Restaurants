package com.delivery.prediction.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PredictionRequest {

    private Integer marketId;
    private String category;
    private Integer totalItems;
    private Double subtotal;
    private Integer drivingDuration;  // Simplified field name
}
