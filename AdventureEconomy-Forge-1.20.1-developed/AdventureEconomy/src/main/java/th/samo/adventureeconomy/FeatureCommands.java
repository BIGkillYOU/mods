package th.samo.adventureeconomy;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.*;
import net.minecraft.commands.*;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import java.util.*;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=AdventureEconomy.MODID)
public final class FeatureCommands {
 private FeatureCommands(){}
 private static RPGData d(CommandSourceStack s){return RPGData.get(s.getServer());}
 private static ServerPlayer self(CommandSourceStack s)throws com.mojang.brigadier.exceptions.CommandSyntaxException{return s.getPlayerOrException();}
 private static void msg(ServerPlayer p,String m){p.sendSystemMessage(Component.literal("§6[Adventure] §f"+m));}
 private static String key(UUID a,UUID b){return a.toString().compareTo(b.toString())<0?a+"_"+b:b+"_"+a;}
 private static CompoundTag trade(RPGData d,UUID id){for(CompoundTag t:d.trades.values())if(t.getUUID("a").equals(id)||t.getUUID("b").equals(id))return t;return null;}
 private static boolean side(CompoundTag t,UUID id){return t.getUUID("a").equals(id);}
 private static void refund(net.minecraft.server.MinecraftServer server,CompoundTag t){
  for(String side:new String[]{"a","b"}){UUID id=t.getUUID(side);var a=EconomyData.get(server).account(id);a.wallet=Math.addExact(a.wallet,t.getLong("money_"+side));ItemStack st=ItemStack.of(t.getCompound("item_"+side));ServerPlayer p=server.getPlayerList().getPlayer(id);if(!st.isEmpty()){
    if(p!=null){if(!p.getInventory().add(st))p.drop(st,false);}
    else {RPGData rd=RPGData.get(server);CompoundTag playerData=rd.player(id);net.minecraft.nbt.ListTag pending=playerData.getList("pendingTradeItems",net.minecraft.nbt.Tag.TAG_COMPOUND);pending.add(st.save(new CompoundTag()));playerData.put("pendingTradeItems",pending);rd.setDirty();}
   }}
  EconomyData.get(server).setDirty();
 }
 public static void register(CommandDispatcher<CommandSourceStack> disp){
  disp.register(Commands.literal("trade")
   .then(Commands.literal("request").then(Commands.argument("player",EntityArgument.player()).executes(c->{
    ServerPlayer a=self(c.getSource()),b=EntityArgument.getPlayer(c,"player");if(a==b)throw err("เลือกตัวเองไม่ได้");
    RPGData r=d(c.getSource());if(trade(r,a.getUUID())!=null||trade(r,b.getUUID())!=null)throw err("ผู้เล่นคนใดคนหนึ่งมีการแลกเปลี่ยนอยู่แล้ว");
    CompoundTag t=new CompoundTag();t.putUUID("a",a.getUUID());t.putUUID("b",b.getUUID());t.putLong("expires",System.currentTimeMillis()+120000);
    t.put("item_a",new CompoundTag());t.put("item_b",new CompoundTag());r.trades.put(key(a.getUUID(),b.getUUID()),t);r.setDirty();
    msg(a,"ส่งคำขอแลกเปลี่ยนให้ "+b.getGameProfile().getName());msg(b,a.getGameProfile().getName()+" ขอแลกเปลี่ยน ใช้ /trade accept");return 1;
   })))
   .then(Commands.literal("accept").executes(c->{ServerPlayer p=self(c.getSource());CompoundTag t=trade(d(c.getSource()),p.getUUID());if(t==null||System.currentTimeMillis()>t.getLong("expires"))throw err("ไม่มีคำขอที่ยังใช้งานได้");if(side(t,p.getUUID()))throw err("ผู้รับคำขอเท่านั้นที่กดยอมรับได้");t.putBoolean("accepted",true);d(c.getSource()).setDirty();msg(p,"เริ่มแลกเปลี่ยนแล้ว ใช้ /trade offer money <จำนวน> หรือ /trade offer hand");return 1;}))
   .then(Commands.literal("offer").then(Commands.literal("money").then(Commands.argument("amount",LongArgumentType.longArg(0,1000000000L)).executes(c->{
    ServerPlayer p=self(c.getSource());CompoundTag t=active(d(c.getSource()),p);String s=side(t,p.getUUID())?"a":"b";long n=LongArgumentType.getLong(c,"amount");var acc=EconomyData.get(p.getServer()).account(p.getUUID());long old=t.getLong("money_"+s);if(acc.wallet+old<n)throw err("Wallet ไม่เพียงพอ");acc.wallet=acc.wallet+old-n;t.putLong("money_"+s,n);t.putBoolean("confirm_a",false);t.putBoolean("confirm_b",false);EconomyData.get(p.getServer()).setDirty();d(c.getSource()).setDirty();msg(p,"ตั้งข้อเสนอเงิน "+n+" Coin แล้ว");return 1;
   })))
   .then(Commands.literal("hand").executes(c->{ServerPlayer p=self(c.getSource());CompoundTag t=active(d(c.getSource()),p);ItemStack hand=p.getMainHandItem();if(hand.isEmpty())throw err("ถือไอเทมในมือก่อน");String s=side(t,p.getUUID())?"a":"b";ItemStack old=ItemStack.of(t.getCompound("item_"+s));if(!old.isEmpty())throw err("ข้อเสนอมีไอเทมอยู่แล้ว ยกเลิกก่อน");t.put("item_"+s,hand.save(new CompoundTag()));hand.setCount(0);t.putBoolean("confirm_a",false);t.putBoolean("confirm_b",false);d(c.getSource()).setDirty();msg(p,"เพิ่มไอเทมในข้อเสนอแล้ว");return 1;})))
   .then(Commands.literal("confirm").executes(c->{ServerPlayer p=self(c.getSource());CompoundTag t=active(d(c.getSource()),p);String s=side(t,p.getUUID())?"a":"b";t.putBoolean("confirm_"+s,true);d(c.getSource()).setDirty();if(t.getBoolean("confirm_a")&&t.getBoolean("confirm_b")){
     UUID a=t.getUUID("a"),b=t.getUUID("b");EconomyData e=EconomyData.get(p.getServer());e.account(a).wallet+=t.getLong("money_b");e.account(b).wallet+=t.getLong("money_a");e.setDirty();
     for(String own:new String[]{"a","b"}){UUID receiver=own.equals("a")?b:a;ItemStack item=ItemStack.of(t.getCompound("item_"+own));ServerPlayer online=p.getServer().getPlayerList().getPlayer(receiver);if(online!=null&&!item.isEmpty()&&!online.getInventory().add(item))online.drop(item,false);}
     d(c.getSource()).trades.remove(key(a,b));d(c.getSource()).setDirty();msg(p,"แลกเปลี่ยนสำเร็จ ทั้งสองฝ่ายได้รับของแล้ว");
    }else msg(p,"ยืนยันแล้ว รออีกฝ่ายยืนยัน");return 1;}))
   .then(Commands.literal("cancel").executes(c->{ServerPlayer p=self(c.getSource());CompoundTag t=trade(d(c.getSource()),p.getUUID());if(t==null)throw err("ไม่มีการแลกเปลี่ยน");refund(p.getServer(),t);d(c.getSource()).trades.remove(key(t.getUUID("a"),t.getUUID("b")));d(c.getSource()).setDirty();msg(p,"ยกเลิกและคืนเงิน/ไอเทมแล้ว");return 1;}))
  );
  disp.register(Commands.literal("house")
   .then(Commands.literal("claim").then(Commands.argument("radius",IntegerArgumentType.integer(4,32)).then(Commands.argument("price",LongArgumentType.longArg(1)).executes(c->{
    ServerPlayer p=self(c.getSource());int radius=IntegerArgumentType.getInteger(c,"radius");RPGData r=d(c.getSource());long price=LongArgumentType.getLong(c,"price");var acc=EconomyData.get(p.getServer()).account(p.getUUID());if(acc.wallet<price)throw err("Wallet ไม่เพียงพอ");
    int x=p.getBlockX(),z=p.getBlockZ();for(CompoundTag h:r.houses.values())if(h.getString("dimension").equals(p.level().dimension().location().toString())){int rr=h.contains("radius")?h.getInt("radius"):16;if(Math.abs(x-h.getInt("x"))<=radius+rr&&Math.abs(z-h.getInt("z"))<=radius+rr)throw err("พื้นที่ทับซ้อนกับบ้าน "+h.getString("id"));}
    acc.wallet-=price;String id="H"+(r.houses.size()+1);CompoundTag h=new CompoundTag();h.putUUID("owner",p.getUUID());h.putInt("x",x);h.putInt("y",p.getBlockY());h.putInt("z",z);h.putInt("radius",radius);h.putString("dimension",p.level().dimension().location().toString());h.putString("id",id);h.putLong("paid",price);h.putString("members","");r.houses.put(id,h);r.setDirty();EconomyData.get(p.getServer()).setDirty();msg(p,"ยึดครองพื้นที่ "+id+" รัศมี "+radius+" บล็อกแล้ว");return 1;
   }))))
   .then(Commands.literal("trust").then(Commands.argument("id",StringArgumentType.word()).then(Commands.argument("player",EntityArgument.player()).executes(c->{ServerPlayer owner=self(c.getSource()),member=EntityArgument.getPlayer(c,"player");CompoundTag h=d(c.getSource()).houses.get(StringArgumentType.getString(c,"id"));if(h==null||!h.getUUID("owner").equals(owner.getUUID()))throw err("คุณไม่ใช่เจ้าของพื้นที่");String list=h.getString("members");if(!list.isEmpty())list+=",";h.putString("members",list+member.getUUID());d(c.getSource()).setDirty();msg(owner,"เพิ่ม "+member.getGameProfile().getName()+" เป็นสมาชิกพื้นที่แล้ว");return 1;}))))
   .then(Commands.literal("untrust").then(Commands.argument("id",StringArgumentType.word()).then(Commands.argument("player",EntityArgument.player()).executes(c->{ServerPlayer owner=self(c.getSource()),member=EntityArgument.getPlayer(c,"player");CompoundTag h=d(c.getSource()).houses.get(StringArgumentType.getString(c,"id"));if(h==null||!h.getUUID("owner").equals(owner.getUUID()))throw err("คุณไม่ใช่เจ้าของพื้นที่");List<String> m=new ArrayList<>(Arrays.asList(h.getString("members").split(",")));m.remove(member.getUUID().toString());h.putString("members",String.join(",",m));d(c.getSource()).setDirty();msg(owner,"นำสมาชิกออกแล้ว");return 1;}))))
  );
  disp.register(Commands.literal("guild").then(Commands.literal("leave").executes(c->{ServerPlayer p=self(c.getSource());CompoundTag t=d(c.getSource()).player(p.getUUID());t.putBoolean("guild",false);t.putInt("rank",0);d(c.getSource()).setDirty();msg(p,"ออกจากกิลด์แล้ว");return 1;})));
 }

 @net.minecraftforge.eventbus.api.SubscribeEvent
 public static void onServerTick(net.minecraftforge.event.TickEvent.ServerTickEvent e){
  if(e.phase!=net.minecraftforge.event.TickEvent.Phase.END)return;
  if(e.getServer().getTickCount()%20!=0)return;
  for(var it=RPGData.get(e.getServer()).trades.entrySet().iterator();it.hasNext();){
   var entry=it.next();CompoundTag t=entry.getValue();
   if(System.currentTimeMillis()>t.getLong("expires")){refund(e.getServer(),t);it.remove();RPGData.get(e.getServer()).setDirty();}
  }
 }
 @net.minecraftforge.eventbus.api.SubscribeEvent
 public static void onLogout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e){
  if(e.getEntity() instanceof ServerPlayer p){RPGData r=RPGData.get(p.getServer());CompoundTag t=trade(r,p.getUUID());if(t!=null){refund(p.getServer(),t);r.trades.remove(key(t.getUUID("a"),t.getUUID("b")));r.setDirty();}}
 }

 private static CompoundTag active(RPGData r,ServerPlayer p)throws com.mojang.brigadier.exceptions.CommandSyntaxException{CompoundTag t=trade(r,p.getUUID());if(t==null||!t.getBoolean("accepted")||System.currentTimeMillis()>t.getLong("expires"))throw err("ไม่มีการแลกเปลี่ยนที่ active");return t;}
 private static com.mojang.brigadier.exceptions.CommandSyntaxException err(String s){return new com.mojang.brigadier.exceptions.SimpleCommandExceptionType(Component.literal(s)).create();}
}
