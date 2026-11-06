package th.samo.adventureeconomy;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class NetworkHandler {
    private static final String VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(AdventureEconomy.MODID, "main"), () -> VERSION, VERSION::equals, VERSION::equals);
    private static int id = 0;

    private NetworkHandler() {}

    public static void register() {
        CHANNEL.messageBuilder(OpenScreenPacket.class, id++, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT)
            .encoder(OpenScreenPacket::encode).decoder(OpenScreenPacket::decode).consumerMainThread(OpenScreenPacket::handle).add();
        CHANNEL.messageBuilder(BankActionPacket.class, id++, net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER)
            .encoder(BankActionPacket::encode).decoder(BankActionPacket::decode).consumerMainThread(BankActionPacket::handle).add();
        CHANNEL.messageBuilder(SyncMoneyPacket.class, id++, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT)
            .encoder(SyncMoneyPacket::encode).decoder(SyncMoneyPacket::decode).consumerMainThread(SyncMoneyPacket::handle).add();
    }

    public static void open(ServerPlayer player, String page) {
        EconomyData.Account account = EconomyData.get(player.getServer()).account(player.getUUID());
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenScreenPacket(page, account.wallet, account.bank));
    }

    public static void sync(ServerPlayer player) {
        EconomyData.Account account = EconomyData.get(player.getServer()).account(player.getUUID());
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncMoneyPacket(account.wallet, account.bank));
    }
}
