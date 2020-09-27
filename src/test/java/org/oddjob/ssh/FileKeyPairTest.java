package org.oddjob.ssh;

import org.junit.jupiter.api.Test;
import org.oddjob.arooa.convert.ArooaConversionException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class FileKeyPairTest {


    @Test
    void testLoadKeyPair() throws ArooaConversionException, IOException, GeneralSecurityException, URISyntaxException {

        FileKeyPair test = new FileKeyPair();

        test.setKeyFiles(
                Paths.get(getClass().getClassLoader().getResource("org/oddjob/ssh/id_rsa").toURI())
        );

        List<KeyPair> keys = StreamSupport
                .stream(test.toValue().loadKeys(null).spliterator(), false)
                .collect(Collectors.toList());;

                assertThat(keys.size(), is(1));

                KeyPair keyPair = keys.get(0);

        assertThat(keyPair, notNullValue());

        assertThat(keyPair.getPrivate(), notNullValue());
        assertThat(keyPair.getPublic(), notNullValue());
    }

}
