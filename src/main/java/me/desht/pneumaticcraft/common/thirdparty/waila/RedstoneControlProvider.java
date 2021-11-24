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

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerDataProvider;
import me.desht.pneumaticcraft.common.tileentity.IRedstoneControl;
import me.desht.pneumaticcraft.common.tileentity.RedstoneController;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedstoneControlProvider {
    public static class Data implements IServerDataProvider<TileEntity> {
        @Override
        public void appendServerData(CompoundNBT compoundNBT, ServerPlayerEntity serverPlayerEntity, World world, TileEntity te) {
            if (te instanceof IRedstoneControl) {
                compoundNBT.putInt("redstoneMode", ((IRedstoneControl) te).getRedstoneMode());
            }
        }
    }

    public static class Component implements IComponentProvider {
        @Override
        public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
            CompoundNBT tag = accessor.getServerData();
            // This is used so that we can split values later easier and have them all in the same layout.
            Map<ITextComponent, ITextComponent> values = new HashMap<>();

            if (tag.contains("redstoneMode")) {
                TileEntity te = accessor.getTileEntity();
                if (te instanceof IRedstoneControl) {
                    RedstoneController<?> rsController = ((IRedstoneControl<?>) te).getRedstoneController();
                    tooltip.add(rsController.getDescription());
                }
            }
        }
    }
}
