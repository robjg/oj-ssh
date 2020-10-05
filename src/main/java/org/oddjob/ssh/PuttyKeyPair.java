package org.oddjob.ssh;

import org.apache.sshd.common.config.keys.FilePasswordProvider;
import org.apache.sshd.common.config.keys.loader.KeyPairResourceParser;
import org.apache.sshd.common.config.keys.loader.putty.PuttyKeyUtils;
import org.apache.sshd.common.keyprovider.KeyIdentityProvider;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.session.SessionContext;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.types.ValueFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.Optional;

/**
 * @oddjob.description Provide a Key Pair from a Putty format file.
 *
 */
public class PuttyKeyPair implements ValueFactory<KeyIdentityProvider> {

    /**
     * @oddjob.property
     * @oddjob.description The file.
     * @oddjob.required Yes.
     */
    private Path keyFile;

    /**
     * @oddjob.property
     * @oddjob.description Provide the passphrase if the file is password protected.
     * @oddjob.required Maybe.
     */
    private FilePasswordProvider passphraseProvider;

    @Override
    public KeyPairProvider toValue() throws ArooaConversionException {

        Path keyFile = Optional.ofNullable(this.keyFile)
                .orElseThrow(() -> new ArooaConversionException("No Putty Key File"));

        return new KeyPairProvider() {
            @Override
            public Iterable<KeyPair> loadKeys(SessionContext session) throws IOException, GeneralSecurityException {
                KeyPairResourceParser parser = PuttyKeyUtils.DEFAULT_INSTANCE;

                return parser.loadKeyPairs(null, keyFile,  PuttyKeyPair.this.passphraseProvider);
            }

            @Override
            public String toString() {
                return PuttyKeyPair.this.toString();
            }
        };
    }

    public FilePasswordProvider getPassphraseProvider() {
        return passphraseProvider;
    }

    public void setPassphraseProvider(FilePasswordProvider passphraseProvider) {
        this.passphraseProvider = passphraseProvider;
    }

    public Path getKeyFile() {
        return keyFile;
    }

    public void setKeyFile(Path keyFile) {
        this.keyFile = keyFile;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ", keyFile=" + this.keyFile +
            ", passphraseProvider=" + this.passphraseProvider;
    }
}
