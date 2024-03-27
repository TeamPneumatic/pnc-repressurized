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

package me.desht.pneumaticcraft.client.pneumatic_armor.block_tracker;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IBlockTrackEntry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.InventoryTrackEvent;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.mixin.accessors.BaseContainerBlockEntityAccess;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.LockCode;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class BlockTrackEntryInventory implements IBlockTrackEntry {
    public static final ResourceLocation ID = RL("block_tracker.module.inventories");

    @Override
    public boolean shouldTrackWithThisEntry(BlockGetter world, BlockPos pos, BlockState state, BlockEntity te) {
        if (te instanceof ChestBlockEntity && state.hasProperty(ChestBlock.TYPE) && state.getValue(ChestBlock.TYPE) == ChestType.RIGHT) {
            // we'll only track the left side of double chest directly
            return false;
        }

        return te != null
                && !TrackerBlacklistManager.isInventoryBlacklisted(te)
                && (te instanceof RandomizableContainerBlockEntity || IBlockTrackEntry.hasCapabilityOnAnyFace(te, Capabilities.ItemHandler.BLOCK))
                && postTrackEvent(te);
    }

    private boolean postTrackEvent(BlockEntity te) {
        InventoryTrackEvent event = NeoForge.EVENT_BUS.post(new InventoryTrackEvent(te));
        return !event.isCanceled();
    }

    @Override
    public List<BlockPos> getServerUpdatePositions(BlockEntity te) {
        if (te instanceof RandomizableContainerBlockEntity && !IBlockTrackEntry.hasCapabilityOnAnyFace(te, Capabilities.ItemHandler.BLOCK)) {
            // lootr chests can be like this
            return Collections.emptyList();
        }
        ImmutableList.Builder<BlockPos> builder = ImmutableList.builder();
        if (te instanceof ChestBlockEntity && te.getBlockState().getValue(ChestBlock.TYPE) == ChestType.LEFT) {
            Direction dir = ChestBlock.getConnectedDirection(te.getBlockState());
            builder.add(te.getBlockPos().relative(dir));
        }
        builder.add(te.getBlockPos());
        return builder.build();
    }

    @Override
    public int spamThreshold() {
        return 16;
    }

    @Override
    public void addInformation(Level world, BlockPos pos, BlockEntity te, Direction face, List<Component> infoList) {
        if (!canUnlock(te)) {
            infoList.add(xlate("pneumaticcraft.gui.misc.locked").withStyle(ChatFormatting.ITALIC));
            return;
        }
        try {
            IOHelper.getInventoryForBlock(te, face).ifPresent(inventory -> {
                List<ItemStack> inventoryStacks = new ArrayList<>(inventory.getSlots());
                for (int i = 0; i < inventory.getSlots(); i++) {
                    ItemStack iStack = inventory.getStackInSlot(i);
                    if (!iStack.isEmpty()) {
                        inventoryStacks.add(iStack);
                    }
                }
                if (inventoryStacks.isEmpty()) {
                    infoList.add(xlate("pneumaticcraft.gui.misc.empty").withStyle(ChatFormatting.ITALIC));
                } else {
                    PneumaticCraftUtils.summariseItemStacks(infoList, inventoryStacks);
                }
            });
        } catch (Throwable e) {
            TrackerBlacklistManager.addInventoryTEToBlacklist(te, e);
        }
    }

    @Override
    public ResourceLocation getEntryID() {
        return ID;
    }

    private static boolean canUnlock(BlockEntity be) {
        // respect vanilla chest locking
        if (be instanceof BaseContainerBlockEntity base) {
            LockCode key = ((BaseContainerBlockEntityAccess) base).getLockKey();
            return key.unlocksWith(ClientUtils.getClientPlayer().getMainHandItem());
        }
        return true;
    }
}
