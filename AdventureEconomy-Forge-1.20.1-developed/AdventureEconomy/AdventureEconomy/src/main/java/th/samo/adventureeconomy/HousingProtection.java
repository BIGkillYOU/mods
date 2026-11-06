package th.samo.adventureeconomy;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.UUID;

@Mod.EventBusSubscriber(modid=AdventureEconomy.MODID)
public final class HousingProtection {
 private HousingProtection(){}
 private static boolean allowed(ServerPlayer p,int x,int z){
  if(p.hasPermissions(2))return true;
  for(CompoundTag h:RPGData.get(p.getServer()).houses.values()){
   if(!h.getString("dimension").equals(p.level().dimension().location().toString()))continue;
   int r=h.contains("radius")?h.getInt("radius"):16;
   if(Math.abs(x-h.getInt("x"))<=r&&Math.abs(z-h.getInt("z"))<=r){
    if(h.getUUID("owner").equals(p.getUUID()))return true;
    for(String id:h.getString("members").split(","))if(id.equals(p.getUUID().toString()))return true;
    return false;
   }
  }
  return true;
 }
 @SubscribeEvent public static void breakBlock(BlockEvent.BreakEvent e){
  if(e.getPlayer() instanceof ServerPlayer p&&!allowed(p,e.getPos().getX(),e.getPos().getZ())){e.setCanceled(true);p.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cพื้นที่นี้ได้รับการคุ้มครอง"));}
 }
 @SubscribeEvent public static void placeBlock(BlockEvent.EntityPlaceEvent e){
  if(e.getEntity() instanceof ServerPlayer p&&!allowed(p,e.getPos().getX(),e.getPos().getZ())){e.setCanceled(true);p.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cพื้นที่นี้ได้รับการคุ้มครอง"));}
 }
 @SubscribeEvent public static void interact(PlayerInteractEvent.RightClickBlock e){
  if(e.getEntity() instanceof ServerPlayer p&&!allowed(p,e.getPos().getX(),e.getPos().getZ())){e.setCanceled(true);p.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cพื้นที่นี้ได้รับการคุ้มครอง"));}
 }
}
