package org.oddjob.ssh;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.config.hosts.HostConfigEntryResolver;
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.keyprovider.KeyIdentityProvider;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.types.ValueFactory;
import org.oddjob.util.OddjobConfigException;
import org.oddjob.util.OddjobWrapperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public class SshConnectionValue implements ValueFactory<SshConnection> {

    private static final Logger logger = LoggerFactory.getLogger(SshClientBase.class);

    private String user;

    private SecretProvider passwordProvider;

    private String host;

    private int port = 22;

    private KeyIdentityProvider keyIdentityProvider;

    private long timeout = 60_000L;

    @Override
    public SshConnection toValue() throws ArooaConversionException {

        String user = Objects.requireNonNull(this.user, "User must be provided");
        String host = Objects.requireNonNull(this.host, "Host must be provided");
        int port = this.port;
        if (port <= 0) {
            throw new OddjobConfigException("Illegal port " + port);
        }
        long timeout = this.timeout;

        SshClient client = SshClient.setUpDefaultClient();

        client.setServerKeyVerifier(AcceptAllServerKeyVerifier.INSTANCE);
        client.setHostConfigEntryResolver(HostConfigEntryResolver.EMPTY);
        client.setKeyIdentityProvider(Optional.ofNullable(keyIdentityProvider)
                .orElse(KeyIdentityProvider.EMPTY_KEYS_PROVIDER));

        client.start();

        String connectionString =  connectionString(user, host, port);

        logger.info("Connecting to {}", connectionString);

        ClientSession session;
        try {
            session = client.connect(user, host, port)
                    .verify(timeout)
                    .getClientSession();
        } catch (IOException e) {
            throw new ArooaConversionException("Failed connecting to " + connectionString, e);
        }

        Runnable closeAction = () -> {

            logger.info("Closing session.");
            try {
                session.close(false);
            } finally {
                logger.info("Stopping client");
                client.stop();
            }
        };

        Optional.ofNullable(this.passwordProvider)
                .ifPresent(pwd -> session.addPasswordIdentity(
                        pwd.tell("Password", 0)));

        try {
            if (!session.auth().verify(timeout).isSuccess()) {
                throw new IllegalStateException("Not Authorised for " + connectionString);
            }
        } catch (IOException e) {

            closeAction.run();

            throw new OddjobWrapperException("Failed to authorise for " + connectionString, e);
        }

        logger.info("Session authorised successfully");

        return new SshConnection() {

            @Override
            public ClientSession getClientSession() {
                return session;
            }

            @Override
            public void close() {
                closeAction.run();
            }

            @Override
            public String toString() {
                return connectionString;
            }
        };

    }

    // connect without a checked exception.
    private static ClientSession connect(SshClient client,
                                         String user, String host, int port, long timeout) {

        String msg = "Connecting to " + connectionString(user, host, port);
        logger.info(msg);

        try {
            return client.connect(user, host, port)
                    .verify(timeout)
                    .getClientSession();
        } catch (IOException e) {
            throw new OddjobWrapperException("Failed " + msg, e);
        }
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public SecretProvider getPasswordProvider() {
        return passwordProvider;
    }

    public void setPasswordProvider(SecretProvider passwordProvider) {
        this.passwordProvider = passwordProvider;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public KeyIdentityProvider getKeyIdentityProvider() {
        return keyIdentityProvider;
    }

    public void setKeyIdentityProvider(KeyIdentityProvider keyIdentityProvider) {
        this.keyIdentityProvider = keyIdentityProvider;
    }

    static String connectionString(String user, String host, int port) {
        return "" + user + "@" + host + ":" + port;
    }
}
