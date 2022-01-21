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

package me.desht.pneumaticcraft.client.render.pneumatic_armor.block_tracker;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IBlockTrackEntry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.InventoryTrackEvent;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class BlockTrackEntryInventory implements IBlockTrackEntry {
    private static final ResourceLocation ID = RL("block_tracker.module.inventories");

    @Override
    public boolean shouldTrackWithThisEntry(BlockGetter world, BlockPos pos, BlockState state, BlockEntity te) {
        if (te instanceof ChestBlockEntity && state.hasProperty(ChestBlock.TYPE) && state.getValue(ChestBlock.TYPE) == ChestType.RIGHT) {
            // we'll only track the left side of double chest directly
            return false;
        }

        return te != null
                && !TrackerBlacklistManager.isInventoryBlacklisted(te)
                && IBlockTrackEntry.hasCapabilityOnAnyFace(te, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                && !MinecraftForge.EVENT_BUS.post(new InventoryTrackEvent(te));
    }

    @Override
    public List<BlockPos> getServerUpdatePositions(BlockEntity te) {
        List<BlockPos> res = new ArrayList<>();
        if (te instanceof ChestBlockEntity && te.getBlockState().getValue(ChestBlock.TYPE) == ChestType.LEFT) {
            Direction dir = ChestBlock.getConnectedDirection(te.getBlockState());
            res.add(te.getBlockPos().relative(dir));
        }
        res.add(te.getBlockPos());
        return res;
    }

    @Override
    public int spamThreshold() {
        return 16;
    }

    @Override
    public void addInformation(Level world, BlockPos pos, BlockEntity te, Direction face, List<Component> infoList) {
        try {
            IOHelper.getInventoryForTE(te, face).ifPresent(inventory -> {
                boolean empty = true;
                ItemStack[] inventoryStacks = new ItemStack[inventory.getSlots()];
                for (int i = 0; i < inventory.getSlots(); i++) {
                    ItemStack iStack = inventory.getStackInSlot(i);
                    if (!iStack.isEmpty()) {
                        empty = false;
                    }
                    inventoryStacks[i] = iStack;
                }
                if (empty) {
                    infoList.add(new TextComponent("Contents: Empty"));
                } else {
                    infoList.add(new TextComponent("Contents:"));
                    List<Component> l = new ArrayList<>();
                    PneumaticCraftUtils.summariseItemStacks(l, inventoryStacks);
                    infoList.addAll(l);
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
}
