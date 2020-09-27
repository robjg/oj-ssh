package org.oddjob.ssh;

import org.apache.sshd.client.session.ClientSession;

import java.util.Objects;

public interface SshConnection extends AutoCloseable {

    ClientSession getClientSession();

    @Override
    void close();


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
