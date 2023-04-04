>*Disclaimer:* 
>- *This project main purpose was a learning experience to tackle building a REST API with Spring Boot.*
>- *It may contain unpolished, experimental or rushed code that needs to be refactored and modernized.*
>
# Cook Book Spring App

A Java [Spring Boot](https://spring.io/projects/spring-boot) REST API exchanging data using the JSON format.  
This application allows users to browse and create cooking recipes.  

The persistence layer is managed by [Spring Data JPA](https://spring.io/projects/spring-data-jpa) and [Hibernate](https://hibernate.org).  
The JSON serialization/deserialization is handled by [Jackson](https://github.com/FasterXML/jackson).  

The application security is managed by [Spring Security](https://docs.spring.io/spring-security/reference/index.html) using a [JWT](https://jwt.io)-based authentication process.

[Lombok](https://projectlombok.org) is used to reduce boilerplate code.  
The mapping between Java bean types is done using [MapStruct](https://mapstruct.org).  
The project's build is managed by [Maven](https://maven.apache.org).  

>*(An Angular web application consuming this REST API can be found [here.](https://github.com/qble2/cook-book-angular-app))*

## Features:
This API allows a user to:
- Sign up.
- Sign in.
- Browse recipes.
- Filter recipes.
- Create recipes.
- Update recipes.
- Rate recipes.

## Built with:
- Java 17
- [Spring Boot 2.7.8](https://spring.io/projects/spring-boot)
- [Spring Security](https://docs.spring.io/spring-security/reference/index.html)
- [Java-JWT](https://github.com/auth0/java-jwt)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Hibernate](https://hibernate.org)
- [Jackson](https://github.com/FasterXML/jackson)
- [Lombok](https://projectlombok.org)
- [MapStruct](https://mapstruct.org)
- [Maven](https://maven.apache.org)
- [Google JIB Maven Plugin](https://github.com/GoogleContainerTools/jib/tree/master/jib-maven-plugin)

