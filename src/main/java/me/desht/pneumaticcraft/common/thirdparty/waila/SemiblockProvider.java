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
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import me.desht.pneumaticcraft.api.semiblock.IDirectionalSemiblock;
import me.desht.pneumaticcraft.api.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.entity.semiblock.AbstractSemiblockEntity;
import me.desht.pneumaticcraft.common.semiblock.SemiblockTracker;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SemiblockProvider {

    public static class Data implements IServerDataProvider<BlockEntity> {
        @Override
        public void appendServerData(CompoundTag compoundTag, ServerPlayer serverPlayer, Level level, BlockEntity blockEntity, boolean b) {
            CompoundTag tag = new CompoundTag();
            SemiblockTracker.getInstance().getAllSemiblocks(level, blockEntity.getBlockPos())
                    .forEach((semiBlock) -> {
                        NonNullList<ItemStack> drops = semiBlock.getDrops();
                        if (!drops.isEmpty()) {
                            tag.put(Integer.toString(semiBlock.getTrackingId()), semiBlock.serializeNBT(new CompoundTag()));
                        }
                    });
            compoundTag.put("semiBlocks", tag);
        }
    }

    public static class Component implements IComponentProvider {
        @Override
        public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
            CompoundTag tag = blockAccessor.getServerData().getCompound("semiBlocks");

            for (String name : tag.getAllKeys()) {
                try {
                    int entityId = Integer.parseInt(name);
                    ISemiBlock entity = ISemiBlock.byTrackingId(blockAccessor.getLevel(), entityId);
                    if (entity instanceof AbstractSemiblockEntity) {
                        if (!(entity instanceof IDirectionalSemiblock) || ((IDirectionalSemiblock) entity).getSide() == blockAccessor.getSide()) {
                            MutableComponent title = new TextComponent("[")
                                    .append(entity.getDisplayName()).append("]").withStyle(ChatFormatting.YELLOW);
                            iTooltip.add(title);
                            entity.addTooltip(iTooltip::add, blockAccessor.getPlayer(), tag.getCompound(name), blockAccessor.getPlayer().isShiftKeyDown());
                        }
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }
}
