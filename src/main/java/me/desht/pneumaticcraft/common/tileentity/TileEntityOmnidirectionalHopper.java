package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModTileEntityTypes;
import me.desht.pneumaticcraft.common.inventory.ContainerOmnidirectionalHopper;
import me.desht.pneumaticcraft.common.inventory.handler.ComparatorItemStackHandler;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;

public class TileEntityOmnidirectionalHopper extends TileEntityAbstractHopper {
    public static final int INVENTORY_SIZE = 5;

    private final ComparatorItemStackHandler itemHandler = new ComparatorItemStackHandler(this, getInvSize());
    private final LazyOptional<IItemHandlerModifiable> invCap = LazyOptional.of(() -> itemHandler);

    public TileEntityOmnidirectionalHopper() {
        super(ModTileEntityTypes.OMNIDIRECTIONAL_HOPPER);
    }

    protected int getInvSize() {
        return INVENTORY_SIZE;
    }

    @Override
    protected LazyOptional<IItemHandlerModifiable> getInventoryCap() {
        return invCap;
    }

    protected int getComparatorValueInternal() {
        return itemHandler.getComparatorValue();
    }

    protected boolean doExport(final int maxItems) {
        Direction outputDir = getRotation();

        // TODO cache the capability
        LazyOptional<IItemHandler> cap = IOHelper.getInventoryForTE(getCachedNeighbor(outputDir), outputDir.getOpposite());
        if (cap.isPresent()) {
            return cap.map(otherHandler -> {
                int max = maxItems;
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    ItemStack stack = itemHandler.getStackInSlot(i);
                    if (stack.getCount() > leaveMaterialCount) {
                        ItemStack exportedStack = ItemHandlerHelper.copyStackWithSize(stack, Math.min(maxItems, stack.getCount() - leaveMaterialCount));
                        int toExport = exportedStack.getCount();
                        ItemStack excess = ItemHandlerHelper.insertItem(otherHandler, exportedStack, false);
                        int exportedCount = toExport - excess.getCount();
                        if (!isCreative) {
                            stack.shrink(exportedCount);
                            if (exportedCount > 0) itemHandler.invalidateComparatorValue();
                        }
                        max -= exportedCount;
                        if (max <= 0) return true;
                    }
                }
                return false;
            }).orElse(false);
        } else if (PNCConfig.Common.Machines.omniHopperDispenser && getUpgrades(EnumUpgrade.DISPENSER) > 0) {
            BlockPos pos = getPos().offset(outputDir);
            int remaining = maxItems;
            if (!Block.hasSolidSide(world.getBlockState(pos), world, pos, outputDir.getOpposite())) {
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    ItemStack inSlot = itemHandler.getStackInSlot(i);
                    ItemStack stack = itemHandler.extractItem(i, Math.min(inSlot.getCount() - leaveMaterialCount, remaining), isCreative);
                    if (!stack.isEmpty()) {
                        remaining -= stack.getCount();
                        PneumaticCraftUtils.dropItemOnGroundPrecisely(stack, getWorld(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                        if (remaining <= 0) return true;
                    }
                }
            }
            return remaining < maxItems;
        }

        return false;
    }

    protected boolean doImport(final int maxItems) {
        boolean success = false;

        if (isInventoryFull()) {
            return false;
        }

        Direction inputDir = getInputDirection();

        // Suck from input inventory
        LazyOptional<IItemHandler> cap = IOHelper.getInventoryForTE(getCachedNeighbor(inputDir), inputDir.getOpposite());
        if (cap.isPresent()) {
            return cap.map(otherHandler -> {
                int remaining = maxItems;
                for (int i = 0; i < otherHandler.getSlots(); i++) {
                    if (otherHandler.getStackInSlot(i).isEmpty()) continue;
                    ItemStack toExtract = otherHandler.extractItem(i, remaining, true);
                    ItemStack excess = ItemHandlerHelper.insertItemStacked(itemHandler, toExtract, false);
                    int transferred = toExtract.getCount() - excess.getCount();
                    if (transferred > 0) {
                        otherHandler.extractItem(i, transferred, false);
                        remaining -= transferred;
                        if (remaining <= 0) {
                            return true;
                        }
                    }
                }
                return remaining < maxItems;
            }).orElse(false);
        }

        // Suck in item entities
        for (ItemEntity entity : getNeighborItems(this, inputDir)) {
            ItemStack remainder = IOHelper.insert(this, entity.getItem(), null, false);
            if (remainder.isEmpty()) {
                entity.remove();
                success = true;
            } else if (remainder.getCount() < entity.getItem().getCount()) {
                // some but not all were inserted
                entity.setItem(remainder);
                success = true;
            }
        }

        return success;
    }

    private boolean isInventoryFull() {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (stack.isEmpty() || stack.getCount() < stack.getMaxStackSize()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getItemTransferInterval() {
        return 8 / (1 << getUpgrades(EnumUpgrade.SPEED));
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        tag.put("Items", itemHandler.serializeNBT());
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        itemHandler.deserializeNBT(tag.getCompound("Items"));
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return itemHandler;
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerOmnidirectionalHopper(i, playerInventory, getPos());
    }
}
