package org.oddjob.ssh;

import org.apache.sshd.server.auth.pubkey.KeySetPublickeyAuthenticator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.input.InputHandler;
import org.oddjob.logging.ConsoleOwner;
import org.oddjob.state.StateConditions;
import org.oddjob.tools.ConsoleCapture;
import org.oddjob.tools.StateSteps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SshExecExamplesTest {

    private static final Logger logger = LoggerFactory.getLogger(SshExecExamplesTest.class);

    private static SshServerService server;

    @BeforeAll
    public static void setUp() throws Exception {

        logger.info("---- setup -----");

        FileKeyPair fileKeyPair = new FileKeyPair();
        fileKeyPair.setKeyFiles(
                Paths.get(Objects.requireNonNull(
                        SshExecExamplesTest.class.getResource("id_rsa")).toURI())
        );

        server = new SshServerService();
        server.setKeyPairProvider(fileKeyPair.toValue());
        server.setPort(22);
        server.setCommandFactory(new SimpleCommandFactory());

        PublicKey publicKey = SshExecWithServerTest
                .decodeOpenSshPublicKey("/org/oddjob/ssh/id3_rsa.pub");
        PublicKey puttyPublicKey = SshExecWithServerTest
                .decodeSsh2PublicKey("/org/oddjob/ssh/PuttyPubKey");

        Set<? extends PublicKey> keys = Stream.of(publicKey, puttyPublicKey)
                .collect(Collectors.toSet());

        KeySetPublickeyAuthenticator pubKeys = new KeySetPublickeyAuthenticator("OurKeys", keys);

        server.setPublickeyAuthenticator(pubKeys);
        server.setPasswordAuthenticator((username, password, session)
                -> "please".equals(password));
        server.start();
    }

    @AfterAll
    public static void tearDown() throws IOException {

        server.stop();
    }

    @Test
    public void testWithKeyAuthentication() throws Exception {

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(new File(Objects.requireNonNull(
                getClass().getResource("ExecKeyConnectionExample.xml")).getFile()));

        StateSteps states = new StateSteps(oddjob);
        states.startCheck(StateConditions.READY, StateConditions.EXECUTING, StateConditions.COMPLETE);

        oddjob.load();

        ConsoleCapture consoleCapture = new ConsoleCapture();

        ConsoleOwner consoleOwner = new OddjobLookup(oddjob).lookup("exec", ConsoleOwner.class);

        try (AutoCloseable ignored = consoleCapture.capture(consoleOwner.consoleLog())) {
            oddjob.run();

            states.checkWait();
        }

        String[] lines = consoleCapture.getLines();

        assertThat(lines,
                is(new String[]{"hello"}));

    }

    @Test
    public void testWithPuttyKeyAuthentication() throws Exception {

        class OurInputHandler implements InputHandler {
            @Override
            public Session start() {
                return requests -> {
                    Properties properties = new Properties();
                    properties.setProperty(PromptSecretProvider.PASSWORD_PROPERTY, "secret");
                    return properties;
                };
            }
        }

        Oddjob oddjob = new Oddjob();
        oddjob.setInputHandler(new OurInputHandler());
        oddjob.setFile(new File(Objects.requireNonNull(
                getClass().getResource("ExecPuttyKeyConnection.xml")).getFile()));

        StateSteps states = new StateSteps(oddjob);
        states.startCheck(StateConditions.READY, StateConditions.EXECUTING, StateConditions.COMPLETE);

        oddjob.load();

        ConsoleCapture consoleCapture = new ConsoleCapture();

        ConsoleOwner consoleOwner = new OddjobLookup(oddjob).lookup("exec", ConsoleOwner.class);

        try (AutoCloseable ignored = consoleCapture.capture(consoleOwner.consoleLog())) {
            oddjob.run();

            states.checkWait();
        }

        String[] lines = consoleCapture.getLines();

        assertThat(lines,
                is(new String[]{"hello"}));

    }

    @Test
    public void testRedirectIO() throws Exception {

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(new File(Objects.requireNonNull(
                getClass().getResource("ExecRedirectingInput.xml")).getFile()));

        StateSteps states = new StateSteps(oddjob);
        states.startCheck(StateConditions.READY, StateConditions.EXECUTING, StateConditions.COMPLETE);

        oddjob.run();

        states.checkWait();

        assertThat(new OddjobLookup(oddjob).lookup("echo.text"),
                is("This will come back to haunt me!"));
    }

}
