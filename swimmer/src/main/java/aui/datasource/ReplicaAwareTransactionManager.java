package aui.datasource;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

@RequiredArgsConstructor
public class ReplicaAwareTransactionManager implements PlatformTransactionManager {

    private final PlatformTransactionManager wrappedTransactionManager;

    @Override
    public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
        try {
            boolean isReadOnly = definition != null && definition.isReadOnly();
            TransactionRoutingDataSource.setDataSource(isReadOnly);
            return wrappedTransactionManager.getTransaction(definition);
        } finally {
            TransactionRoutingDataSource.unloadDataSource();
        }
    }

    @Override
    public void commit(TransactionStatus status) throws TransactionException {
        wrappedTransactionManager.commit(status);
    }

    @Override
    public void rollback(TransactionStatus status) throws TransactionException {
        wrappedTransactionManager.rollback(status);
    }
}
