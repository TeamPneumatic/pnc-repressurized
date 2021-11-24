/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
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
    private final IGuiAnimatedStat stat;
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
        player.level.playLocalSound(player.getX(), player.getY(), player.getZ(), ModSounds.SCI_FI.get(), SoundCategory.PLAYERS, 0.1F, 1.0F, true);
    }

    void setDependingMessage(IGuiAnimatedStat dependingStat) {
        stat.setParentStat(dependingStat);
        stat.setBaseY(2);
    }

    public IGuiAnimatedStat getStat() {
        return stat;
    }

    void renderMessage(MatrixStack matrixStack, float partialTicks) {
        if (lifeSpan > 10) {
            stat.openStat();
        } else {
            stat.closeStat();
        }
        stat.renderStat(matrixStack, -1, -1, partialTicks);
    }
}
