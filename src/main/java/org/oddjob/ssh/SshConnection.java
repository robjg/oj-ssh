package org.oddjob.ssh;

import org.apache.sshd.client.session.ClientSession;

import java.util.Objects;

/**
 * Abstraction of an Apache SSHD connection that allows it to be shared
 * between jobs. Jobs always need to close their connections, if the
 * connection is shared, close will have no affect.
 *
 * @see SshSequenceJob
 */
public interface SshConnection extends AutoCloseable {

    ClientSession getClientSession();

    @Override
    void close();

    /**
     * Provides a connection that can be closed without any affect.
     *
     * @param wrapped The real connection.
     * @return A connection that can be closed.
     */
    static SshConnection noClose(SshConnection wrapped) {
        Objects.requireNonNull(wrapped);
        return new SshConnection() {
            @Override
            public ClientSession getClientSession() {
                return wrapped.getClientSession();
            }

            @Override
            public void close() {
                // It's for us to close.
            }

            @Override
            public String toString() {
                return wrapped.toString();
            }
        };
    }
}
