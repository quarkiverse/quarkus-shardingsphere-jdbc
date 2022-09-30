# Quarkus - Shardingsphere JDBC Extension

<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
<!-- ALL-CONTRIBUTORS-BADGE:END -->
[![Build](https://github.com/quarkiverse/quarkus-shardingsphere-jdbc/workflows/Build/badge.svg?branch=main)](https://github.com/quarkiverse/quarkus-shardingsphere-jdbc/actions?query=workflow%3ABuild)
[![License](https://img.shields.io/github/license/quarkiverse/quarkus-shardingsphere-jdbc)](http://www.apache.org/licenses/LICENSE-2.0)
[![Central](https://img.shields.io/maven-central/v/io.quarkiverse.shardingsphere/quarkus-shardingsphere-jdbc-parent?color=green)](https://search.maven.org/search?q=g:io.quarkiverse.shardingsphere%20AND%20a:quarkus-shardingsphere-jdbc-parent)

This extension provides a new datasource kind for Quarkus, which is based on [ShardingSphere JDBC](https://shardingsphere.apache.org/document/current/en/overview/). For more information about ShardingSphere, please refer to [ShardingSphere official website](https://shardingsphere.apache.org/).

## Getting Started
Add the following dependency in your pom.xml to get started,

```xml
<dependency>
    <groupId>io.quarkiverse.shardingsphere</groupId>
    <artifactId>quarkus-shardingsphere-jdbc</artifactId>
</dependency>
```

## Limitations
It only supports JVM mode for now. And if you want to work with Quarkus Hibernate ORM, you need to add some configurations to your `application.properties` file. Please check with the [Hibernate ORM guide](https://quarkus.io/guides/hibernate-orm#quarkus-hibernate-orm_quarkus.hibernate-orm.database.generation) and the Quarkus Shardingsphere JDBC documentation.

## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):
