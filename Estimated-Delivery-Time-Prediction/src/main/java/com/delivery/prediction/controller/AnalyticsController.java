package com.delivery.prediction.controller;

import com.delivery.prediction.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private static final Logger logger = Logger.getLogger(AnalyticsController.class.getName());

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/average-time")
    public ResponseEntity<Map<String, Object>> getAverageDeliveryTime() {
        try {
            Map<String, Object> result = analyticsService.calculateAverageDeliveryTime();
            
            if (result.containsKey("error")) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.severe("Error in average-time endpoint: " + e.getMessage());
            Map<String, Object> errorResponse = Map.of(
                "error", "Unexpected error: " + e.getMessage(),
                "status", "error"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/traffic-impact")
    public ResponseEntity<Map<String, Object>> getTrafficImpact() {
        try {
            Map<String, Object> result = analyticsService.calculateTrafficImpact();
            
            if (result.containsKey("error")) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.severe("Error in traffic-impact endpoint: " + e.getMessage());
            Map<String, Object> errorResponse = Map.of(
                "error", "Unexpected error: " + e.getMessage(),
                "status", "error"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}