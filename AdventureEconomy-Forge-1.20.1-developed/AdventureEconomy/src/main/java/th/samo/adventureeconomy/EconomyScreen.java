package th.samo.adventureeconomy;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class EconomyScreen extends Screen {
    private final String page;
    private long wallet;
    private long bank;
    private static final int PANEL_W = 300, PANEL_H = 190;

    protected EconomyScreen(String page, long wallet, long bank) {
        super(Component.literal("Adventure Kingdom"));
        this.page = page; this.wallet = wallet; this.bank = bank;
    }

    @Override protected void init() {
        int x = (width - PANEL_W) / 2, y = (height - PANEL_H) / 2;
        int bw = 126;
        if (page.equals("bank")) {
            addButton("ฝาก 100 Coin",x+22,y+112,bw,20,()->act(0,100));
            addButton("ถอน 100 Coin",x+152,y+112,bw,20,()->act(1,100));
            addButton("ฝาก 500 Coin",x+22,y+138,bw,20,()->act(0,500));
            addButton("ถอน 500 Coin",x+152,y+138,bw,20,()->act(1,500));
        } else if (page.equals("market") || page.equals("shop")) {
            addCommand("ดูรายการสินค้า","market list",x+22,y+112,bw,20);
            addCommand("ลงขายของในมือ 100 G","market sell 100",x+152,y+112,bw,20);
            addCommand("ดูรายการประมูล","auction list",x+22,y+138,bw,20);
            addCommand("เปิดร้านค้า","shop open",x+152,y+138,bw,20);
        } else if (page.equals("guild")) {
            addCommand("สมัครเข้ากิลด์","guild join",x+22,y+112,bw,20);
            addCommand("ดูเควส","quest list",x+152,y+112,bw,20);
            addCommand("รับเควสล่า Skeleton","quest accept hunt_skeleton",x+22,y+138,bw,20);
            addCommand("สถานะเควส","quest status",x+152,y+138,bw,20);
        } else if (page.equals("hud")) {
            addButton("←",x+22,y+75,42,20,()->{HudConfig.x=Math.max(0,HudConfig.x-5);HudConfig.save();});
            addButton("→",x+236,y+75,42,20,()->{HudConfig.x=Math.min(width-140,HudConfig.x+5);HudConfig.save();});
            addButton("↑",x+87,y+75,42,20,()->{HudConfig.y=Math.max(0,HudConfig.y-5);HudConfig.save();});
            addButton("↓",x+152,y+75,42,20,()->{HudConfig.y=Math.min(height-40,HudConfig.y+5);HudConfig.save();});
            addButton("ขนาด -",x+22,y+112,bw,20,()->{HudConfig.scale=Math.max(.5f,HudConfig.scale-.1f);HudConfig.save();});
            addButton("ขนาด +",x+152,y+112,bw,20,()->{HudConfig.scale=Math.min(2f,HudConfig.scale+.1f);HudConfig.save();});
            addButton("แสดง/ซ่อน HUD",x+87,y+138,126,20,()->{HudConfig.enabled=!HudConfig.enabled;HudConfig.save();});
        }
        addButton("ปิด",x+87,y+164,126,20,()->onClose());
    }
    private void addButton(String label,int x,int y,int w,int h,Runnable action) {
        addRenderableWidget(Button.builder(Component.literal(label),b->action.run()).bounds(x,y,w,h).build());
    }
    private void addCommand(String label,String command,int x,int y,int w,int h) {
        addButton(label,x,y,w,h,()->{
            if(net.minecraft.client.Minecraft.getInstance().player!=null)
                net.minecraft.client.Minecraft.getInstance().player.connection.sendCommand(command);
        });
    }

    public void updateBalances(long newWallet, long newBank) { this.wallet = newWallet; this.bank = newBank; }

    private void act(int action, long amount) { NetworkHandler.CHANNEL.sendToServer(new BankActionPacket(action, amount)); }

    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        int x = (width - PANEL_W) / 2, y = (height - PANEL_H) / 2;
        g.fill(x, y, x + PANEL_W, y + PANEL_H, 0xF0181714);
        g.fill(x + 2, y + 2, x + PANEL_W - 2, y + 3, 0xFFD5B46A);
        g.fill(x + 2, y + PANEL_H - 3, x + PANEL_W - 2, y + PANEL_H - 2, 0xFFD5B46A);
        g.drawCenteredString(font, Component.literal("✦ ADVENTURE KINGDOM ✦"), width / 2, y + 13, 0xFFE8CB86);
        g.drawCenteredString(font, titleForPage(), width / 2, y + 37, 0xFFFFFFFF);
        g.drawString(font, "Wallet: " + wallet + " Coin", x + 24, y + 65, 0xFFFFD66B);
        g.drawString(font, "Bank: " + bank + " Coin", x + 24, y + 82, 0xFF8DD7FF);
        if (page.equals("market") || page.equals("shop")) g.drawCenteredString(font, Component.literal("จัดการประกาศตลาดและร้านค้า"), width / 2, y + 91, 0xFFBBBBBB);
        if (page.equals("guild")) g.drawCenteredString(font, Component.literal("สมัครกิลด์ รับเควส และสะสมชื่อเสียง"), width / 2, y + 91, 0xFFBBBBBB);
        if (page.equals("hud")) g.drawCenteredString(font, Component.literal("ตำแหน่ง "+HudConfig.x+","+HudConfig.y+" | Scale "+HudConfig.scale+" | "+(HudConfig.enabled?"แสดง":"ซ่อน")), width / 2, y + 50, 0xFFBBBBBB);
        super.render(g, mouseX, mouseY, partialTick);
    }

    private Component titleForPage() {
        return Component.literal(switch (page) {
            case "market" -> "ตลาดผู้เล่น"; case "guild" -> "สมาคมนักผจญภัย"; case "shop" -> "ร้านค้า NPC"; case "hud" -> "ตั้งค่า HUD";
            default -> "ธนาคารกลาง";
        });
    }

    @Override public boolean isPauseScreen() { return false; }
}
