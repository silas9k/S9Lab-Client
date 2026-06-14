package site.s9lab.s9labclient.client.foundation.model;

public record CreatorCodeSummary(
        String code,
        String displayName,
        boolean enabled,
        long activatedAt
) {
    public static final CreatorCodeSummary NONE = new CreatorCodeSummary("", "", false, 0L);
}
