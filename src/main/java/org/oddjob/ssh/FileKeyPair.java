package org.oddjob.ssh;

import org.apache.sshd.common.config.keys.FilePasswordProvider;
import org.apache.sshd.common.keyprovider.FileKeyPairProvider;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.types.ValueFactory;

import java.nio.file.Path;
import java.util.Optional;

/**
 * @oddjob.description Provide a Key Pair from Open SSH format files.
 * <p/>
 * This is the file format created with the 'ssh-keytool'
 * and defaults to 'id_rsa'. I think this form is also known as PEM, it's of the form
 * <pre>
 * -----BEGIN OPENSSH PRIVATE KEY-----
 * Base 64 stuff
 * -----END RSA PRIVATE KEY-----
 * </pre>
 * And not to be confused, as I did, with SSH2 format files hat begin {@code ---- BEGIN SSH2} (note the space) and
 * can have headers in before the Base 64 stuff. Oddjob doesn't currently support these.
 *
 */
public class FileKeyPair implements ValueFactory<KeyPairProvider> {

    /**
     * @oddjob.property
     * @oddjob.description The files.
     * @oddjob.required Yes, at least one..
     */
    private Path[] keyFiles;

    /**
     * @oddjob.property
     * @oddjob.description Provide the passphrase if the file is password protected.
     * @oddjob.required Maybe.
     */
    private FilePasswordProvider passphraseProvider;

    @Override
    public KeyPairProvider toValue() throws ArooaConversionException {

        Path[] keyFiles = Optional.ofNullable(this.keyFiles)
                .orElseThrow(() -> new ArooaConversionException("No key files"));

        FileKeyPairProvider provider = new FileKeyPairProvider(keyFiles);
        Optional.ofNullable(this.passphraseProvider)
                .ifPresent(provider::setPasswordFinder);

        return provider;
    }

    public Path[] getKeyFiles() {
        return keyFiles;
    }

    public void setKeyFiles(Path... keyFiles) {
        this.keyFiles = keyFiles;
    }

    public FilePasswordProvider getPassphraseProvider() {
        return passphraseProvider;
    }

    public void setPassphraseProvider(FilePasswordProvider passphraseProvider) {
        this.passphraseProvider = passphraseProvider;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ", keyFiles=" + this.keyFiles +
                ", passphraseProvider=" + this.passphraseProvider;
    }
}
