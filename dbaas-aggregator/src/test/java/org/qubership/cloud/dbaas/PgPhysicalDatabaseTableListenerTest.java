package org.qubership.cloud.dbaas;

import org.qubership.cloud.dbaas.repositories.dbaas.PhysicalDatabaseDbaasRepository;
import org.qubership.cloud.dbaas.service.event.listener.PgPhysicalDatabaseTableListener;
import io.agroal.api.AgroalDataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.postgresql.PGNotification;
import org.postgresql.jdbc.PgConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import static org.qubership.cloud.dbaas.mock.MockedQuarkusTransactionRunnableWrapper.withStaticMocks;


class PgPhysicalDatabaseTableListenerTest {

    @Mock
    PhysicalDatabaseDbaasRepository physicalDatabaseDbaasRepository;
    private final Object mutex = new Object();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        Mockito.doNothing().when(physicalDatabaseDbaasRepository).reloadH2Cache(Mockito.any());
        Mockito.doNothing().when(physicalDatabaseDbaasRepository).reloadH2Cache();
        Mockito.doReturn(mutex).when(physicalDatabaseDbaasRepository).getMutex();
    }

    @Test
    void getNotificationTest() throws SQLException, InterruptedException {

        PgConnection pgConnection = Mockito.mock(PgConnection.class);
        Mockito.when(pgConnection.createStatement()).thenReturn(Mockito.mock(Statement.class));

        PGNotification pgNotificationMock = Mockito.mock(PGNotification.class);
        Mockito.when(pgNotificationMock.getParameter()).thenReturn(UUID.randomUUID().toString());
        PGNotification[] pgNotifications = {pgNotificationMock};

        Connection connectionWrapper = Mockito.mock(Connection.class);
        Mockito.when(connectionWrapper.unwrap(PgConnection.class)).thenReturn(pgConnection);
        Mockito.when(connectionWrapper.createStatement()).thenReturn(Mockito.mock(Statement.class));
        Mockito.when(connectionWrapper.isWrapperFor(PgConnection.class)).thenReturn(true);


        AgroalDataSource datasource = Mockito.mock(AgroalDataSource.class);
        Mockito.when(datasource.getConnection()).thenReturn(connectionWrapper);

        Mockito.when(pgConnection.getNotifications(300_000)).thenReturn(pgNotifications).then(invocation ->
        {
            Thread.sleep(300);
            return null;
        });

        PgPhysicalDatabaseTableListener pgListener = new PgPhysicalDatabaseTableListener(datasource, physicalDatabaseDbaasRepository);

        new Thread(withStaticMocks(pgListener)).start();
        Thread.sleep(200);
        Mockito.verify(physicalDatabaseDbaasRepository).reloadH2Cache(Mockito.any());
        pgListener.stopListening();
    }

    @Test
    void stopListeningTest() throws SQLException, InterruptedException {
        PgConnection pgConnection = Mockito.mock(PgConnection.class);
        Mockito.when(pgConnection.createStatement()).thenReturn(Mockito.mock(Statement.class));

        PGNotification pgNotificationMock = Mockito.mock(PGNotification.class);
        Mockito.when(pgNotificationMock.getParameter()).thenReturn(UUID.randomUUID().toString());
        PGNotification[] pgNotifications = {pgNotificationMock};

        Connection connectionWrapper = Mockito.mock(Connection.class);
        Mockito.when(connectionWrapper.unwrap(PgConnection.class)).thenReturn(pgConnection);
        Mockito.when(connectionWrapper.createStatement()).thenReturn(Mockito.mock(Statement.class));
        Mockito.when(connectionWrapper.isWrapperFor(PgConnection.class)).thenReturn(true);


        Mockito.when(pgConnection.getNotifications(300_000)).then(invocation ->
        {
            Thread.sleep(300);
            return pgNotifications;
        });

        AgroalDataSource datasource = Mockito.mock(AgroalDataSource.class);
        Mockito.when(datasource.getConnection()).thenReturn(connectionWrapper);

        PgPhysicalDatabaseTableListener pgListener = new PgPhysicalDatabaseTableListener(datasource, physicalDatabaseDbaasRepository);

        new Thread(withStaticMocks(pgListener)).start();
        Thread.sleep(200);
        pgListener.stopListening();
        Thread.sleep(400);
        Mockito.verify(physicalDatabaseDbaasRepository).reloadH2Cache(Mockito.any());
        Mockito.verify(pgConnection, Mockito.atLeastOnce()).getNotifications(300_000);
    }
}
