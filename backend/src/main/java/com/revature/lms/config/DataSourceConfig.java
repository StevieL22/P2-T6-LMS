package com.revature.lms.config;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class DataSourceConfig {

    // PostgreSQL connection properties
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties postgresProperties() {
        return new DataSourceProperties();
    }

    // Primary PostgreSQL datasource
    @Bean
    @Primary
    public DataSource postgresDataSource() {
        return postgresProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    // MySQL connection properties
    @Bean
    @ConfigurationProperties("app.datasource.mysql")
    public DataSourceProperties mysqlProperties() {
        return new DataSourceProperties();
    }

    // Secondary MySQL datasource
    @Bean
    public DataSource mysqlDataSource() {
        return mysqlProperties()
                .initializeDataSourceBuilder()
                .build();
    }
}