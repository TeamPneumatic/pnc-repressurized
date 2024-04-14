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

import me.desht.pneumaticcraft.common.block.entity.utility.ReinforcedChestBlockEntity;
import me.desht.pneumaticcraft.common.inventory.ReinforcedChestMenu;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ReinforcedChestScreen extends AbstractPneumaticCraftContainerScreen<ReinforcedChestMenu, ReinforcedChestBlockEntity> {
    public ReinforcedChestScreen(ReinforcedChestMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);

        imageHeight = 186;
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_REINFORCED_CHEST;
    }

    @Override
    protected boolean shouldAddProblemTab() {
        return false;
    }
}
