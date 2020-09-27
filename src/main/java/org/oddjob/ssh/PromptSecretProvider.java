package org.oddjob.ssh;

import org.apache.sshd.common.NamedResource;
import org.apache.sshd.common.SshException;
import org.apache.sshd.common.config.keys.FilePasswordProvider;
import org.apache.sshd.common.session.SessionContext;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.types.ValueFactory;
import org.oddjob.input.InputHandler;
import org.oddjob.input.InputRequest;
import org.oddjob.input.requests.InputPassword;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

public class PromptSecretProvider implements ValueFactory<SecretProvider> {

    public static final String PASSWORD_PROPERTY = "top.secret";

    private InputHandler inputHandler;

    private int retries;

    @Override
    public SecretProvider toValue() throws ArooaConversionException {

        InputHandler inputHandler = Optional.ofNullable(this.inputHandler)
                .orElseThrow(() -> new ArooaConversionException("No Input Handler"));

        return new SecretProvider() {
            @Override
            public String tell(String about, int retryIndex) {

                if (retryIndex > PromptSecretProvider.this.retries) {
                    return null;
                }
                else {
                    InputPassword inputPassword = new InputPassword();
                    inputPassword.setPrompt("Password for " + about);
                    inputPassword.setProperty(PASSWORD_PROPERTY);

                    Properties properties = inputHandler.handleInput(new InputRequest[] {  inputPassword });

                    return properties.getProperty(PASSWORD_PROPERTY);
                }
            }

            @Override
            public String toString() {
                return PromptSecretProvider.this.toString();
            }
        };
    }

    public InputHandler getInputHandler() {
        return inputHandler;
    }

    @Inject
    public void setInputHandler(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ", inputHandler=" + this.inputHandler +
                ", retries=" + retries;
    }
}
