package th.samo.adventureeconomy;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import java.util.Map;

public final class EconomyCommands {
    private EconomyCommands() {}
    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("money").executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            var a = EconomyData.get(c.getSource().getServer()).account(p.getUUID());
            p.sendSystemMessage(Component.literal("§6Wallet: §e"+a.wallet+" Coin §7| §6Bank: §e"+a.bank+" Coin"));
            return 1;
        }));

        d.register(Commands.literal("bank")
            .then(Commands.literal("deposit").then(Commands.argument("amount", LongArgumentType.longArg(1)).executes(c -> {
                ServerPlayer p=c.getSource().getPlayerOrException(); long n=LongArgumentType.getLong(c,"amount");
                boolean ok=EconomyData.get(c.getSource().getServer()).deposit(p.getUUID(),n);
                p.sendSystemMessage(Component.literal(ok?"§aฝากเงิน "+n+" Coin สำเร็จ":"§cเงินใน Wallet ไม่เพียงพอ"));
                return ok?1:0;
            })))
            .then(Commands.literal("withdraw").then(Commands.argument("amount", LongArgumentType.longArg(1)).executes(c -> {
                ServerPlayer p=c.getSource().getPlayerOrException(); long n=LongArgumentType.getLong(c,"amount");
                boolean ok=EconomyData.get(c.getSource().getServer()).withdraw(p.getUUID(),n);
                p.sendSystemMessage(Component.literal(ok?"§aถอนเงิน "+n+" Coin สำเร็จ":"§cเงินใน Bank ไม่เพียงพอ"));
                return ok?1:0;
            }))));

        d.register(Commands.literal("pay")
            .then(Commands.argument("player", EntityArgument.player())
            .then(Commands.argument("amount", LongArgumentType.longArg(1)).executes(c -> {
                ServerPlayer from=c.getSource().getPlayerOrException(), to=EntityArgument.getPlayer(c,"player");
                long n=LongArgumentType.getLong(c,"amount");
                boolean ok=EconomyData.get(c.getSource().getServer()).pay(from.getUUID(),to.getUUID(),n);
                from.sendSystemMessage(Component.literal(ok?"§aโอน "+n+" Coin ให้ "+to.getGameProfile().getName()+" สำเร็จ":"§cโอนไม่สำเร็จ ตรวจสอบยอดเงิน"));
                if(ok) to.sendSystemMessage(Component.literal("§aได้รับเงิน "+n+" Coin จาก "+from.getGameProfile().getName()));
                return ok?1:0;
            }))));

        d.register(Commands.literal("baltop").executes(c -> {
            var data=EconomyData.get(c.getSource().getServer());
            c.getSource().sendSuccess(() -> Component.literal("§6--- Richest Adventurers ---"), false);
            int i=1;
            for (Map.Entry<java.util.UUID, EconomyData.Account> e : data.top(10)) {
                String name=c.getSource().getServer().getProfileCache().get(e.getKey()).map(x->x.getName()).orElse(e.getKey().toString().substring(0,8));
                long total=e.getValue().wallet+e.getValue().bank;
                final String line="§e"+(i++)+". §f"+name+" §7- §6"+total+" Coin";
                c.getSource().sendSuccess(() -> Component.literal(line), false);
            }
            return 1;
        }));

        d.register(Commands.literal("eco").requires(s -> s.hasPermission(2))
            .then(Commands.literal("give").then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("amount", LongArgumentType.longArg(0)).executes(c -> admin(c.getSource(),EntityArgument.getPlayer(c,"player"),LongArgumentType.getLong(c,"amount"),"give")))))
            .then(Commands.literal("take").then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("amount", LongArgumentType.longArg(0)).executes(c -> admin(c.getSource(),EntityArgument.getPlayer(c,"player"),LongArgumentType.getLong(c,"amount"),"take")))))
            .then(Commands.literal("set").then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("amount", LongArgumentType.longArg(0)).executes(c -> admin(c.getSource(),EntityArgument.getPlayer(c,"player"),LongArgumentType.getLong(c,"amount"),"set")))))
            .then(Commands.literal("inspect").then(Commands.argument("player", EntityArgument.player()).executes(c -> {
                ServerPlayer p=EntityArgument.getPlayer(c,"player"); var a=EconomyData.get(c.getSource().getServer()).account(p.getUUID());
                c.getSource().sendSuccess(() -> Component.literal(p.getName().getString()+": Wallet "+a.wallet+" Coin | Bank "+a.bank+" Coin"), false); return 1;
            })))
            .then(Commands.literal("log").then(Commands.argument("player", EntityArgument.player()).executes(c -> {
                ServerPlayer p=EntityArgument.getPlayer(c,"player"); var logs=EconomyData.get(c.getSource().getServer()).logs(p.getUUID(),20);
                c.getSource().sendSuccess(() -> Component.literal("Recent transactions for "+p.getName().getString()+": "+logs.size()), false);
                logs.forEach(line -> c.getSource().sendSuccess(() -> Component.literal("§7"+line), false)); return 1;
            }))));
    }

    private static int admin(CommandSourceStack source, ServerPlayer p, long amount, String action) {
        EconomyData.get(source.getServer()).adminChange(p.getUUID(),amount,action);
        source.sendSuccess(() -> Component.literal("§a"+action+" "+amount+" Coin for "+p.getGameProfile().getName()), true);
        return 1;
    }
}
