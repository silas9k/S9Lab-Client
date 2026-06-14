package site.s9lab.s9labclient.client.foundation.registry;

import site.s9lab.s9labclient.client.foundation.model.EntitlementSet;
import site.s9lab.s9labclient.client.foundation.model.EntitlementType;
import site.s9lab.s9labclient.client.foundation.model.PlusStatus;
import site.s9lab.s9labclient.client.foundation.model.S9Rank;

public final class EntitlementRegistry {
    private EntitlementRegistry() {
    }

    public static EntitlementSet forUser(S9Rank rank, PlusStatus plusStatus) {
        EntitlementSet set = new EntitlementSet()
                .with(EntitlementType.PROFILE_SHOWCASE_SLOT, 3)
                .with(EntitlementType.COSMETIC_LOADOUT_SLOT, 1);
        if ((plusStatus != null && plusStatus.active()) || rank == S9Rank.PLUS) {
            set.with(EntitlementType.PLUS_BADGE, 1)
                    .with(EntitlementType.PROFILE_SHOWCASE_SLOT, 6)
                    .with(EntitlementType.COSMETIC_LOADOUT_SLOT, 3)
                    .with(EntitlementType.PREVIEW_BACKGROUND, 1)
                    .with(EntitlementType.ANIMATED_PROFILE_BORDER, 1)
                    .with(EntitlementType.MONTHLY_REWARD, 1);
        }
        return set;
    }
}
