package com.delivery.prediction.controller;

import com.delivery.prediction.dto.PredictionRequest;
import com.delivery.prediction.model.Prediction;
import com.delivery.prediction.service.PredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PredictionController {

    @Autowired
    private PredictionService predictionService;

    @PostMapping("/predict/simple")
    public ResponseEntity<Map<String, Object>> predictSimple(@RequestBody PredictionRequest request) {
        // Map simplified input to the Prediction model
        Prediction data = Prediction.builder()
                .marketId(request.getMarketId())
                .createdAt(LocalDateTime.now())  // Default to current time
                .storePrimaryCategory(request.getCategory())
                .totalItems(request.getTotalItems())
                .subtotal(request.getSubtotal())
                .estimatedStoreToConsumerDrivingDuration(request.getDrivingDuration())
                .build();

        // Predict and save
        double estimatedTime = predictionService.predictSimpleDeliveryTime(data);
        data.setActualDeliveryTime(data.getCreatedAt().plusMinutes((long) estimatedTime));
        predictionService.savePrediction(data);

        // Create response
        Map<String, Object> response = new HashMap<>();
        response.put("estimatedTime", estimatedTime);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/predict/multiple")
    public ResponseEntity<Map<String, Object>> predictMultiple(@RequestBody PredictionRequest request) {
        // Map input to the Prediction model for multiple regression
        Prediction data = Prediction.builder()
                .marketId(request.getMarketId())
                .createdAt(LocalDateTime.now())
                .storePrimaryCategory(request.getCategory())
                .totalItems(request.getTotalItems())
                .subtotal(request.getSubtotal())
                .estimatedStoreToConsumerDrivingDuration(request.getDrivingDuration())
                .build();

        // Predict and save using multiple linear regression
        double estimatedTime = predictionService.predictMultipleDeliveryTime(data);
        data.setActualDeliveryTime(data.getCreatedAt().plusMinutes((long) estimatedTime));
        predictionService.savePrediction(data);

        // Create response
        Map<String, Object> response = new HashMap<>();
        response.put("estimatedTime", estimatedTime);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/predict/polynomial")
    public ResponseEntity<Map<String, Object>> predictPolynomial(@RequestBody PredictionRequest request) {
        // Map input to the Prediction model for polynomial regression
        Prediction data = Prediction.builder()
                .marketId(request.getMarketId())
                .createdAt(LocalDateTime.now())
                .storePrimaryCategory(request.getCategory())
                .totalItems(request.getTotalItems())
                .subtotal(request.getSubtotal())
                .estimatedStoreToConsumerDrivingDuration(request.getDrivingDuration())
                .build();

        // Predict and save using polynomial regression
        double estimatedTime = predictionService.predictPolynomialDeliveryTime(data);
        data.setActualDeliveryTime(data.getCreatedAt().plusMinutes((long) estimatedTime));
        predictionService.savePrediction(data);

        // Create response
        Map<String, Object> response = new HashMap<>();
        response.put("estimatedTime", estimatedTime);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/predictions")
    public ResponseEntity<List<Prediction>> getAllPredictions() {
        return ResponseEntity.ok(predictionService.getAllPredictions());
    }
}
