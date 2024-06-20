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

package me.desht.pneumaticcraft.client.gui.pneumatic_armor.options;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import me.desht.pneumaticcraft.client.gui.widget.PNCForgeSlider;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import me.desht.pneumaticcraft.common.network.PacketUpdateArmorExtraData;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

public abstract class AbstractSliderOptions<T extends IArmorUpgradeClientHandler<?>> extends IOptionPage.SimpleOptionPage<T> {
    private Integer pendingVal = null;

    AbstractSliderOptions(IGuiScreen screen, T handler) {
        super(screen, handler);
    }

    protected PointXY getSliderPos() {
        return new PointXY(30, 60);
    }

    protected Pair<Integer, Integer> getRange() {
        return Pair.of(0, 100);
    }

    /**
     * The data component in which this slider value will be saved. The item it's saved to is determined
     * by {@link #getSlot()}.
     * @return the data component type
     */
    protected abstract DataComponentType<Integer> getIntegerComponent();

    protected abstract Component getPrefix();

    protected abstract Component getSuffix();

    EquipmentSlot getSlot() {
        return getClientUpgradeHandler().getCommonHandler().getEquipmentSlot();
    }

    @Override
    public void populateGui(IGuiScreen gui) {
        Pair<Integer,Integer> range = getRange();
        int initVal = range.getRight();
        if (Minecraft.getInstance().player != null) {
            ItemStack stack = Minecraft.getInstance().player.getItemBySlot(getSlot());
            initVal = PneumaticArmorItem.getIntData(stack, getIntegerComponent(), range.getRight());
        }
        PointXY pos = getSliderPos();
        gui.addWidget(new PNCForgeSlider(pos.x(), pos.y(), 150, 20, getPrefix(), getSuffix(),
                range.getLeft(), range.getRight(), initVal, true, slider -> pendingVal = slider.getValueInt()));
    }

    @Override
    public void tick() {
        if (pendingVal != null && !getGuiScreen().getScreen().isDragging()) {
            // avoid sending a stream of update packets if player is dragging slider
            IArmorUpgradeHandler<?> upgradeHandler = getClientUpgradeHandler().getCommonHandler();
            PacketUpdateArmorExtraData.sendToServer(upgradeHandler, getIntegerComponent(), pendingVal);
            // also update the clientside handler
            upgradeHandler.onDataFieldUpdated(CommonArmorHandler.getHandlerForPlayer(), getIntegerComponent(), pendingVal);
            pendingVal = null;
        }
    }
}
