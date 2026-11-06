package th.samo.adventureeconomy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ClientScreens {
    private ClientScreens() {}
    public static void updateBalances(long wallet, long bank) {
        if (Minecraft.getInstance().screen instanceof EconomyScreen screen) screen.updateBalances(wallet, bank);
    }
    public static void open(String page, long wallet, long bank) {
        Minecraft.getInstance().setScreen(new EconomyScreen(page, wallet, bank));
    }
}
