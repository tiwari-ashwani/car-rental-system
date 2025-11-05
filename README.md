# Car Rental System

## Project Overview ##
    A modular, RESTful Car Rental Management System built using Spring Boot 3, Java 21, Lombok, and Spring Data JPA (Hibernate).  
    It manages Customers, Vehicles, and Bookings with strong validation, transactional logic, and clean REST APIs.


## Technologies Used
	
    |   Category      | Technology                    |
    |-----------------|-------------------------------|
    | Language        | Java 21                       |
    | Framework       | Spring Boot 3.x               |
    | ORM             | Spring Data JPA               |
    | Database        | H2 (in-memory)                |
    | Build Tool      | Maven                         |
    | Logging         | SLF4J + Logback               |
    | Testing         | WireMock , JUnit 5 + Mockito  |
    ---------------------------------------------------

##Prerequisites
JDK 21 or higher installed
Maven installed  
IDE (optional, but recommended)


## Local Setup
     1.Clone the Repository
         https://github.com/tiwari-ashwani/car-rental-system.git
         
     2. Build
          mvn clean package              
         
     3. Run as Spring Boot Application
            mvn spring-boot:run
     
     4. Run the application and swagger on below urls:
         http://localhost:8080/swagger-ui.html

     5. Generate API specs using OpenAPI generator for below APIs
            1. carrentalpricing_api.yaml
            2. drivinglicense_api.yaml

     6. Use Wiremock to mock the response from these APIs

## Usage and Main Operations
    The application provides the following RESTful endpoints:

      . POST /api/v1/customers - Create a new customer
      . GET  /api/v1/customers/{id} - Get customer details by ID
      . POST /api/v1/vehicles - Create a new vehicle
      . POST /api/v1/vehicles/bookings - Create a new vehicle booking



## Documentation ##

    [Swagger Link]
    http://localhost:8080/swagger-ui.html                                                                                                                 |


## Main Operations Performed by this API ##

    |  These services can perform                      |
    |--------------------------------------------------|
    |    1. Customer Management                        |
    |    2. Vehicle Management                         | 
    |    3. Booking - Retrievela and Creation          |
    |--------------------------------------------------|


## Version
	0.0.1-SNAPSHOT


### Enhancements ###
     * Security 
     * Authentication

### Contributors and Owners ###

     Repo Admin\Owner
      Ashwani Tiwari (tiwariashwanik@gmail.com)