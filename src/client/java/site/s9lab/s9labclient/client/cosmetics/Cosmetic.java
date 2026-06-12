package site.s9lab.s9labclient.client.cosmetics;

import net.minecraft.util.Identifier;

public interface Cosmetic {
    String id();

    String displayName();

    CosmeticType type();

    Identifier texture();

    boolean animated();
}
