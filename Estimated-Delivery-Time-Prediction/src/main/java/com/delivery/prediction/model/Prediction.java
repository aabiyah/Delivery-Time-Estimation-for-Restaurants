package com.delivery.prediction.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "porter_data", schema = "predictions")
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer marketId;
    private LocalDateTime createdAt = LocalDateTime.now();  // Default to current time

    @Column(nullable = true)
    private LocalDateTime actualDeliveryTime;

    private String storePrimaryCategory;

    @Column(nullable = true)
    @Builder.Default
    private Integer orderProtocol = 1;  // Default to 1 if not provided

    private Integer totalItems;
    private Double subtotal;

    @Column(nullable = true)
    @Builder.Default
    private Integer numDistinctItems = 1;  // Default to 1 if not provided

    @Column(nullable = true)
    @Builder.Default
    private Double minItemPrice = 0.0;  // Default to 0 if not provided

    @Column(nullable = true)
    @Builder.Default
    private Double maxItemPrice = 0.0;  // Default to 0 if not provided

    @Column(nullable = true)
    @Builder.Default
    private Integer totalOnshiftDashers = 1;  // Default to 1 if not provided

    @Column(nullable = true)
    @Builder.Default
    private Integer totalBusyDashers = 0;  // Default to 0 if not provided

    @Column(nullable = true)
    @Builder.Default
    private Integer totalOutstandingOrders = 0;  // Default to 0 if not provided

    private Integer estimatedStoreToConsumerDrivingDuration;
}
