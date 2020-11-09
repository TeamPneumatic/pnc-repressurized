package me.desht.pneumaticcraft.client.render.pneumatic_armor.block_tracker;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IBlockTrackEntry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.InventoryTrackEvent;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketDescriptionPacketRequest;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.ChestType;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class BlockTrackEntryInventory implements IBlockTrackEntry {
    @Override
    public boolean shouldTrackWithThisEntry(IBlockReader world, BlockPos pos, BlockState state, TileEntity te) {
        if (te instanceof ChestTileEntity && state.hasProperty(ChestBlock.TYPE) && state.get(ChestBlock.TYPE) == ChestType.RIGHT) {
            // we'll only track the left side of double chest directly
            return false;
        }

        return te != null
                && !TrackerBlacklistManager.isInventoryBlacklisted(te)
                && IBlockTrackEntry.hasCapabilityOnAnyFace(te, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                && !MinecraftForge.EVENT_BUS.post(new InventoryTrackEvent(te));
    }

    @Override
    public List<BlockPos> getServerUpdatePositions(TileEntity te) {
        List<BlockPos> res = new ArrayList<>();
        if (te instanceof ChestTileEntity && te.getBlockState().get(ChestBlock.TYPE) == ChestType.LEFT) {
            Direction dir = ChestBlock.getDirectionToAttached(te.getBlockState());
            res.add(te.getPos().offset(dir));
            NetworkHandler.sendToServer(new PacketDescriptionPacketRequest(te.getPos().offset(dir)));
        }
        res.add(te.getPos());
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
                    infoList.add(new StringTextComponent("Contents: Empty"));
                } else {
                    infoList.add(new StringTextComponent("Contents:"));
                    List<ITextComponent> l = new ArrayList<>();
                    PneumaticCraftUtils.sortCombineItemStacksAndToString(l, inventoryStacks);
                    infoList.addAll(l);
                }
            });
        } catch (Throwable e) {
            TrackerBlacklistManager.addInventoryTEToBlacklist(te, e);
        }
    }

    @Override
    public ResourceLocation getEntryID() {
        return RL("block_tracker.module.inventories");
    }
}
