package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.common.config.ArmorHUDLayout;
import me.desht.pneumaticcraft.lib.Sounds;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@SideOnly(Side.CLIENT)
public class ArmorMessage {
    private final GuiAnimatedStat stat;
    int lifeSpan;

    public ArmorMessage(String title, List<String> message, int duration, int backColor) {
        lifeSpan = duration;
        stat = new GuiAnimatedStat(null, title, GuiAnimatedStat.StatIcon.NONE, backColor, null, ArmorHUDLayout.INSTANCE.messageStat);
        stat.setMinDimensionsAndReset(0, 0);
        stat.setText(message);
        EntityPlayer player = FMLClientHandler.instance().getClient().player;
        player.world.playSound(player.posX, player.posY, player.posZ, Sounds.SCIFI, SoundCategory.PLAYERS, 0.1F, 1.0F, true);
    }

    void setDependingMessage(GuiAnimatedStat dependingStat) {
        stat.setParentStat(dependingStat);
        stat.setBaseY(2);
    }

    public GuiAnimatedStat getStat() {
        return stat;
    }

    void renderMessage(FontRenderer fontRenderer, float partialTicks) {
        if (lifeSpan > 10) {
            stat.openWindow();
        } else {
            stat.closeWindow();
        }
        stat.render(-1, -1, partialTicks);
    }
}
