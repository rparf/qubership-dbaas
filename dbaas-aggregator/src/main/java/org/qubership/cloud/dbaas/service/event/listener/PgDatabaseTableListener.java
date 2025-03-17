package org.qubership.cloud.dbaas.service.event.listener;

import org.qubership.cloud.dbaas.repositories.dbaas.DatabaseDbaasRepository;
import io.agroal.api.AgroalDataSource;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.UUID;

@Slf4j
public class PgDatabaseTableListener extends AbstractPgTableListener {

    private final DatabaseDbaasRepository databaseDbaasRepository;

    public PgDatabaseTableListener(AgroalDataSource dataSource,
                                   DatabaseDbaasRepository databaseDbaasRepository) throws SQLException {
        this.databaseDbaasRepository = databaseDbaasRepository;
        this.dataSource = dataSource;
        this.connectionStatement = "LISTEN database_table_changes_event";
        establishConnection(dataSource.getConnection());
    }

    @Override
    protected void reloadH2Cache(UUID id) {
        synchronized (databaseDbaasRepository.getMutex()) {
            databaseDbaasRepository.reloadH2Cache(id);
        }
    }

    @Override
    protected String tableName() {
        return "database";
    }
}
