Borsch
======

Simple project for testing how fine some technologies works together.

##Test target
Let's create service for storing user profiles, game profiles for example -- Profile Server.
Server provide API for:

 * creating profile
 * obtaining current state
 * modifying state
 
Server should use authentication/authorization mechanisms to prevent modifying profiles that does not belong to player.
 
##Used technologies

 * **Kotlin** as a general programming language
 * **gRPC** for communication
 * **JetBrains/Exposed** as ORM
 * **HikariCP** for pooling connections to DB
 * Logging by **slf4j+logback**
 * **JUnit** for testing
 * **Gradle** for building
