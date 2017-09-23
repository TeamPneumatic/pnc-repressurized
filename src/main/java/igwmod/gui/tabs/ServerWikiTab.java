package igwmod.gui.tabs;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

import org.lwjgl.opengl.GL11;

import igwmod.IGWMod;
import igwmod.InfoSupplier;
import igwmod.gui.GuiWiki;
import igwmod.gui.LocatedTexture;
import igwmod.lib.IGWLog;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ServerWikiTab extends BaseWikiTab{
    private String serverName;
    private ItemStack iconStack;
    private LocatedTexture icon;

    public ServerWikiTab(){
        List<String> info = InfoSupplier.getInfo("igwmod", "server/properties", true);
        if(info != null) {
            for(String s : info) {
                String[] entry = s.split("=");
                if(entry[0].equals("server_name")) serverName = entry[1];
                if(entry[0].equals("icon_item")) {
                    String[] icon = entry[1].split(":");
//                    iconStack = new ItemStack(GameRegistry.findItem(icon[0], icon[1]));
                    iconStack = GameRegistry.makeItemStack(icon[0] + ":" + icon[1], 1, 1, "");
                    if(iconStack == null) {
                        IGWLog.warning("Couldn't find a server tab icon item stack for the name: " + entry[1]);
                    }
                }
            }
        }

        if(iconStack == null) {
            icon = new LocatedTexture(new ResourceLocation("server/tab_icon.png"), 5, 10, 27, 27);
        }

        File[] files = new File(IGWMod.proxy.getSaveLocation() + "\\igwmod").listFiles(new FilenameFilter(){
            @Override
            public boolean accept(File dir, String filename){
                return filename.endsWith(".txt");
            }
        });
        for(File file : files) {
            if(!file.getName().equals("properties.txt")) {
                pageEntries.add(file.getName().substring(0, file.getName().length() - 4));
            }
        }

    }

    @Override
    public String getName(){
        return serverName != null ? serverName : "Missing 'igwmod/properties.txt' with 'server_name=' key";
    }

    @Override
    public ItemStack renderTabIcon(GuiWiki gui){
        if(iconStack != null) {
            return iconStack;
        } else {
            GL11.glPushMatrix();
            GL11.glTranslated(0, -6, 0);
            icon.renderBackground(null, 0, 0);
            GL11.glPopMatrix();
            return null;
        }
    }

    @Override
    protected String getPageName(String pageEntry){
        return pageEntry;
    }

    @Override
    protected String getPageLocation(String pageEntry){
        return "server/" + pageEntry;
    }

}
