package th.samo.adventureeconomy;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record OpenScreenPacket(String page, long wallet, long bank) {
    public static void encode(OpenScreenPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.page, 32); buf.writeLong(msg.wallet); buf.writeLong(msg.bank);
    }
    public static OpenScreenPacket decode(FriendlyByteBuf buf) {
        return new OpenScreenPacket(buf.readUtf(32), buf.readLong(), buf.readLong());
    }
    public static void handle(OpenScreenPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> { ClientMoneyCache.update(msg.wallet(), msg.bank()); ClientScreens.open(msg.page(), msg.wallet(), msg.bank()); }));
        ctx.get().setPacketHandled(true);
    }
}
