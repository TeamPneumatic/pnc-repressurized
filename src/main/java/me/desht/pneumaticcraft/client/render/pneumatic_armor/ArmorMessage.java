package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.common.config.aux.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.core.ModSounds;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Collections;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ArmorMessage {
    private final WidgetAnimatedStat stat;
    int lifeSpan;

    public ArmorMessage(String title, int duration, int backColor) {
        this(title, Collections.emptyList(), duration, backColor);
    }

    public ArmorMessage(String title, List<String> message, int duration, int backColor) {
        lifeSpan = duration;
        stat = new WidgetAnimatedStat(null, title, WidgetAnimatedStat.StatIcon.NONE, backColor, null, ArmorHUDLayout.INSTANCE.messageStat);
        stat.setMinDimensionsAndReset(0, 0);
        stat.setText(message);
        PlayerEntity player = PneumaticCraftRepressurized.proxy.getClientPlayer();
        player.world.playSound(player.posX, player.posY, player.posZ, ModSounds.SCI_FI.get(), SoundCategory.PLAYERS, 0.1F, 1.0F, true);
    }

    void setDependingMessage(WidgetAnimatedStat dependingStat) {
        stat.setParentStat(dependingStat);
        stat.setBaseY(2);
    }

    public WidgetAnimatedStat getStat() {
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
