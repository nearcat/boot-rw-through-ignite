package com.github.iyboklee.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zaxxer.hikari.HikariDataSource;

import net.sf.log4jdbc.Log4jdbcProxyDataSource;

@Configuration
public class DataSourceConfig {

    @Autowired private DataSourceProperties dataSourceProperties;

    @Bean
    public DataSource dataSource() {
        DataSourceBuilder factory = DataSourceBuilder
                .create(dataSourceProperties.getClassLoader())
                .url(dataSourceProperties.getUrl())
                .username(dataSourceProperties.getUsername())
                .password(dataSourceProperties.getPassword());
        HikariDataSource dataSource = (HikariDataSource) factory.build();
        dataSource.setPoolName("[TEST] MyHikariSource");
        dataSource.setMinimumIdle(1);
        dataSource.setMaximumPoolSize(3);
        return new Log4jdbcProxyDataSource(dataSource);
    }

}
