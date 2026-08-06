package aui.datasource;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfiguration {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties writeDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource-replica")
    public DataSourceProperties readDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource writeDataSource(@Qualifier("writeDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean
    public DataSource readDataSource(@Qualifier("readDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    // The @Primary datasource used by JPA. It hands out the writer or replica connection
    // depending on the read-only flag of the current transaction (see ReplicaAwareTransactionManager).
    @Bean
    @Primary
    public DataSource routingDataSource(@Qualifier("writeDataSource") DataSource writeDataSource,
                                        @Qualifier("readDataSource") DataSource readDataSource) {
        return new TransactionRoutingDataSource(writeDataSource, readDataSource);
    }

    @Bean
    public PlatformTransactionManager jpaTransactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    // Wraps the JPA transaction manager so the routing key is set right before the transaction begins.
    // Hibernate grabs the JDBC connection at begin (to disable autocommit), so the routing decision is
    // already in place by then - no LazyConnectionDataSourceProxy needed.
    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(
            @Qualifier("jpaTransactionManager") PlatformTransactionManager wrappedTransactionManager) {
        return new ReplicaAwareTransactionManager(wrappedTransactionManager);
    }
}
