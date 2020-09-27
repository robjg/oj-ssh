package org.oddjob.ssh;

import org.apache.sshd.client.session.ClientSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Objects;
import java.util.Optional;

abstract  public class SshClientBase implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SshClientBase.class);

    private String name;

    private SshConnection connection;


    @Override
    public void run() {

        SshConnection sshConnection = Objects.requireNonNull(this.connection, "No Client Connection");

        ClientSession clientSession = sshConnection.getClientSession();

        try {
            withSession(clientSession);
        }
        finally {
            sshConnection.close();
        }
    }


    /**
     * Implementations do something here.
     *
     * @param session A started authorised session.
     */
    abstract void withSession(ClientSession session);

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SshConnection getConnection() {
        return connection;
    }

    @Inject
    public void setConnection(SshConnection connection) {
        this.connection = connection;
    }

    @Override
    public String toString() {
        return Optional.ofNullable(this.name).orElse(getClass().getSimpleName());
    }
}
