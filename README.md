# Eco Charge Planner

You can see deployed website on https://eco-charge-planner.onrender.com/


Main idea — optimizing energy consumption by calculating the most optimal time windows for charging based on **live carbon intensity data**.

*Powered by live data from the [UK Carbon Intensity API](https://carbonintensity.org.uk/).*

---

## Project Highlights
* Strict separation: `Controller`-> `Service` -> `Client`
* Consumes `3rd-party` REST services for live energy stats.
* High `Unit test` coverage & `Integration testing`
* Fully `Dockerized` for production deployment.


---

## Backend (API)

**Tech Stack**: Java 21, Spring Boot 3.

### Endpoints

*   **`GET /api/energyMixAverage`**
    *   **Goal**: Retrieve energy generation mix.
    *   **Description**: List of daily summaries (clean energy %, fuel breakdown).

*   **`GET /api/optimalChargingWindow`**
    *   **Goal**: Find optimal time to charge.
    *   **Description**: Uses prefix sums for fast calculation
---

## Frontend (Web)

**Tech Stack**: React 19, Vite, TypeScript.

### Key Components

*   **`EnergyMixChart`**
    *   Uses responsive Pie Charts (MUI X Charts).
*   **`ChargingWindowForm`**
    *   User input for charging duration.
    *   Includes validation logic.
*   **`ResultsDisplay`**
    *   Displays optimal window results.
    *   Shows potential carbon savings.
---
## Testing
### 1. Unit Tests

We cover edge cases for each architectural layer:

#### **Controllers (`EnergyControllerTest`)**
*    **API Contract Verification**: 
     * Verifies correct JSON structure and HTTP status.
*   **Edge Case Verification**:
       * Handles cases where the service returns an empty list.
*   **Input validation**:
     * Checking input is an integer between 1 and 6

#### **Services (`EnergyServiceImplTest`)**
*   **Business Logic**:
    *   Validates the 3-day average energy mix calculation.
    *   Verifies the accuracy of the optimal charging window algorithm.
*   **Data Integrity Checks**:
    *   Ensures resilience against missing days or null API values.
    *   Tests boundary conditions (e.g., exact window size matches).
*   **Error Handling**:
    *   Confirms exception handling for insufficient data.
    *   Validates behavior when external API returns an empty response.

#### **Clients (`CarbonIntensityClientTest`)**
*   **Success Scenarios**:
    *   Verifies correct parsing of valid JSON responses.
*   **Error Handling**:
    *   Validates `HttpClientErrorException` throwing on 404 Not Found.
    *   Validates `HttpServerErrorException` throwing on 500 Server Error.

#### **Exception Handling (`GlobalExceptionHandlerTest`)**
*   **Client Error Mapping**:
    *   Tests mapping validation errors and type mismatches to 400 Bad Request.
    *   Tests mapping insufficient data exceptions to 400 Bad Request.
*   **Server Error Mapping**:
    *   Checks converting external API failures to 503 Service Unavailable.
    *   Handling unexpected internal errors as 500 Internal Server Error.

### 2. Integration Tests

`EnergyControllerIntegrationTest`
*   **Scope**: Full application flow (Controller + Service + Client).
*   **Tools**: `SpringBootTest`, `WireMock`.
*   **Coverage**:
    *   **External API Simulation**: WireMock stubs Carbon Intensity API.
    *   **End-to-End Logic**: Verifies data processing and aggregation.
    *   **Error Resilience**: Tests handling of external 500 errors.
    *  **Configuration Verification**: All application context load correctly with all beans and properties.

---
## Deployment & Setup

The system uses a **Reverse Proxy** as a single entry point to route traffic, isolating API and Web. This architecture eliminates CORS issues and simplifies frontend-backend communication by serving both from the same origin.

*   **Reverse Proxy (`nginx:alpine`)**:
    *   Acts as an API Gateway exposing only port `3000`.
    *   Routes `/api/*` to backend and `/*` to frontend.

*   **API Service (`Spring Boot`)**:
    *   Runs in a private network, inaccessible from the host.

*   **Web Service (`React served by Nginx`)**:
    *   Serves static assets via high-performance Nginx.


---

## How to Run

### Docker Run
* **Docker**
* **Docker Compose**

1.  **Start**: Run the following command in the project root:
    ```bash
    docker-compose up --build
    ```
2.  **Access**: Open your browser and go to `http://localhost:3000`.


### Manual Run (Local Dev)
* **Java 21** (JDK)
* **Node.js** (v18 or higher) and **npm**

1.  **Backend**:
    ```bash
    cd api
    ./mvnw spring-boot:run
    ```
2.  **Frontend**:
    ```bash
    cd web
    npm install
    npm run dev
    ```
3.  **Access**: Open `http://localhost:5173` (Vite default).