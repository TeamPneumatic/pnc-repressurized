package pneumaticCraft.client.render.pneumaticArmor;

import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.lib.Sounds;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ArmorMessage{
    private final GuiAnimatedStat stat;
    public int lifeSpan;

    public ArmorMessage(String title, List<String> message, int duration, int backColor){
        lifeSpan = duration;
        MainHelmetHandler mainOptions = HUDHandler.instance().getSpecificRenderer(MainHelmetHandler.class);
        stat = new GuiAnimatedStat(null, title, "", mainOptions.messagesStatX, mainOptions.messagesStatY, backColor, null, mainOptions.messagesStatLeftSided);
        stat.setMinDimensionsAndReset(0, 0);
        stat.setText(message);
        EntityPlayer player = FMLClientHandler.instance().getClient().thePlayer;
        player.worldObj.playSound(player.posX, player.posY, player.posZ, Sounds.SCIFI, 0.1F, 1.0F, true);
    }

    public void setDependingMessage(GuiAnimatedStat dependingStat){
        stat.setParentStat(dependingStat);
        stat.setBaseY(2);
    }

    public GuiAnimatedStat getStat(){
        return stat;
    }

    public void renderMessage(FontRenderer fontRenderer, float partialTicks){
        if(lifeSpan > 10) {
            stat.openWindow();
        } else {
            stat.closeWindow();
        }
        stat.render(-1, -1, partialTicks);
        // PneumaticCraftUtils.getPartOfString(
    }
}
