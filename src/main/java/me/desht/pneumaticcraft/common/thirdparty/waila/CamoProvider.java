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

package me.desht.pneumaticcraft.common.thirdparty.waila;

import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import me.desht.pneumaticcraft.common.item.ItemCamoApplicator;
import me.desht.pneumaticcraft.common.tileentity.CamouflageableBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class CamoProvider {
    public static class Component implements IComponentProvider {
        @Override
        public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
            BlockEntity te = blockAccessor.getBlockEntity();
            if (te instanceof CamouflageableBlockEntity camo && camo.getCamouflage() != null) {
                net.minecraft.network.chat.Component str = ItemCamoApplicator.getCamoStateDisplayName(((CamouflageableBlockEntity) te).getCamouflage());
                iTooltip.add(xlate("pneumaticcraft.waila.camo", str));
            }
        }
    }
}
