package th.samo.adventureeconomy;

import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** Server-persistent economy ledger. All balances are stored as integer Gold. */
public class EconomyData extends SavedData {
    private static final String DATA_NAME = "adventureeconomy";
    private final Map<UUID, Account> accounts = new HashMap<>();
    private final List<String> audit = new ArrayList<>();
    private static final int MAX_LOGS = 5000;

    public static class Account {
        public long wallet;
        public long bank;
        public Account(long wallet, long bank) { this.wallet = wallet; this.bank = bank; }
    }

    public static EconomyData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(EconomyData::load, EconomyData::new, DATA_NAME);
    }

    public Account account(UUID id) {
        return accounts.computeIfAbsent(id, k -> new Account(500, 2500));
    }

    public long total(UUID id) { Account a = account(id); return a.wallet + a.bank; }

    public boolean deposit(UUID id, long amount) {
        if (amount <= 0) return false;
        Account a = account(id);
        if (a.wallet < amount) return false;
        a.wallet -= amount; a.bank += amount; setDirty();
        log("DEPOSIT", id, amount, "wallet->bank"); return true;
    }

    public boolean withdraw(UUID id, long amount) {
        if (amount <= 0) return false;
        Account a = account(id);
        if (a.bank < amount) return false;
        a.bank -= amount; a.wallet += amount; setDirty();
        log("WITHDRAW", id, amount, "bank->wallet"); return true;
    }

    public boolean pay(UUID from, UUID to, long amount) {
        if (amount <= 0 || from.equals(to)) return false;
        Account a = account(from), b = account(to);
        if (a.wallet < amount) return false;
        a.wallet -= amount; b.wallet += amount; setDirty();
        log("PAY", from, amount, "to=" + to); return true;
    }

    public void adminChange(UUID id, long amount, String action) {
        Account a = account(id);
        switch (action) {
            case "give" -> a.wallet = Math.addExact(a.wallet, amount);
            case "take" -> a.wallet = Math.max(0, a.wallet - amount);
            case "set" -> a.wallet = amount;
        }
        setDirty(); log("ADMIN_" + action.toUpperCase(Locale.ROOT), id, amount, "wallet");
    }

    public List<Map.Entry<UUID, Account>> top(int limit) {
        return accounts.entrySet().stream()
            .sorted((x,y) -> Long.compare(y.getValue().wallet + y.getValue().bank, x.getValue().wallet + x.getValue().bank))
            .limit(limit).toList();
    }

    public List<String> logs(UUID id, int limit) {
        return audit.stream().filter(s -> s.contains(id.toString())).skip(Math.max(0, audit.size()-limit)).toList();
    }

    private void log(String type, UUID id, long amount, String note) {
        audit.add(System.currentTimeMillis()+"|"+type+"|"+id+"|"+amount+"|"+note);
        while (audit.size() > MAX_LOGS) audit.remove(0);
    }

    @Override public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        accounts.forEach((id,a) -> {
            CompoundTag row = new CompoundTag();
            row.putUUID("uuid", id); row.putLong("wallet", a.wallet); row.putLong("bank", a.bank);
            list.add(row);
        });
        tag.put("accounts", list);
        ListTag logs = new ListTag();
        for (String line : audit) { CompoundTag row = new CompoundTag(); row.putString("line", line); logs.add(row); }
        tag.put("audit", logs);
        return tag;
    }

    public static EconomyData load(CompoundTag tag) {
        EconomyData data = new EconomyData();
        ListTag list = tag.getList("accounts", Tag.TAG_COMPOUND);
        for (int i=0;i<list.size();i++) {
            CompoundTag row = list.getCompound(i);
            data.accounts.put(row.getUUID("uuid"), new Account(Math.max(0,row.getLong("wallet")), Math.max(0,row.getLong("bank"))));
        }
        ListTag logs = tag.getList("audit", Tag.TAG_COMPOUND);
        for (int i=0;i<logs.size();i++) data.audit.add(logs.getCompound(i).getString("line"));
        return data;
    }
}
