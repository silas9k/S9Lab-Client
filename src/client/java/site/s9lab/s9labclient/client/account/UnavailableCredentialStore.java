package site.s9lab.s9labclient.client.account;

import java.util.Optional;

public final class UnavailableCredentialStore implements CredentialStore {
    @Override public Optional<String> load(String accountKey) { return Optional.empty(); }
    @Override public boolean save(String accountKey, String secret) { return false; }
    @Override public void remove(String accountKey) { }
    @Override public boolean available() { return false; }
    @Override public String displayName() { return "Unavailable"; }
}
