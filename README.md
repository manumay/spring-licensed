# @Licensed-Annotation for Spring (Boot)

Experimental @Licensed annotation for Spring Boot to play with Spring's AOP features.
Supports the following license checks:

* hostname check, only allow requests for specific hostnames
* version check, only allow a specific application version
* user check, only allow a specific amount of users
* device check, only allow a specific amount of devices
* feature check, only allow specific features
* time check, only allow usage for a specific time period

There is a tool to generate license files, which need to be configured in the application. There also some beans which need to be configured. Please check the module spring-licensed-test for details.
