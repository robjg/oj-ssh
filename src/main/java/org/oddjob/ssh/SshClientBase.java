package org.oddjob.ssh;

import org.apache.sshd.client.session.ClientSession;
import org.oddjob.framework.adapt.Stop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * Base class for Scp and Exec Jobs.
 */
abstract  public class SshClientBase implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(SshClientBase.class);

    /**
     * @oddjob.property
     * @oddjob.description The name of the job. Can be any text.
     * @oddjob.required No.
     */
    private volatile String name;

    /**
     * @oddjob.property
     * @oddjob.description The Remote Connection. This will be automatically
     * injected if this is the child of an {@link SshSequenceJob}.
     * @oddjob.required Yes.
     */
    private volatile SshConnection connection;

    /** Used to close the session during stop. */
    private volatile ClientSession clientSession;

    @Override
    public Integer call() {

        try (SshConnection sshConnection = Objects.requireNonNull(this.connection, "No Client Connection")) {

            logger.info("Created connection to {}", sshConnection);

            this.clientSession = sshConnection.getClientSession();

            return withSession(clientSession);
        }
        finally {
            this.clientSession = null;
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
