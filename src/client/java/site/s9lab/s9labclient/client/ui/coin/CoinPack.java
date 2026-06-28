package site.s9lab.s9labclient.client.ui.coin;

import net.minecraft.util.Identifier;

public record CoinPack(int baseCoins, int bonusCoins, String price, Identifier texture, int accentColor) {
    public int totalCoins() {
        return baseCoins + bonusCoins;
    }
}
