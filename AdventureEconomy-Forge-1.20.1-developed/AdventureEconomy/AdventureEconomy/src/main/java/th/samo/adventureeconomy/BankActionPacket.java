package th.samo.adventureeconomy;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record BankActionPacket(int action, long amount) {
    public static void encode(BankActionPacket msg, FriendlyByteBuf buf) { buf.writeVarInt(msg.action); buf.writeLong(msg.amount); }
    public static BankActionPacket decode(FriendlyByteBuf buf) { return new BankActionPacket(buf.readVarInt(), buf.readLong()); }
    public static void handle(BankActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            //if (!(ctx.get().getSender() instanceof ServerPlayer player)) return;
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            if (msg.amount() < 1 || msg.amount() > 1_000_000_000L) return;
            EconomyData data = EconomyData.get(player.getServer());
            boolean success = switch (msg.action()) {
                case 0 -> data.deposit(player.getUUID(), msg.amount());
                case 1 -> data.withdraw(player.getUUID(), msg.amount());
                default -> false;
            };
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(success ? "§aทำรายการสำเร็จ: " + msg.amount() + " Coin" : "§cทำรายการไม่สำเร็จ: ตรวจสอบยอดเงิน"));
            NetworkHandler.sync(player);
        });
        ctx.get().setPacketHandled(true);
    }
}
