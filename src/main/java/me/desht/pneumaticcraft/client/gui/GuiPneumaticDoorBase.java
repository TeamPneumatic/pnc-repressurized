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

package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.inventory.ContainerPneumaticDoorBase;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDoorBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiPneumaticDoorBase extends GuiPneumaticContainerBase<ContainerPneumaticDoorBase,TileEntityPneumaticDoorBase> {
    WidgetAnimatedStat passRedstoneTab;

    public GuiPneumaticDoorBase(ContainerPneumaticDoorBase container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        passRedstoneTab = addAnimatedStat(xlate("pneumaticcraft.gui.tab.pneumaticDoorBaseRedstone.title"),
                new ItemStack(ModBlocks.PNEUMATIC_DOOR.get()), 0xFFFFAA00, false);
        WidgetCheckBox cb;
        passRedstoneTab.addSubWidget(cb = new WidgetCheckBox(5, 20, 0x404040, xlate("pneumaticcraft.gui.tab.pneumaticDoorBaseRedstone.text"))
                .setChecked(te.shouldPassSignalToDoor())
                .setTooltip(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.pneumaticDoorBaseRedstone.tooltip"))
                .withTag("pass_signal"));
        passRedstoneTab.setMinimumExpandedDimensions(cb.getWidth(), 40);
    }

    @Override
    public void tick() {
        super.tick();

        int mode = te.getRedstoneController().getCurrentMode();
        passRedstoneTab.visible = mode == TileEntityPneumaticDoorBase.RS_MODE_WOODEN_DOOR || mode == TileEntityPneumaticDoorBase.RS_MODE_IRON_DOOR;
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_PNEUMATIC_DOOR;
    }
}
