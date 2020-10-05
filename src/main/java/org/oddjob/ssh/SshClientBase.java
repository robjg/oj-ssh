package org.oddjob.ssh;

import org.apache.sshd.client.session.ClientSession;
import org.oddjob.framework.adapt.Stop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;

abstract  public class SshClientBase implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(SshClientBase.class);

    private volatile String name;

    private volatile SshConnection connection;

    private volatile ClientSession clientSession;

    @Override
    public Integer call() {

        SshConnection sshConnection = Objects.requireNonNull(this.connection, "No Client Connection");

        this.clientSession = sshConnection.getClientSession();

        try {
            return withSession(clientSession);
        }
        finally {
            this.clientSession = null;
            sshConnection.close();
        }
    }

    /**
     * Implementations do something here.
     *
     * @param session A started authorised session.
     */
    abstract Integer withSession(ClientSession session);

    @Stop
    public void stop() {
        Optional.ofNullable(this.clientSession)
                .ifPresent(cs -> cs.close(true));
    }

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
