package org.oddjob.ssh;

import org.apache.sshd.common.config.keys.AuthorizedKeyEntry;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.config.keys.PublicKeyEntry;
import org.apache.sshd.common.config.keys.loader.ssh2.Ssh2PublicKeyEntryDecoder;
import org.apache.sshd.common.util.ValidateUtils;
import org.apache.sshd.server.auth.pubkey.KeySetPublickeyAuthenticator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

class SshExecWithServerTest {

    private static final Logger logger = LoggerFactory.getLogger(SshExecWithServerTest.class);

    private SshServerService server;

    @BeforeEach
    public void setUp(TestInfo testInfo) throws URISyntaxException, ArooaConversionException {

        logger.info("---- starting {} -----", testInfo.getDisplayName());

        FileKeyPair fileKeyPair = new FileKeyPair();
        fileKeyPair.setKeyFiles(
                Paths.get(Objects.requireNonNull(
                        getClass().getClassLoader().getResource("org/oddjob/ssh/id_rsa"))
                        .toURI())
        );

        server = new SshServerService();
        server.setKeyPairProvider(fileKeyPair.toValue());

        server.setCommandFactory(new SimpleCommandFactory());

    }

    @AfterEach
    public void tearDown() throws IOException {

        server.stop();
    }

    @Test
    void testExecWithPasswordAuth() throws IOException, ArooaConversionException {

        AtomicReference<String> userRef = new AtomicReference<>();
        AtomicReference<String> passwordRef = new AtomicReference<>();

        server.setPasswordAuthenticator((username, password, session) -> {
            userRef.set(username);
            passwordRef.set(password);

            return true;
        });

        server.start();

        SshConnectionValue connection = new SshConnectionValue();
        connection.setHost("localhost");
        connection.setPort(server.getPort());
        connection.setUser("foo");
        connection.setPasswordProvider(SecretProvider.fromPassword("bar"));
        connection.setTimeout(1000L);

        SshExecJob client = new SshExecJob();
        client.setConnection(connection.toValue());
        client.setCommand("cat");

        ByteArrayInputStream in = new ByteArrayInputStream("Hello\nGoodbye\n".getBytes());
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        client.setStdin(in);
        client.setStdout(result);

        assertThat(client.call(), is(0));

        assertThat(userRef.get(), is("foo"));
        assertThat(passwordRef.get(), is("bar"));

        assertThat(result.toString(), is("Hello\nGoodbye\n"));
    }

    @Test
    void testKeyPairClientAuthentication() throws Exception {

        PublicKey publicKey = decodeOpenSshPublicKey("/org/oddjob/ssh/id2_rsa.pub");

        Set<? extends PublicKey> keys = Stream.of(publicKey).collect(Collectors.toSet());

        KeySetPublickeyAuthenticator pubKeys = new KeySetPublickeyAuthenticator("OurKeys", keys);

        server.setPublickeyAuthenticator(pubKeys);
        server.start();

        FileKeyPair fileKeyPair = new FileKeyPair();
        fileKeyPair.setKeyFiles(Paths.get(Objects.requireNonNull(
                getClass().getClassLoader().getResource("org/oddjob/ssh/id2_rsa"))
                .toURI()));

        SshConnectionValue connection = new SshConnectionValue();
        connection.setHost("localhost");
        connection.setPort(server.getPort());
        connection.setUser("foo");
        connection.setKeyIdentityProvider(fileKeyPair.toValue());
        connection.setTimeout(1000L);

        SshExecJob client = new SshExecJob();
        client.setConnection(connection.toValue());
        client.setCommand("cat");

        ByteArrayInputStream in = new ByteArrayInputStream("Hello\nGoodbye\n".getBytes());
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        client.setStdin(in);
        client.setStdout(result);

        assertThat(client.call(), is(0));

        assertThat(result.toString(), is("Hello\nGoodbye\n"));
    }

    @Test
    void testPuttyKeyPairAuthentication() throws Exception {

        // Used puttygen to save as ssh2 key format
        PublicKey publicKey = decodeSsh2PublicKey("/org/oddjob/ssh/PuttyPubKey");

        Set<? extends PublicKey> keys = Stream.of(publicKey).collect(Collectors.toSet());

        KeySetPublickeyAuthenticator pubKeys = new KeySetPublickeyAuthenticator("OurKeys", keys);

        server.setPublickeyAuthenticator(pubKeys);

        server.start();

        PuttyKeyPair puttyKeyPair = new PuttyKeyPair();
        puttyKeyPair.setKeyFile(Paths.get(Objects.requireNonNull(
                getClass().getClassLoader().getResource("org/oddjob/ssh/PuttyKeyPair.ppk"))
                .toURI()));

        SshConnectionValue connection = new SshConnectionValue();
        connection.setHost("localhost");
        connection.setPort(server.getPort());
        connection.setUser("foo");
        connection.setKeyIdentityProvider(puttyKeyPair.toValue());
        connection.setTimeout(1000L);

        SshExecJob client = new SshExecJob();
        client.setConnection(connection.toValue());
        client.setCommand("cat");

        ByteArrayInputStream in = new ByteArrayInputStream("Hello\nGoodbye\n".getBytes());
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        client.setStdin(in);
        client.setStdout(result);

        assertThat(client.call(), is(0));

        assertThat(result.toString(), is("Hello\nGoodbye\n"));
    }

    @Test
    void testEncryptedKeyPairClientAuthentication() throws Exception {

        PublicKey publicKey = decodeOpenSshPublicKey("/org/oddjob/ssh/id3_rsa.pub");

        Set<? extends PublicKey> keys = Stream.of(publicKey).collect(Collectors.toSet());

        KeySetPublickeyAuthenticator pubKeys = new KeySetPublickeyAuthenticator("OurKeys", keys);

        server.setPublickeyAuthenticator(pubKeys);
        server.start();


        FileKeyPair fileKeyPair = new FileKeyPair();
        fileKeyPair.setKeyFiles(Paths.get(
                Objects.requireNonNull(
                        getClass().getClassLoader().getResource("org/oddjob/ssh/id3_rsa"))
                        .toURI()));
        fileKeyPair.setPassphraseProvider(SecretConversions.SECRET_TO_FILE_PASSWORD
                .convert(SecretProvider.fromPassword("secret")));

        SshConnectionValue connection = new SshConnectionValue();
        connection.setHost("localhost");
        connection.setPort(server.getPort());
        connection.setUser("foo");
        connection.setKeyIdentityProvider(fileKeyPair.toValue());
        connection.setTimeout(1000L);

        SshExecJob client = new SshExecJob();
        client.setConnection(connection.toValue());
        client.setCommand("cat");

        ByteArrayInputStream in = new ByteArrayInputStream("Hello\nGoodbye\n".getBytes());
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        client.setStdin(in);
        client.setStdout(result);

        assertThat(client.call(), is(0));

        assertThat(result.toString(), is("Hello\nGoodbye\n"));
    }

    @Test
    void testEncodedPuttyKeyPairAuthentication() throws Exception {

        // Used puttygen to save as ssh2 key format
        PublicKey publicKey = decodeSsh2PublicKey("/org/oddjob/ssh/PuttyPubKey");

        Set<? extends PublicKey> keys = Stream.of(publicKey).collect(Collectors.toSet());

        KeySetPublickeyAuthenticator pubKeys = new KeySetPublickeyAuthenticator("OurKeys", keys);

        server.setPublickeyAuthenticator(pubKeys);

        server.start();

        PuttyKeyPair puttyKeyPair = new PuttyKeyPair();
        puttyKeyPair.setKeyFile(Paths.get(
                Objects.requireNonNull(
                        getClass().getClassLoader().getResource("org/oddjob/ssh/PuttyKeyPairEnc.ppk"))
                        .toURI()));
        puttyKeyPair.setPassphraseProvider(SecretConversions.SECRET_TO_FILE_PASSWORD
                .convert(SecretProvider.fromPassword("secret")));

        SshConnectionValue connection = new SshConnectionValue();
        connection.setHost("localhost");
        connection.setPort(server.getPort());
        connection.setUser("foo");
        connection.setKeyIdentityProvider(puttyKeyPair.toValue());
        connection.setTimeout(1000L);

        SshExecJob client = new SshExecJob();
        client.setConnection(connection.toValue());
        client.setCommand("cat");

        ByteArrayInputStream in = new ByteArrayInputStream("Hello\nGoodbye\n".getBytes());
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        client.setStdin(in);
        client.setStdout(result);

        assertThat(client.call(), is(0));

        assertThat(result.toString(), is("Hello\nGoodbye\n"));
    }

    static PublicKey decodeOpenSshPublicKey(String resourceName)throws Exception {

        try (InputStream stream = ValidateUtils.checkNotNull(
                SshExecWithServerTest.class.getResourceAsStream(resourceName),
                "Missing test resource: %s", resourceName)) {

            Collection<? extends PublicKeyEntry> entries = AuthorizedKeyEntry.readAuthorizedKeys(stream, true);
            List<PublicKey> keys = PublicKeyEntry.resolvePublicKeyEntries(null, entries, null);

            return keys.get(0);
        }
    }


    static PublicKey decodeSsh2PublicKey(String resourceName) throws Exception {
        PublicKey key;
        try (InputStream stream = ValidateUtils.checkNotNull(
                SshExecWithServerTest.class.getResourceAsStream(resourceName), "Missing test resource: %s", resourceName)) {
            key = Ssh2PublicKeyEntryDecoder.INSTANCE.readPublicKey(null, () -> resourceName, stream);
        }

        assertThat("No key loaded from " + resourceName, key, notNullValue());

        String keyType = KeyUtils.getKeyType(key);

        assertThat("Unknown key type loaded from " + resourceName, keyType, notNullValue());
        return key;
    }

    @Test
    void testCommandWithoutStdIn() throws IOException, ArooaConversionException {

        server.setDisableAuthentication(true);

        server.start();

        SshConnectionValue connection = new SshConnectionValue();
        connection.setHost("localhost");
        connection.setPort(server.getPort());
        connection.setUser("foo");
        connection.setTimeout(1000L);

        SshExecJob client = new SshExecJob();
        client.setConnection(connection.toValue());
        client.setCommand("echo Hello");

        ByteArrayOutputStream result = new ByteArrayOutputStream();

        client.setStdout(result);
        assertThat(client.call(), is(0));

        assertThat(result.toString(), is("Hello\n"));
    }

    @Test
    void testHugeIo() throws IOException, ArooaConversionException {

        server.setDisableAuthentication(true);

        server.start();

        SshConnectionValue connection = new SshConnectionValue();
        connection.setHost("localhost");
        connection.setPort(server.getPort());
        connection.setUser("foo");
        connection.setTimeout(1000L);

        SshExecJob client = new SshExecJob();
        client.setConnection(connection.toValue());
        client.setCommand("cat");

        CountInputStream in = new CountInputStream(1_000_000);
        CountOutputStream out = new CountOutputStream();

        client.setStdin(in);
        client.setStdout(out);

        assertThat(client.call(), is(0));

        assertThat(out.getLast(), is(1_000_000));
    }

    @Test
    void testBadCommand() throws IOException, ArooaConversionException {

        server.setDisableAuthentication(true);

        server.start();

        SshConnectionValue connection = new SshConnectionValue();
        connection.setHost("localhost");
        connection.setPort(server.getPort());
        connection.setUser("foo");
        connection.setTimeout(1000L);

        SshExecJob client = new SshExecJob();
        client.setConnection(connection.toValue());
        client.setCommand("doh");

        ByteArrayOutputStream result = new ByteArrayOutputStream();

        client.setStdout(result);
        assertThat(client.call(), is(-1));

        // TODO: Any way to get the error message?
        assertThat(result.toString(), is(""));
    }

    @Test
    void testHangAndStop() throws IOException, ArooaConversionException, InterruptedException {

        server.setDisableAuthentication(true);

        server.start();

        SshConnectionValue connection = new SshConnectionValue();
        connection.setHost("localhost");
        connection.setPort(server.getPort());
        connection.setUser("foo");
        connection.setTimeout(1000L);

        SshExecJob client = new SshExecJob();
        client.setConnection(connection.toValue());
        client.setCommand("hang");

        ByteArrayOutputStream result = new ByteArrayOutputStream();

        client.setStdout(result);

        AtomicInteger res = new AtomicInteger();
        AtomicReference<Exception> er = new AtomicReference<>();

        Thread t = new Thread(() -> {
            try {
                res.set(client.call());
            }
            catch (Exception e ) {
                er.set(e);
            }
        });

        t.start();

        Thread.sleep(2000L);

        client.stop();

        // Todo: Is this the what we'd expect?
        assertThat(er.get(), nullValue());
        assertThat(res.get(), is(0));

    }
}

