package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.common.config.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.core.Sounds;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ArmorMessage {
    private final GuiAnimatedStat stat;
    int lifeSpan;

    public ArmorMessage(String title, List<String> message, int duration, int backColor) {
        lifeSpan = duration;
        stat = new GuiAnimatedStat(null, title, GuiAnimatedStat.StatIcon.NONE, backColor, null, ArmorHUDLayout.INSTANCE.messageStat);
        stat.setMinDimensionsAndReset(0, 0);
        stat.setText(message);
        PlayerEntity player = PneumaticCraftRepressurized.proxy.getClientPlayer();
        player.world.playSound(player.posX, player.posY, player.posZ, Sounds.SCIFI, SoundCategory.PLAYERS, 0.1F, 1.0F, true);
    }

    void setDependingMessage(GuiAnimatedStat dependingStat) {
        stat.setParentStat(dependingStat);
        stat.setBaseY(2);
    }

    public GuiAnimatedStat getStat() {
        return stat;
    }

    void renderMessage(float partialTicks) {
        if (lifeSpan > 10) {
            stat.openWindow();
        } else {
            stat.closeWindow();
        }
        stat.render(-1, -1, partialTicks);
    }
}
