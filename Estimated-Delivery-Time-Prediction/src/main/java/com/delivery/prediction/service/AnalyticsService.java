package com.delivery.prediction.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.delivery.prediction.model.Prediction;
import com.delivery.prediction.repository.PredictionRepository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class AnalyticsService {

    private static final Logger logger = Logger.getLogger(AnalyticsService.class.getName());

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private PredictionService predictionService;
    
    @Autowired
    private PredictionRepository predictionRepository;

public Map<String, Object> calculateAverageDeliveryTime() {
    Map<String, Object> response = new HashMap<>();
    
    try {
        // Calculate model-based average times
        Map<String, Object> modelBasedTimes = calculateModelBasedAverageTimes();
        response.put("modelBasedPredictions", modelBasedTimes);
        
        // Add total record count for context
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM porter_data", Integer.class);
        response.put("totalRecords", count);
        
    } catch (Exception e) {
        logger.severe("Error in calculateAverageDeliveryTime: " + e.getMessage());
        e.printStackTrace();
        response.put("error", "Error calculating average delivery time: " + e.getMessage());
    }
    
    return response;
}

public Map<String, Object> calculateTrafficImpact() {
    Map<String, Object> response = new HashMap<>();
    
    try {
        // Add model-based traffic impact
        response.put("modelBasedTrafficImpact", calculateModelBasedTrafficImpact());
        
        // Add note explaining traffic impact
        response.put("note", "Traffic impact is the ratio of actual delivery time to estimated delivery time. Values greater than 1 indicate traffic delays.");
        
        // Add total record count
        Integer totalRecords = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM porter_data WHERE estimated_store_to_consumer_driving_duration > 0", 
            Integer.class
        );
        response.put("totalRecords", totalRecords);
        
    } catch (Exception e) {
        logger.severe("Error in calculateTrafficImpact: " + e.getMessage());
        e.printStackTrace();
        response.put("error", "Error calculating traffic impact: " + e.getMessage());
    }
    
    return response;
}
    
    // Calculate average delivery times using each prediction model
    private Map<String, Object> calculateModelBasedAverageTimes() {
        Map<String, Object> modelPredictions = new HashMap<>();
        
        try {
            // Get sample data to run predictions on
            List<Prediction> sampleData = predictionRepository.findAll();
            int limit = Math.min(sampleData.size(), 1000); // Limit to 1000 records for performance
            
            // Track predictions for each model
            double simpleRegTotal = 0;
            double multipleRegTotal = 0;
            double polynomialRegTotal = 0;
            double actualTimeTotal = 0;
            
            // Calculate model-based average times
            for (int i = 0; i < limit; i++) {
                Prediction data = sampleData.get(i);
                
                try {
                    // Get actual time
                    double actualTime = Duration.between(data.getCreatedAt(), data.getActualDeliveryTime()).toMinutes();
                    actualTimeTotal += actualTime;
                    
                    // Get predictions from each model
                    double simpleTime = predictionService.predictSimpleDeliveryTime(data);
                    double multipleTime = predictionService.predictMultipleDeliveryTime(data);
                    double polynomialTime = predictionService.predictPolynomialDeliveryTime(data);
                    
                    simpleRegTotal += simpleTime;
                    multipleRegTotal += multipleTime;
                    polynomialRegTotal += polynomialTime;
                } catch (Exception e) {
                    logger.warning("Skipping record in model prediction: " + e.getMessage());
                }
            }
            
            // Calculate averages
            double avgActual = actualTimeTotal / limit;
            double avgSimple = simpleRegTotal / limit;
            double avgMultiple = multipleRegTotal / limit;
            double avgPolynomial = polynomialRegTotal / limit;
            
            // Add results for each model
            Map<String, Object> simpleRegression = new HashMap<>();
            simpleRegression.put("averagePredictedTimeMinutes", avgSimple);
            simpleRegression.put("differenceFromActualMinutes", avgActual - avgSimple);
            simpleRegression.put("ratioToActual", avgActual / avgSimple);
            
            Map<String, Object> multipleRegression = new HashMap<>();
            multipleRegression.put("averagePredictedTimeMinutes", avgMultiple);
            multipleRegression.put("differenceFromActualMinutes", avgActual - avgMultiple);
            multipleRegression.put("ratioToActual", avgActual / avgMultiple);
            
            Map<String, Object> polynomialRegression = new HashMap<>();
            polynomialRegression.put("averagePredictedTimeMinutes", avgPolynomial);
            polynomialRegression.put("differenceFromActualMinutes", avgActual - avgPolynomial);
            polynomialRegression.put("ratioToActual", avgActual / avgPolynomial);
            
            // Add to result
            modelPredictions.put("actualAverageTimeMinutes", avgActual);
            modelPredictions.put("simpleRegression", simpleRegression);
            modelPredictions.put("multipleRegression", multipleRegression);
            modelPredictions.put("polynomialRegression", polynomialRegression);
            modelPredictions.put("sampleSize", limit);
            
            // Determine most accurate model
            double simpleDiff = Math.abs(avgActual - avgSimple);
            double multipleDiff = Math.abs(avgActual - avgMultiple);
            double polynomialDiff = Math.abs(avgActual - avgPolynomial);
            
            String mostAccurate = "simpleRegression";
            if (multipleDiff < simpleDiff && multipleDiff < polynomialDiff) {
                mostAccurate = "multipleRegression";
            } else if (polynomialDiff < simpleDiff && polynomialDiff < multipleDiff) {
                mostAccurate = "polynomialRegression";
            }
            
            modelPredictions.put("mostAccurateModel", mostAccurate);
            
        } catch (Exception e) {
            logger.severe("Error calculating model-based average times: " + e.getMessage());
            modelPredictions.put("error", "Error calculating model-based average times: " + e.getMessage());
        }
        
        return modelPredictions;
    }
    
    // Calculate traffic impact using each prediction model
    private Map<String, Object> calculateModelBasedTrafficImpact() {
        Map<String, Object> modelTrafficImpact = new HashMap<>();
        
        try {
            // Get data grouped by market
            List<Prediction> allData = predictionRepository.findAll();
            Map<String, List<Prediction>> marketData = new HashMap<>();
            
            for (Prediction data : allData) {
                //String marketId = data.getMarketId();
				String marketId = String.valueOf(data.getMarketId());
                if (marketId != null) {
                    if (!marketData.containsKey(marketId)) {
                        marketData.put(marketId, new ArrayList<>());
                    }
                    marketData.get(marketId).add(data);
                }
            }
            
            // Calculate model-based traffic impact by market
            Map<String, Object> marketImpacts = new HashMap<>();
            
            for (Map.Entry<String, List<Prediction>> entry : marketData.entrySet()) {
                String marketId = entry.getKey();
                List<Prediction> marketPredictions = entry.getValue();
                int limit = Math.min(marketPredictions.size(), 500); // Limit per market
                
                double actualTotal = 0;
                double simpleTotal = 0;
                double multipleTotal = 0;
                double polynomialTotal = 0;
                
                for (int i = 0; i < limit; i++) {
                    Prediction data = marketPredictions.get(i);
                    
                    try {
                        // Get actual delivery time
                        double actualTime = Duration.between(data.getCreatedAt(), data.getActualDeliveryTime()).toMinutes();
                        actualTotal += actualTime;
                        
                        // Get model predictions
                        double simpleTime = predictionService.predictSimpleDeliveryTime(data);
                        double multipleTime = predictionService.predictMultipleDeliveryTime(data);
                        double polynomialTime = predictionService.predictPolynomialDeliveryTime(data);
                        
                        simpleTotal += simpleTime;
                        multipleTotal += multipleTime;
                        polynomialTotal += polynomialTime;
                    } catch (Exception e) {
                        logger.warning("Skipping record in market traffic impact: " + e.getMessage());
                    }
                }
                
                // Calculate averages
                double avgActual = actualTotal / limit;
                double avgSimple = simpleTotal / limit;
                double avgMultiple = multipleTotal / limit;
                double avgPolynomial = polynomialTotal / limit;
                
                // Calculate traffic impact ratios
                Map<String, Object> marketResult = new HashMap<>();
                marketResult.put("averageActualMinutes", avgActual);
                marketResult.put("simpleRegressionPredictionMinutes", avgSimple);
                marketResult.put("multipleRegressionPredictionMinutes", avgMultiple);
                marketResult.put("polynomialRegressionPredictionMinutes", avgPolynomial);
                marketResult.put("simpleRegressionImpactRatio", avgActual / avgSimple);
                marketResult.put("multipleRegressionImpactRatio", avgActual / avgMultiple);
                marketResult.put("polynomialRegressionImpactRatio", avgActual / avgPolynomial);
                marketResult.put("sampleSize", limit);
                
                marketImpacts.put(marketId, marketResult);
            }
            
            // Calculate overall model-based traffic impact
            double overallActualTotal = 0;
            double overallSimpleTotal = 0;
            double overallMultipleTotal = 0;
            double overallPolynomialTotal = 0;
            int overallLimit = Math.min(allData.size(), 1000);
            
            for (int i = 0; i < overallLimit; i++) {
                Prediction data = allData.get(i);
                
                try {
                    // Get actual delivery time
                    double actualTime = Duration.between(data.getCreatedAt(), data.getActualDeliveryTime()).toMinutes();
                    overallActualTotal += actualTime;
                    
                    // Get model predictions
                    double simpleTime = predictionService.predictSimpleDeliveryTime(data);
                    double multipleTime = predictionService.predictMultipleDeliveryTime(data);
                    double polynomialTime = predictionService.predictPolynomialDeliveryTime(data);
                    
                    overallSimpleTotal += simpleTime;
                    overallMultipleTotal += multipleTime;
                    overallPolynomialTotal += polynomialTime;
                } catch (Exception e) {
                    logger.warning("Skipping record in overall traffic impact: " + e.getMessage());
                }
            }
            
            // Calculate overall averages
            double overallAvgActual = overallActualTotal / overallLimit;
            double overallAvgSimple = overallSimpleTotal / overallLimit;
            double overallAvgMultiple = overallMultipleTotal / overallLimit;
            double overallAvgPolynomial = overallPolynomialTotal / overallLimit;
            
            // Calculate overall traffic impact ratios
            Map<String, Object> overallResult = new HashMap<>();
            overallResult.put("averageActualMinutes", overallAvgActual);
            overallResult.put("simpleRegressionPredictionMinutes", overallAvgSimple);
            overallResult.put("multipleRegressionPredictionMinutes", overallAvgMultiple);
            overallResult.put("polynomialRegressionPredictionMinutes", overallAvgPolynomial);
            overallResult.put("simpleRegressionImpactRatio", overallAvgActual / overallAvgSimple);
            overallResult.put("multipleRegressionImpactRatio", overallAvgActual / overallAvgMultiple);
            overallResult.put("polynomialRegressionImpactRatio", overallAvgActual / overallAvgPolynomial);
            overallResult.put("sampleSize", overallLimit);
            
            // Add to result
            modelTrafficImpact.put("overallModelBasedTrafficImpact", overallResult);
            modelTrafficImpact.put("marketModelBasedTrafficImpact", marketImpacts);
            
        } catch (Exception e) {
            logger.severe("Error calculating model-based traffic impact: " + e.getMessage());
            modelTrafficImpact.put("error", "Error calculating model-based traffic impact: " + e.getMessage());
        }
        
        return modelTrafficImpact;
    }
}