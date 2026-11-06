package th.samo.adventureeconomy;

import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** Persistent server-side data for market, guild, quests, houses and RPG progression. */
public class RPGData extends SavedData {
    private static final String NAME="adventureeconomy_rpg";
    public final List<CompoundTag> listings=new ArrayList<>();
    public final Map<UUID,CompoundTag> players=new HashMap<>();
    public final Map<String,CompoundTag> houses=new LinkedHashMap<>();
    public final Map<String,CompoundTag> trades=new LinkedHashMap<>();
    public long treasury=0;
    public double taxRate=0.05;
    public String eventName="";
    public long eventEnd=0;

    public static RPGData get(MinecraftServer s) {
        return s.overworld().getDataStorage().computeIfAbsent(RPGData::load,RPGData::new,NAME);
    }
    public CompoundTag player(UUID id) {
        return players.computeIfAbsent(id,k->{
            CompoundTag t=new CompoundTag();
            t.putBoolean("guild",false); t.putInt("rank",0);
            t.putString("profession","none"); t.putInt("professionLevel",1); t.putInt("professionXp",0);
            t.putInt("guildRep",0); t.putInt("merchantRep",0); t.putInt("blackRep",0);
            t.putString("quest",""); t.putInt("questProgress",0); t.putBoolean("questClaimed",false);
            return t;
        });
    }
    public int nextId() { int n=1; for(CompoundTag t:listings)n=Math.max(n,t.getInt("id")+1); return n; }
    public CompoundTag listing(int id) { for(CompoundTag t:listings)if(t.getInt("id")==id)return t; return null; }
    public void saveDirty(){setDirty();}
    @Override public CompoundTag save(CompoundTag out) {
        out.putLong("treasury",treasury); out.putDouble("taxRate",taxRate);
        out.putString("eventName",eventName); out.putLong("eventEnd",eventEnd);
        ListTag ls=new ListTag(); for(CompoundTag t:listings)ls.add(t.copy()); out.put("listings",ls);
        ListTag ps=new ListTag(); players.forEach((id,t)->{CompoundTag x=t.copy();x.putUUID("uuid",id);ps.add(x);});out.put("players",ps);
        ListTag hs=new ListTag(); houses.forEach((id,t)->{CompoundTag x=t.copy();x.putString("id",id);hs.add(x);});out.put("houses",hs);
        ListTag ts=new ListTag(); trades.forEach((id,t)->{CompoundTag x=t.copy();x.putString("tradeKey",id);ts.add(x);});out.put("trades",ts);
        return out;
    }
    public static RPGData load(CompoundTag in) {
        RPGData d=new RPGData(); d.treasury=in.getLong("treasury"); d.taxRate=in.contains("taxRate")?in.getDouble("taxRate"):0.05;
        d.eventName=in.getString("eventName"); d.eventEnd=in.getLong("eventEnd");
        ListTag ls=in.getList("listings",Tag.TAG_COMPOUND);for(int i=0;i<ls.size();i++)d.listings.add(ls.getCompound(i).copy());
        ListTag ps=in.getList("players",Tag.TAG_COMPOUND);for(int i=0;i<ps.size();i++){CompoundTag t=ps.getCompound(i).copy();UUID id=t.getUUID("uuid");t.remove("uuid");d.players.put(id,t);}
        ListTag hs=in.getList("houses",Tag.TAG_COMPOUND);for(int i=0;i<hs.size();i++){CompoundTag t=hs.getCompound(i).copy();String id=t.getString("id");t.remove("id");d.houses.put(id,t);}
        ListTag ts=in.getList("trades",Tag.TAG_COMPOUND);for(int i=0;i<ts.size();i++){CompoundTag t=ts.getCompound(i).copy();String id=t.getString("tradeKey");t.remove("tradeKey");d.trades.put(id,t);}
        return d;
    }
}
