package com.kyohwee.ojt.global.config;

import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;

@Configuration
@EnableBatchProcessing(dataSourceRef = "metaDBSource", transactionManagerRef = "metaTransactionManager")
public class MetaDBConfig {

    @Primary
    @Bean(name = {"metaDBSource", "dataSource"})
    @ConfigurationProperties(prefix = "spring.datasource-meta")
    public DataSource metaDBSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = {"metaTransactionManager", "transactionManager"})
    public PlatformTransactionManager metaTransactionManager() {
        return new DataSourceTransactionManager(metaDBSource());
    }

    // jobRepository 빈 정의 제거
}