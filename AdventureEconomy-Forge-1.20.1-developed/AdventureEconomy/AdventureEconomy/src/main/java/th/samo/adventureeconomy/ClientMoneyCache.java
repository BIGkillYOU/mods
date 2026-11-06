package th.samo.adventureeconomy;

public final class ClientMoneyCache {
    public static volatile long wallet = 0;
    public static volatile long bank = 0;
    private ClientMoneyCache() {}
    public static void update(long newWallet, long newBank) { wallet = newWallet; bank = newBank; }
}
