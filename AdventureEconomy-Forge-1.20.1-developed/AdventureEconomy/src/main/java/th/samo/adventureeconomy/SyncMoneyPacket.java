package th.samo.adventureeconomy;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record SyncMoneyPacket(long wallet, long bank) {
    public static void encode(SyncMoneyPacket msg, FriendlyByteBuf buf) { buf.writeLong(msg.wallet); buf.writeLong(msg.bank); }
    public static SyncMoneyPacket decode(FriendlyByteBuf buf) { return new SyncMoneyPacket(buf.readLong(), buf.readLong()); }
    public static void handle(SyncMoneyPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> { ClientMoneyCache.update(msg.wallet(), msg.bank()); ClientScreens.updateBalances(msg.wallet(), msg.bank()); }));
        ctx.get().setPacketHandled(true);
    }
}
