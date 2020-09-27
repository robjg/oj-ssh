package org.oddjob.ssh;

import org.apache.sshd.server.shell.ProcessShellCommandFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.logging.ConsoleOwner;
import org.oddjob.state.StateConditions;
import org.oddjob.tools.ConsoleCapture;
import org.oddjob.tools.StateSteps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SshSequenceJobTest {

    private static final Logger logger = LoggerFactory.getLogger(SshExecWithServerTest.class);

    private SshServerService server;

    @BeforeEach
    public void setUp() throws URISyntaxException, ArooaConversionException {

        logger.info("---- setup -----");

        FileKeyPair fileKeyPair = new FileKeyPair();
        fileKeyPair.setKeyFiles(
                Paths.get(Objects.requireNonNull(
                        getClass().getClassLoader().getResource("org/oddjob/ssh/id_rsa"))
                        .toURI())
        );

        server = new SshServerService();
        server.setKeyPairProvider(fileKeyPair.toValue());
//        server.setPort(22);
        server.setCommandFactory(new ProcessShellCommandFactory());

    }

    @AfterEach
    public void tearDown() throws IOException {

        server.stop();
    }

    @Test
    public void testInOddjob() throws ArooaConversionException, IOException, InterruptedException {

        server.setDisableAuthentication(true);
        server.start();

        logger.info("---- server started on port {} -----", server.getPort());

  //      Thread.sleep(Long.MAX_VALUE);


        Properties properties = new Properties();
        properties.setProperty("ssh.server.port", String.valueOf(server.getPort()));

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(new File(getClass().getResource("SequenceExample.xml").getFile()));
        oddjob.setProperties(properties);
        oddjob.load();

        OddjobLookup lookup = new OddjobLookup(oddjob);

        StateSteps states = new StateSteps(oddjob);
        states.startCheck(StateConditions.READY, StateConditions.EXECUTING,
                StateConditions.ACTIVE, StateConditions.COMPLETE);

        ConsoleCapture capture1 = new ConsoleCapture();
        ConsoleCapture capture2 = new ConsoleCapture();

        try (
                ConsoleCapture.Close ignored1 =
                        capture1.capture(lookup.lookup("exec1", ConsoleOwner.class).consoleLog());
                ConsoleCapture.Close ignored2 =
                        capture2.capture(lookup.lookup("exec2", ConsoleOwner.class).consoleLog())) {

            oddjob.run();

            states.checkWait();
        }

        String[] lines1 = capture1.getLines();
        String[] lines2 = capture2.getLines();

        assertThat(lines1[0], is("hello"));
        assertThat(lines2[0], is("goodbye"));
    }

}
