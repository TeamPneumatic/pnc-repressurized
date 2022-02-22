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
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.ChestType;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.EnderChestTileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class BlockTrackEntryInventory implements IBlockTrackEntry {
    private static final ResourceLocation ID = RL("block_tracker.module.inventories");

    @Override
    public boolean shouldTrackWithThisEntry(IBlockReader world, BlockPos pos, BlockState state, TileEntity te) {
        if (te instanceof ChestTileEntity && state.hasProperty(ChestBlock.TYPE) && state.getValue(ChestBlock.TYPE) == ChestType.RIGHT) {
            // we'll only track the left side of double chest directly
            return false;
        }

        return te != null
                && !TrackerBlacklistManager.isInventoryBlacklisted(te)
                && (te instanceof LockableLootTileEntity || te instanceof EnderChestTileEntity || IBlockTrackEntry.hasCapabilityOnAnyFace(te, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY))
                && !MinecraftForge.EVENT_BUS.post(new InventoryTrackEvent(te));
    }

    @Override
    public List<BlockPos> getServerUpdatePositions(TileEntity te) {
        if (te instanceof LockableLootTileEntity && !IBlockTrackEntry.hasCapabilityOnAnyFace(te, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)) {
            // lootr chests can be like this
            return Collections.emptyList();
        }
        List<BlockPos> res = new ArrayList<>();
        if (te instanceof ChestTileEntity && te.getBlockState().getValue(ChestBlock.TYPE) == ChestType.LEFT) {
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
    public void addInformation(World world, BlockPos pos, TileEntity te, Direction face, List<ITextComponent> infoList) {
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
                    infoList.add(xlate("pneumaticcraft.gui.misc.empty").withStyle(TextFormatting.ITALIC));
                } else {
                    List<ITextComponent> l = new ArrayList<>();
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
