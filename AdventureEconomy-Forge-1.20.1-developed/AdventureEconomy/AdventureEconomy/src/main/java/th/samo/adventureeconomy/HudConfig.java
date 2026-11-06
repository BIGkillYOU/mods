package th.samo.adventureeconomy;

import net.minecraftforge.fml.loading.FMLPaths;
import java.nio.file.*;
import java.util.Properties;

public final class HudConfig {
 public static int x=6,y=6; public static float scale=1.0f; public static boolean enabled=true;
 private static final Path FILE=FMLPaths.CONFIGDIR.get().resolve("adventureeconomy-hud.properties");
 static { load(); }
 private HudConfig(){}
 public static void load(){try{if(Files.exists(FILE)){Properties p=new Properties();try(var in=Files.newInputStream(FILE)){p.load(in);}x=Integer.parseInt(p.getProperty("x","6"));y=Integer.parseInt(p.getProperty("y","6"));scale=Math.max(.5f,Math.min(2f,Float.parseFloat(p.getProperty("scale","1"))));enabled=Boolean.parseBoolean(p.getProperty("enabled","true"));}}catch(Exception ignored){}}
 public static void save(){try{Properties p=new Properties();p.setProperty("x",String.valueOf(x));p.setProperty("y",String.valueOf(y));p.setProperty("scale",String.valueOf(scale));p.setProperty("enabled",String.valueOf(enabled));Files.createDirectories(FILE.getParent());try(var out=Files.newOutputStream(FILE)){p.store(out,"Adventure Economy HUD");}}catch(Exception ignored){}}
}
