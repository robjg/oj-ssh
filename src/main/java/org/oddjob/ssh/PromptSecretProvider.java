package org.oddjob.ssh;

import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.types.ValueFactory;
import org.oddjob.input.InputHandler;
import org.oddjob.input.InputRequest;
import org.oddjob.input.requests.InputPassword;

import javax.inject.Inject;
import java.util.Optional;
import java.util.Properties;

/**
 * @oddjob.description Provide a Password/Passphrase via a prompt.
 */
public class PromptSecretProvider implements ValueFactory<SecretProvider> {

    public static final String PASSWORD_PROPERTY = "top.secret";

    /**
     * @oddjob.property
     * @oddjob.description Provides the prompt.
     * @oddjob.required No, set automatically by Oddjob.
     */
    private InputHandler inputHandler;

    /**
     * @oddjob.property
     * @oddjob.description Possibly allow this many retries if whatever requires
     * the password supports this..
     * @oddjob.required No.
     */
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

                    try (InputHandler.Session session = inputHandler.start()) {
                        Properties properties = session.handleInput(new InputRequest[] {  inputPassword });
                        return properties.getProperty(PASSWORD_PROPERTY);
                    }
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
