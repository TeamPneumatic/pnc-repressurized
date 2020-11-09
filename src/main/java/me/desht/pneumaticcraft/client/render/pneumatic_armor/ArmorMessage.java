package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.config.subconfig.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.core.ModSounds;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;

import java.util.Collections;
import java.util.List;

public class ArmorMessage {
    private final WidgetAnimatedStat stat;
    int lifeSpan;

    public ArmorMessage(ITextComponent title, int duration, int backColor) {
        this(title, Collections.emptyList(), duration, backColor);
    }

    public ArmorMessage(ITextComponent title, List<ITextComponent> message, int duration, int backColor) {
        lifeSpan = duration;
        stat = new WidgetAnimatedStat(null, title, WidgetAnimatedStat.StatIcon.NONE, backColor, null, ArmorHUDLayout.INSTANCE.messageStat);
        stat.setMinimumContractedDimensions(0, 0);
        stat.setText(message);
        PlayerEntity player = ClientUtils.getClientPlayer();
        player.world.playSound(player.getPosX(), player.getPosY(), player.getPosZ(), ModSounds.SCI_FI.get(), SoundCategory.PLAYERS, 0.1F, 1.0F, true);
    }

    void setDependingMessage(WidgetAnimatedStat dependingStat) {
        stat.setParentStat(dependingStat);
        stat.setBaseY(2);
    }

    public WidgetAnimatedStat getStat() {
        return stat;
    }

    void renderMessage(MatrixStack matrixStack, float partialTicks) {
        if (lifeSpan > 10) {
            stat.openStat();
        } else {
            stat.closeStat();
        }
        stat.render(matrixStack, -1, -1, partialTicks);
    }
}
