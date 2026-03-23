package aui.configuration;

import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;

public class PostgreSqlTestContainer {

    private static final String DOCKER_IMAGE_NAME = "postgres";

    protected static final PostgreSQLContainer<?> POSTGRES_CONTAINER;

    static {
        POSTGRES_CONTAINER = new PostgreSQLContainer<>(DOCKER_IMAGE_NAME)
                .withInitScript("init.sql");
    }

    public PostgreSqlTestContainer() {
        POSTGRES_CONTAINER.start();
    }

    public DataSource createDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(POSTGRES_CONTAINER.getDriverClassName());
        dataSource.setUrl(POSTGRES_CONTAINER.getJdbcUrl());
        dataSource.setUsername(POSTGRES_CONTAINER.getUsername());
        dataSource.setPassword(POSTGRES_CONTAINER.getPassword());

        return dataSource;
    }
}
