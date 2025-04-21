# 🍽️ Delivery Time Estimation for Restaurants

## Overview

This project is a full-stack web application that predicts restaurant delivery times using various regression models. Built with Java-based enterprise computing technologies (Spring Boot, MySQL), it includes RESTful services, a responsive frontend interface, and an interactive analytics dashboard.

The core goal is to support restaurants in forecasting delivery durations by analyzing order details and delivery patterns using machine learning.

---

## 📌 Key Features

- 🔍 **Prediction Models**:  
  - Simple Linear Regression  
  - Multiple Linear Regression  
  - Polynomial Regression

- 📊 **Analytics Dashboard**:  
  Real-time insights into:
  - Average delivery time
  - Traffic impact
  - Order parameters affecting delays

- 🖥️ **Frontend**:  
  - Built with HTML, CSS, JavaScript  
  - Dynamic form for user inputs  
  - Live visualization of analytics results

- 🧠 **Backend**:  
  - Spring Boot with REST APIs  
  - Integrated MySQL database  
  - Pre-trained model loading for fast response  
  - Modular, scalable architecture  
  - Swagger documentation included

---

## 🗃️ Dataset

- **Source**: [Porter Delivery Time Estimation - Kaggle](https://www.kaggle.com/datasets/ranitsarkar01/porter-delivery-time-estimation-dataset)  
- Contains order-level data: delivery time, market ID, total items, subtotal, etc.  
- Preprocessing:
  - Added auto-incrementing primary key
  - Set default values for non-essential fields

---

## 🛠️ Technical Stack

- **Frontend**: JavaScript, HTML, CSS
- **Backend**: Java Spring Boot
- **Database**: MySQL
- **Machine Learning**: Apache Commons Math
- **API Testing**: Postman, Swagger
- **Model Persistence**: Trained models saved and reused from `/trained_model`

---

## 📡 REST API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/predict/simple` | Predict using Simple Regression |
| `POST` | `/predict/multiple` | Predict using Multiple Regression |
| `POST` | `/predict/polynomial` | Predict using Polynomial Regression |
| `GET` | `/predictions` | Fetch all prediction records |
| `GET` | `/analytics/average-time` | Average delivery time |
| `GET` | `/analytics/traffic-impact` | Analyze traffic impact on delivery time |

---

## 🚀 Deployment

- Backend deployed on `localhost:8080`
- Frontend accessed via browser or local server
- Models loaded from disk to avoid retraining overhead

---

## 🧪 Testing

- Tested using Postman (functional testing of all API routes)
- Logging enabled with SLF4J for backend traceability
- Frontend tested for responsiveness and API communication

---

## 👩‍💻 Authors

- Aabiyah Zehra  
- Adarshan Nagara Anthal  
- Madhuri Chitrala  

---

## 📽️ Demo

- Presentation slides and demo video included
- Video covers architecture, prediction workflow, and API/frontend demonstration

---

## 📚 References

- [Apache Commons Math](https://commons.apache.org/proper/commons-math/)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [MySQL Docs](https://dev.mysql.com/doc/)
- [MDN JavaScript Docs](https://developer.mozilla.org/en-US/docs/Web/JavaScript)
- [Khan Academy: Polynomial Regression](https://www.khanacademy.org/math/statistics-probability)

---

## 📄 License

This project is for academic purposes as part of the CP630 - Enterprise Computing course at Wilfrid Laurier University.

