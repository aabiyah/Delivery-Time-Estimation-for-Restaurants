package com.delivery.prediction.service;

import com.delivery.prediction.model.Prediction;
import com.delivery.prediction.repository.PredictionRepository;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.List;

@Service
public class PredictionService {

    private static final String MODEL_FILE_PATH = "src/main/java/com/delivery/prediction/trained_model/model.ser";  // Path to save/load the model file

    @Autowired
    private PredictionRepository deliveryDataRepository;

    private SimpleRegression simpleRegression;
    private OLSMultipleLinearRegression multipleLinearRegression;
    private PolynomialFunction polynomialRegression;

    @PostConstruct
    public void initializeModel() {
        // Load pre-trained SimpleRegression model if available
        try {
            simpleRegression = loadSimpleModel(MODEL_FILE_PATH);
            System.out.println("Loaded pre-trained simple regression model.");
        } catch (IOException | ClassNotFoundException e) {
            // No pre-trained model, train a new one
            System.out.println("No pre-trained simple regression model found. Training a new model.");
            simpleRegression = new SimpleRegression();
            List<Prediction> historicalData = deliveryDataRepository.findAll();
            int i = 0;
            for (Prediction data : historicalData) {
                double distance = data.getEstimatedStoreToConsumerDrivingDuration();
                double deliveryTime = java.time.Duration.between(data.getCreatedAt(), data.getActualDeliveryTime()).toMinutes();
                simpleRegression.addData(distance, deliveryTime);
                i++;
                if (i % 100 == 0) {
                    System.out.println(i + " records trained");
                }
            }
            // Save the model after training
            try {
                saveSimpleModel(simpleRegression, MODEL_FILE_PATH);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        // Initialize and train the Multiple Linear Regression model
        multipleLinearRegression = new OLSMultipleLinearRegression();
        trainMultipleRegressionModel();

        // Initialize and train Polynomial Regression model
        trainPolynomialRegressionModel();
    }

    private void trainMultipleRegressionModel() {
        List<Prediction> historicalData = deliveryDataRepository.findAll();
        int dataSize = historicalData.size();
        double[][] xData = new double[dataSize][3]; // Assuming 3 features: Total Items, Subtotal, Estimated Driving Duration
        double[] yData = new double[dataSize];

        int i = 0;
        for (Prediction data : historicalData) {
            xData[i][0] = data.getTotalItems(); // First feature
            xData[i][1] = data.getSubtotal();   // Second feature
            xData[i][2] = data.getEstimatedStoreToConsumerDrivingDuration();  // Third feature (could be another feature if necessary)
            yData[i] = java.time.Duration.between(data.getCreatedAt(), data.getActualDeliveryTime()).toMinutes(); // Target variable
            i++;
        }

        multipleLinearRegression.newSampleData(yData, xData);
    }

    private void trainPolynomialRegressionModel() {
        // Example: Polynomial regression with a quadratic function (degree 2)
        // You may define a more complex polynomial if needed
        double[] coefficients = new double[]{0.0, 0.5, 1.5};  // Example quadratic function: y = 0.5x^2 + 1.5x
        polynomialRegression = new PolynomialFunction(coefficients);  // Quadratic polynomial regression
    }

    public double predictSimpleDeliveryTime(Prediction newData) {
        // Predict using Simple Regression (based on driving duration)
        return simpleRegression.predict(newData.getEstimatedStoreToConsumerDrivingDuration());
    }

    public double predictMultipleDeliveryTime(Prediction newData) {
        // Estimate the regression parameters (coefficients and intercept)
        double[] regressionParameters = multipleLinearRegression.estimateRegressionParameters();
        double intercept = regressionParameters[0];  // Intercept is the first element
        double[] coefficients = new double[regressionParameters.length - 1];
        System.arraycopy(regressionParameters, 1, coefficients, 0, coefficients.length); // Remaining elements are coefficients

        // Calculate the predicted value using the regression equation:
        // prediction = intercept + (coef1 * totalItems) + (coef2 * subtotal) + (coef3 * estimatedDrivingDuration)
        double prediction = intercept
                + coefficients[0] * newData.getTotalItems()
                + coefficients[1] * newData.getSubtotal()
                + coefficients[2] * newData.getEstimatedStoreToConsumerDrivingDuration();

        return prediction;
    }

    public double predictPolynomialDeliveryTime(Prediction newData) {
        // Predict using Polynomial Regression (based on driving duration)
        return polynomialRegression.value(newData.getEstimatedStoreToConsumerDrivingDuration());
    }

    public List<Prediction> getAllPredictions() {
        return deliveryDataRepository.findAll();
    }

    public void savePrediction(Prediction data) {
        deliveryDataRepository.save(data);
    }

    private void saveSimpleModel(SimpleRegression regression, String filePath) throws IOException {
        // Serialize and save the model to a file
        try (FileOutputStream fileOut = new FileOutputStream(filePath);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(regression);
        }
    }

    private SimpleRegression loadSimpleModel(String filePath) throws IOException, ClassNotFoundException {
        // Load the serialized model from a file
        try (FileInputStream fileIn = new FileInputStream(filePath);
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            return (SimpleRegression) in.readObject();
        }
    }
}
