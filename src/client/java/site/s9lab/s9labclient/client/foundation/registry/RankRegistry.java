package site.s9lab.s9labclient.client.foundation.registry;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import site.s9lab.s9labclient.client.foundation.model.S9Rank;

public final class RankRegistry {
    private static final List<S9Rank> BY_PRIORITY = Arrays.stream(S9Rank.values())
            .sorted(Comparator.comparingInt(S9Rank::priority).reversed())
            .toList();

    private RankRegistry() {
    }

    public static S9Rank resolve(String value) {
        return S9Rank.fromWire(value);
    }

    public static List<S9Rank> byPriority() {
        return BY_PRIORITY;
    }
}
