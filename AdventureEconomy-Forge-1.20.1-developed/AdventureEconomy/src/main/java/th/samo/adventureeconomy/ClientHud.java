package th.samo.adventureeconomy;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AdventureEconomy.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientHud {
    private ClientHud() {}
    @SubscribeEvent public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("adventure_economy_hud", (gui, pose, partialTick, width, height) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null || !HudConfig.enabled) return;
            pose.pose().pushPose();
            pose.pose().translate(HudConfig.x, HudConfig.y, 0);
            pose.pose().scale(HudConfig.scale, HudConfig.scale, 1.0f);
            pose.fill(0, 0, 136, 29, 0x990D0B08);
            pose.drawString(mc.font, Component.literal("Wallet: " + ClientMoneyCache.wallet + " Coin"), 5, 4, 0xFFFFD66B, true);
            pose.drawString(mc.font, Component.literal("Bank: " + ClientMoneyCache.bank + " Coin"), 5, 16, 0xFF8DD7FF, true);
            pose.pose().popPose();
        });
    }
}
