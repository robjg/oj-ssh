package org.oddjob.ssh;

import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.UserAuthNoneFactory;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.command.CommandFactory;
import org.apache.sshd.server.shell.ProcessShellCommandFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SshServerService {

    private static final Logger logger = LoggerFactory.getLogger(SshServerService.class);

    private String name;

    private int port;

    private SshServer sshServer;

    private KeyPairProvider keyPairProvider;

    private PasswordAuthenticator passwordAuthenticator;

    private PublickeyAuthenticator publickeyAuthenticator;

    private CommandFactory commandFactory;

    private boolean disableAuthentication;

    public void start() throws IOException {

        this.sshServer = SshServer.setUpDefaultServer();

        sshServer.setPort(this.port);

        sshServer.setKeyPairProvider(
                Objects.requireNonNull(this.keyPairProvider,
                        "No Key Pair Provider"));

        if (disableAuthentication) {
            logger.warn("** Authentication Disabled!!! ***");

            sshServer.setUserAuthFactories(List.of(new UserAuthNoneFactory()));
        }

        Optional.ofNullable(this.passwordAuthenticator)
                .ifPresent(sshServer::setPasswordAuthenticator);

        Optional.ofNullable(this.publickeyAuthenticator)
                .ifPresent(sshServer::setPublickeyAuthenticator);

        sshServer.setCommandFactory(
                Optional.ofNullable(this.commandFactory)
                .orElseGet(ProcessShellCommandFactory::new)
        );

        sshServer.start();


        if (this.port == 0) {
            this.port = sshServer.getPort();
        }
    }

    public void stop() throws IOException {

        this.sshServer.stop();

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public KeyPairProvider getKeyPairProvider() {
        return keyPairProvider;
    }

    public void setKeyPairProvider(KeyPairProvider keyPairProvider) {
        this.keyPairProvider = keyPairProvider;
    }

    public PasswordAuthenticator getPasswordAuthenticator() {
        return passwordAuthenticator;
    }

    public void setPasswordAuthenticator(PasswordAuthenticator passwordAuthenticator) {
        this.passwordAuthenticator = passwordAuthenticator;
    }

    public PublickeyAuthenticator getPublickeyAuthenticator() {
        return publickeyAuthenticator;
    }

    public void setPublickeyAuthenticator(PublickeyAuthenticator publickeyAuthenticator) {
        this.publickeyAuthenticator = publickeyAuthenticator;
    }

    public CommandFactory getCommandFactory() {
        return commandFactory;
    }

    public void setCommandFactory(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    public boolean isDisableAuthentication() {
        return disableAuthentication;
    }

    public void setDisableAuthentication(boolean disableAuthentication) {
        this.disableAuthentication = disableAuthentication;
    }

    @Override
    public String toString() {
        return Optional.ofNullable(this.name)
                .orElse(getClass().getSimpleName());
    }
}
