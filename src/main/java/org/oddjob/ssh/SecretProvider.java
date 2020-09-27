package org.oddjob.ssh;

public interface SecretProvider {

    String tell(String about, int retries);

    static SecretProvider fromPassword(String password) {
        return (about, retries) -> password;
    }
}
