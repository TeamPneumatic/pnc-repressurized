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

import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.StatPanelLayout;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler.CoreComponentsClientHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.config.subconfig.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.CoreComponentsHandler;
import me.desht.pneumaticcraft.common.registry.ModSounds;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

import java.util.Collections;
import java.util.List;

public class ArmorMessage {
    private final IGuiAnimatedStat stat;
    private int lifeSpan;

    public ArmorMessage(Component title, int duration, int backColor) {
        this(title, Collections.emptyList(), duration, backColor);
    }

    public ArmorMessage(Component title, List<Component> message, int duration, int backColor) {
        lifeSpan = duration;
        StatPanelLayout layout = ArmorHUDLayout.INSTANCE.getLayoutFor(CoreComponentsHandler.getMessageID(), CoreComponentsClientHandler.getDefaultMessageLayout());
        stat = new WidgetAnimatedStat(null, title, WidgetAnimatedStat.StatIcon.NONE, backColor, null, layout);
        stat.setMinimumContractedDimensions(0, 0);
        stat.setText(message);
        Player player = ClientUtils.getClientPlayer();
        player.level().playLocalSound(player.getX(), player.getY(), player.getZ(), ModSounds.SCI_FI.get(), SoundSource.PLAYERS, 0.1F, 1.0F, true);
    }

    void setDependingMessage(IGuiAnimatedStat dependingStat) {
        stat.setParentStat(dependingStat);
        stat.setBaseY(2);
    }

    public IGuiAnimatedStat getStat() {
        return stat;
    }

    void renderMessage(GuiGraphics graphics, float partialTicks) {
        if (lifeSpan > 10) {
            stat.openStat();
        } else {
            stat.closeStat();
        }
        stat.renderStat(graphics, -1, -1, partialTicks);
    }

    public void tick() {
        stat.tickWidget();
        --lifeSpan;
    }

    public boolean isExpired() {
        return lifeSpan <= 0;
    }
}
