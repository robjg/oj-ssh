package org.oddjob.ssh;

import org.apache.sshd.common.config.keys.FilePasswordProvider;
import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;
import org.oddjob.arooa.convert.Convertlet;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * Provides conversions for a {@link SecretProvider}. Provides a
 * conversions from a file and value. Providing a password as a value allows
 * it to be hard coded in the configuration, which probably isn't the best
 * idea.
 *
 * @see FileKeyPair
 * @see PuttyKeyPair
 */
public class SecretConversions implements ConversionProvider {

    public static Convertlet<SecretProvider, FilePasswordProvider> SECRET_TO_FILE_PASSWORD
            = from -> (session, resource, retries)
                -> from.tell(resource.getName(), retries);

    @Override
    public void registerWith(ConversionRegistry registry) {

        registry.register(SecretProvider.class, FilePasswordProvider.class,
                SECRET_TO_FILE_PASSWORD);

        registry.register(String.class, SecretProvider.class,
                from ->
                    new SecretProvider() {
                        @Override
                        public String tell(String about, int retries) {
                            if (retries > 0) {
                                return null;
                            }
                            else {
                                return from;
                            }
                        }

                        @Override
                        public String toString() {
                            return SecretProvider.class.getSimpleName() +
                                    " from " + String.class.getSimpleName();
                        }
                    });

        registry.register(InputStream.class, SecretProvider.class,
                from ->
                        new SecretProvider() {
                            @Override
                            public String tell(String about, int retries) {
                                if (retries > 0) {
                                    return null;
                                }
                                else {
                                    return new BufferedReader(new InputStreamReader(from))
                                            .lines().collect(Collectors.joining());
                                }
                            }

                            @Override
                            public String toString() {
                                return SecretProvider.class.getSimpleName() +
                                        " from " + InputStream.class.getSimpleName();
                            }
                        });

    }
}
