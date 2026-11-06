package th.samo.adventureeconomy;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.eventbus.api.IEventBus;

@Mod(AdventureEconomy.MODID)
public class AdventureEconomy {
    public static final String MODID = "adventureeconomy";

    public AdventureEconomy() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        NetworkHandler.register();
    }

    private void commonSetup(final FMLCommonSetupEvent event) { }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        event.getServer().getCommands().getDispatcher();
    }

    @EventBusSubscriber(modid = MODID)
    public static class Commands {
        @SubscribeEvent
        public static void registerCommands(RegisterCommandsEvent event) {
            EconomyCommands.register(event.getDispatcher());
            RPGCommands.register(event.getDispatcher());
            FeatureCommands.register(event.getDispatcher());
            event.getDispatcher().register(net.minecraft.commands.Commands.literal("ake")
                .then(net.minecraft.commands.Commands.literal("npc")
                    .then(net.minecraft.commands.Commands.literal("bank").executes(c -> openNpc(c.getSource(), "bank")))
                    .then(net.minecraft.commands.Commands.literal("market").executes(c -> openNpc(c.getSource(), "market")))
                    .then(net.minecraft.commands.Commands.literal("guild").executes(c -> openNpc(c.getSource(), "guild")))
                    .then(net.minecraft.commands.Commands.literal("shop").executes(c -> openNpc(c.getSource(), "shop")))
                    .then(net.minecraft.commands.Commands.literal("hud").executes(c -> openNpc(c.getSource(), "hud")))));
        }

        private static int openNpc(net.minecraft.commands.CommandSourceStack source, String page) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
            net.minecraft.server.level.ServerPlayer player = source.getPlayerOrException();
            NetworkHandler.open(player, page);
            return 1;
        }
    }

        @SubscribeEvent
        public void onPlayerLogin(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
                NetworkHandler.sync(player);
                net.minecraft.nbt.CompoundTag pd = RPGData.get(player.getServer()).player(player.getUUID());
                net.minecraft.nbt.ListTag pending = pd.getList("pendingTradeItems", net.minecraft.nbt.Tag.TAG_COMPOUND);
                for (int i=0;i<pending.size();i++) {
                    net.minecraft.world.item.ItemStack stack = net.minecraft.world.item.ItemStack.of(pending.getCompound(i));
                    if (!stack.isEmpty() && !player.getInventory().add(stack)) player.drop(stack,false);
                }
                pd.remove("pendingTradeItems");
                RPGData.get(player.getServer()).setDirty();
            }
        }

        @SubscribeEvent
        public void onMobKilled(net.minecraftforge.event.entity.living.LivingDeathEvent event) {
            if (!(event.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer p)) return;
            RPGData d = RPGData.get(p.getServer());
            var t = d.player(p.getUUID());
            if (t.getString("quest").equals("hunt_skeleton") &&
                event.getEntity().getType() == net.minecraft.world.entity.EntityType.SKELETON &&
                !t.getBoolean("questClaimed")) {
                t.putInt("questProgress", t.getInt("questProgress") + 1);
                d.setDirty();
            }
            if (t.getString("profession").equals("blacksmith") || t.getString("profession").equals("miner")) {
                addProfessionXp(t, 2); d.setDirty();
            }
        }

        @SubscribeEvent
        public void onBlockBroken(net.minecraftforge.event.level.BlockEvent.BreakEvent event) {
            if (!(event.getPlayer() instanceof net.minecraft.server.level.ServerPlayer p)) return;
            RPGData d = RPGData.get(p.getServer());
            var t = d.player(p.getUUID());
            var block = event.getState().getBlock();
            if (t.getString("quest").equals("gather_iron") &&
                (block == net.minecraft.world.level.block.Blocks.IRON_ORE ||
                 block == net.minecraft.world.level.block.Blocks.DEEPSLATE_IRON_ORE) &&
                !t.getBoolean("questClaimed")) {
                t.putInt("questProgress", t.getInt("questProgress") + 1);
            }
            String job=t.getString("profession");
            if (job.equals("miner") && (block == net.minecraft.world.level.block.Blocks.IRON_ORE ||
                block == net.minecraft.world.level.block.Blocks.DEEPSLATE_IRON_ORE ||
                block == net.minecraft.world.level.block.Blocks.COAL_ORE ||
                block == net.minecraft.world.level.block.Blocks.DEEPSLATE_COAL_ORE)) addProfessionXp(t, 3);
            if (job.equals("lumberjack") && block.defaultBlockState().is(net.minecraft.tags.BlockTags.LOGS)) addProfessionXp(t, 3);
            if (job.equals("farmer") && block.defaultBlockState().is(net.minecraft.tags.BlockTags.CROPS)) addProfessionXp(t, 2);
            d.setDirty();
        }

        private static void addProfessionXp(net.minecraft.nbt.CompoundTag t, int amount) {
            int xp=t.getInt("professionXp")+amount;
            while (xp >= t.getInt("professionLevel")*100) {
                xp -= t.getInt("professionLevel")*100;
                t.putInt("professionLevel",t.getInt("professionLevel")+1);
            }
            t.putInt("professionXp",xp);
        }
}
