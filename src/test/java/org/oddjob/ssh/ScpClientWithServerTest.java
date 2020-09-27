package org.oddjob.ssh;

import org.apache.sshd.server.scp.ScpCommandFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oddjob.OurDirs;
import org.oddjob.arooa.convert.ArooaConversionException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ScpClientWithServerTest {

    private SshServerService server;

    @BeforeEach
    public void setUp() throws ArooaConversionException, URISyntaxException {

        server = new SshServerService();

        FileKeyPair fileKeyPair = new FileKeyPair();
        fileKeyPair.setKeyFiles(
                Paths.get(Objects.requireNonNull(
                        getClass().getClassLoader().getResource("org/oddjob/ssh/id_rsa"))
                        .toURI())
        );

        server.setKeyPairProvider(fileKeyPair.toValue());

        ScpCommandFactory commandFactory = new ScpCommandFactory();
        server.setCommandFactory(commandFactory);
    }

    @AfterEach
    public void tearDown() throws IOException {

        server.stop();
    }

    @Test
    public void testFileUploadDownload() throws IOException, ArooaConversionException {

        Path workDir = OurDirs.workPathDir(getClass().getSimpleName(), true);
        Path serverWork = Files.createDirectory(workDir.resolve("server"));
        Path clientWork = Files.createDirectory(workDir.resolve("client"));

        server.setDisableAuthentication(true);
        server.start();

        SshConnectionValue connection = new SshConnectionValue();
        connection.setHost("localhost");
        connection.setPort(server.getPort());
        connection.setUser("foo");

        ScpClientJob scpJob = new ScpClientJob();
        scpJob.setConnection(connection.toValue());
        scpJob.setFrom(getClass().getResource("TheMonths.txt").getFile());
        scpJob.setRemote(serverWork.resolve("OurRemoteFile.txt").toString());
        scpJob.setTo(clientWork.resolve("TheMonthsCopy.txt").toString());

        scpJob.run();

        String[] originalLines = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream("TheMonths.txt"),
                        StandardCharsets.UTF_8))
                .lines()
                .toArray(String[]::new);

        String[] remoteLines = Files.lines(serverWork.resolve("OurRemoteFile.txt"))
                .toArray(String[]::new);

        String[] copyBackLines = Files.lines(clientWork.resolve("TheMonthsCopy.txt"))
                .toArray(String[]::new);

        assertThat(originalLines, is(remoteLines));
        assertThat(originalLines, is(copyBackLines));
    }
}
