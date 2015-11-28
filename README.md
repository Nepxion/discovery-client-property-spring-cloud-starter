# Spring Cloud Discovery Configuration Property Editor

> A Spring Cloud Configuration Property for discovering your configuration properties.

[![Build Status](https://travis-ci.org/jmnarloch/discovery-client-property-spring-cloud-starter.svg?branch=master)](https://travis-ci.org/jmnarloch/discovery-client-property-spring-cloud-starter)
[![Coverage Status](https://coveralls.io/repos/jmnarloch/discovery-client-property-spring-cloud-starter/badge.svg?branch=master&service=github)](https://coveralls.io/github/jmnarloch/discovery-client-property-spring-cloud-starter?branch=master)

## Features

## Setup

Add the Spring Cloud starter to your project:

```xml
<dependency>
  <groupId>io.jmnarloch</groupId>
  <artifactId>discovery-client-property-spring-cloud-starter</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Usage

Registers a custom `PropertySource` that allows to resolve a SpEL like expressions for ${discoveryClient.} prefix.
The underlying implementation will delegate to the registered ```DiscoveryClient``` instance allowing to resolve the
properties based on discovery registry.

Example:

```yaml
spring:
  data:
    elasticsearch:
      clusterNodes: ${discoveryClient.url(http://elasticsearch/)}
```

Currently supported operations:

* service - returns a single service url
* url - expands the url replacing the host name and port with discovered service values, any additional information
passed as the argument like the user login, path and query params will remain intact.

Notices that the property will be resolve only once during the application startup.

Currently this extension is most useful with [Consul](http://cloud.spring.io/spring-cloud-consul/) which allows you
to register any arbitrary service.

## License

Apache 2.0