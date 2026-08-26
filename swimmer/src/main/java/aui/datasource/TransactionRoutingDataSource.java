package aui.datasource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static aui.datasource.DatabaseDataSourceType.READ_ONLY;
import static aui.datasource.DatabaseDataSourceType.READ_WRITE;

@Slf4j
public class TransactionRoutingDataSource extends AbstractRoutingDataSource {

    private static final ThreadLocal<DatabaseDataSourceType> CURRENT_DATA_SOURCE = new ThreadLocal<>();

    public TransactionRoutingDataSource(DataSource master, DataSource replica) {
        Map<Object, Object> dataSources = new HashMap<>();
        dataSources.put(READ_WRITE, master);
        dataSources.put(READ_ONLY, replica);

        super.setTargetDataSources(dataSources);
        super.setDefaultTargetDataSource(master);
    }

    static void setDataSource(boolean isReadOnly) {
        String databaseType = isReadOnly ? "replica (read-only)" : "writer (read-write)";
        log.info("Setting {} datasource.", databaseType);
        CURRENT_DATA_SOURCE.set(isReadOnly ? READ_ONLY : READ_WRITE);
    }

    static void unloadDataSource() {
        CURRENT_DATA_SOURCE.remove();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return CURRENT_DATA_SOURCE.get();
    }
}
