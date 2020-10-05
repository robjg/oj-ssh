package org.oddjob.ssh;

/**
 * Something that provides secrets.
 */
public interface SecretProvider {

    /**
     * Provide the secret.
     *
     * @param about Used for prompts.
     * @param retries The of times to retry if supported.
     * @return The password.
     */
    String tell(String about, int retries);

    /**
     * Provide athe password directly.
     *
     * @param password The password.
     * @return The password.
     */
    static SecretProvider fromPassword(String password) {
        return (about, retries) -> password;
    }
}
