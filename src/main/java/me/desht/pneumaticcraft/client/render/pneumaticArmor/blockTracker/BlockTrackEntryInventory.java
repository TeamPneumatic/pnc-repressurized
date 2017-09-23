package me.desht.pneumaticcraft.client.render.pneumaticArmor.blockTracker;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IBlockTrackEntry;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.InventoryTrackEvent;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.HUDHandler;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketDescriptionPacketRequest;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlockTrackEntryInventory implements IBlockTrackEntry {
    private static final Set<ResourceLocation> invBlackList = new HashSet<>();

    public static void addTileEntityToBlackList(TileEntity te, Throwable e) {
        e.printStackTrace();
        String title = te.getWorld().getBlockState(te.getPos()).getBlock().getLocalizedName();
        HUDHandler.instance().addMessage(
                "Block tracking failed for " + title + "! A stacktrace can be found in the log.",
                new ArrayList<>(), 60, 0xFFFF0000);
        invBlackList.add(TileEntity.getKey(te.getClass()));
    }

    @Override
    public boolean shouldTrackWithThisEntry(IBlockAccess world, BlockPos pos, IBlockState state, TileEntity te) {
        if (te instanceof TileEntityChest) {
            TileEntityChest chest = (TileEntityChest) te;
            if (chest.adjacentChestXNeg != null || chest.adjacentChestZNeg != null) return false;
        }

        return te != null
                && !invBlackList.contains(TileEntity.getKey(te.getClass()))
                && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)
                && !MinecraftForge.EVENT_BUS.post(new InventoryTrackEvent(te));
    }

    @Override
    public boolean shouldBeUpdatedFromServer(TileEntity te) {
        if (te instanceof TileEntityChest) {
            TileEntityChest chest = (TileEntityChest) te;
            if (chest.adjacentChestXPos != null)
                NetworkHandler.sendToServer(new PacketDescriptionPacketRequest(chest.adjacentChestXPos.getPos()));
            if (chest.adjacentChestZPos != null)
                NetworkHandler.sendToServer(new PacketDescriptionPacketRequest(chest.adjacentChestZPos.getPos()));
        }
        return true;
    }

    @Override
    public int spamThreshold() {
        return 6;
    }

    @Override
    public void addInformation(World world, BlockPos pos, TileEntity te, List<String> infoList) {
        try {
            IItemHandler inventory = IOHelper.getInventoryForTE(te);
            if (inventory != null) {
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
                    infoList.add("Contents: Empty");
                } else {
                    infoList.add("Contents:");
                    PneumaticCraftUtils.sortCombineItemStacksAndToString(infoList, inventoryStacks);
                }
            }
        } catch (Throwable e) {
            addTileEntityToBlackList(te, e);
        }
    }

    @Override
    public String getEntryName() {
        return "blockTracker.module.inventories";
    }
}
