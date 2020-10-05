package org.oddjob.ssh;

import org.oddjob.arooa.registry.ServiceProvider;
import org.oddjob.arooa.registry.Services;
import org.oddjob.state.CascadeJob;
import org.oddjob.state.StateConditions;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;

import javax.inject.Inject;
import java.util.Objects;
import java.util.Optional;

/**
 * @oddjob.description Run a sequence of SSH Jobs using the same connection.
 */
public class SshSequenceJob extends CascadeJob implements ServiceProvider {

    public static final String SSH_SESSION_SOURCE_SERVICE_NAME = "SshClientSessionSource";

    /**
     * @oddjob.property
     * @oddjob.description The Remote Connection.
     * @oddjob.required Yes.
     */
    private volatile SshConnection connection;

    private final Services connectionServices = new Services() {
        @Override
        public String serviceNameFor(Class<?> theClass, String flavour) {
            if (theClass == SshConnection.class) {
                return SSH_SESSION_SOURCE_SERVICE_NAME;
            } else {
                return null;
            }
        }

        @Override
        public SshConnection getService(String serviceName) throws IllegalArgumentException {
            return SshConnection.noClose(Objects.requireNonNull(connection,
                    "No Ssh Connection - expect to run as a child of " +
                            SshSequenceJob.this.toString()));
        }
    };

    @Override
    public Services getServices() {
        return connectionServices;
    }

    @Override
    protected void execute() throws InterruptedException {

        this.addStateListener(new StateListener() {
            @Override
            public void jobStateChange(StateEvent event) {
                if (StateConditions.DONE.test(event.getState())) {
                    connection.close();
                    removeStateListener(this);
                }
            }
        });

        super.execute();
    }

    public SshConnection getConnection() {
        return Optional.ofNullable(this.connection)
                .orElse(null);
    }

    @Inject
    public void setConnection(SshConnection connection) {
        this.connection = connection;
    }
}
