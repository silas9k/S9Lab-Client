package site.s9lab.s9labclient.client.account;

import java.util.Optional;

/** Stores OAuth refresh tokens outside the account metadata file. */
public interface CredentialStore {
    Optional<String> load(String accountKey);
    boolean save(String accountKey, String secret);
    void remove(String accountKey);
    boolean available();
    String displayName();
}
