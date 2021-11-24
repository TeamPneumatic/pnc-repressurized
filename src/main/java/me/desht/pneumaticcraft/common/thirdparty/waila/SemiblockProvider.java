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
import me.desht.pneumaticcraft.api.semiblock.IDirectionalSemiblock;
import me.desht.pneumaticcraft.api.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.entity.semiblock.EntitySemiblockBase;
import me.desht.pneumaticcraft.common.semiblock.SemiblockTracker;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class SemiblockProvider {

    public static class Data implements IServerDataProvider<TileEntity> {
        @Override
        public void appendServerData(CompoundNBT compoundNBT, ServerPlayerEntity serverPlayerEntity, World world, TileEntity tileEntity) {
            CompoundNBT tag = new CompoundNBT();
            SemiblockTracker.getInstance().getAllSemiblocks(world, tileEntity.getBlockPos())
                    .forEach((semiBlock) -> {
                        NonNullList<ItemStack> drops = semiBlock.getDrops();
                        if (!drops.isEmpty()) {
                            tag.put(Integer.toString(semiBlock.getTrackingId()), semiBlock.serializeNBT(new CompoundNBT()));
                        }
                    });
            compoundNBT.put("semiBlocks", tag);
        }
    }

    public static class Component implements IComponentProvider {
        @Override
        public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
            CompoundNBT tag = accessor.getServerData().getCompound("semiBlocks");

            for (String name : tag.getAllKeys()) {
                try {
                    int entityId = Integer.parseInt(name);
                    ISemiBlock entity = ISemiBlock.byTrackingId(accessor.getWorld(), entityId);
                    if (entity instanceof EntitySemiblockBase) {
                        if (!(entity instanceof IDirectionalSemiblock) || ((IDirectionalSemiblock) entity).getSide() == accessor.getSide()) {
                            ITextComponent title = new StringTextComponent(TextFormatting.YELLOW + "[")
                                    .append(entity.getDisplayName()).append("]");
                            tooltip.add(title);
                            entity.addTooltip(tooltip, accessor.getPlayer(), tag.getCompound(name), accessor.getPlayer().isShiftKeyDown());
                        }
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }
}
