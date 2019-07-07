# @Licensed-Annotation for Spring (Boot)

Experimental @Licensed annotation for Spring Boot to play with Spring's AOP features. This was not meant for production use, it is just an experiment for creating a custom Spring annotation. It would be easy to remove the license check just by simply removing the Spring Boot Auto Configuration from the classpath.

The library supports the following license checks:

* hostname check, only allow requests for specific hostnames
* version check, only allow a specific application version
* user check, only allow a specific amount of users
* device check, only allow a specific amount of devices
* feature check, only allow specific features
* time check, only allow usage for a specific time period

There is a tool to generate license files, which need to be configured in the application. There also some beans which need to be configured. Please check the module spring-licensed-tool and spring-licensed-test for details.

You can start spring-licensed-test easily using the Spring Boot Dashboard in Eclipse. Use user1/password or user2/password to login into the application. The user interface is very simple, here are some URLs you might find useful:

* http://localhost:8080/ - lists user licensings
* http://localhost:8080/yes - feature which is licensed, should always return a status code 200
* http://localhost:8080/no - feature which is not licensed, should always return a status code 500
* http://localhost:8080/user - automatically acquires a license for the logged in user if available, should return status code 200 for the first logged in user and status code 500 for further users
* http://localhost:8080/actuator/license - display license information
* http://localhost:8080/actuator/licensecheck - checks current license
