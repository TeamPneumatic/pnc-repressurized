package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerOmnidirectionalHopper;
import me.desht.pneumaticcraft.common.inventory.handler.ComparatorItemStackHandler;
import me.desht.pneumaticcraft.common.network.GuiSynced;
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
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityOmnidirectionalHopper extends TileEntityAbstractHopper {
    public static final int INVENTORY_SIZE = 5;

    private final ComparatorItemStackHandler itemHandler = new ComparatorItemStackHandler(this, getInvSize());
    private final LazyOptional<IItemHandler> invCap = LazyOptional.of(() -> itemHandler);
    @GuiSynced
    public boolean roundRobin;
    private int rrSlot;

    public TileEntityOmnidirectionalHopper() {
        super(ModTileEntities.OMNIDIRECTIONAL_HOPPER.get());
    }

    protected int getInvSize() {
        return INVENTORY_SIZE;
    }

    @Override
    protected LazyOptional<IItemHandler> getInventoryCap() {
        return invCap;
    }

    protected int getComparatorValueInternal() {
        return itemHandler.getComparatorValue();
    }

    protected boolean doExport(final int maxItems) {
        Direction outputDir = getRotation();

        // TODO cache the capability rather than the TE?
        LazyOptional<IItemHandler> inv = IOHelper.getInventoryForTE(getCachedNeighbor(outputDir), outputDir.getOpposite());
        int notExported;
        if (inv.isPresent()) {
            notExported = inv.map(h -> exportToInventory(h, maxItems)).orElse(maxItems);
        } else if (PNCConfig.Common.Machines.omniHopperDispenser && getUpgrades(EnumUpgrade.DISPENSER) > 0) {
            notExported = exportToInventory(new DropInWorldHandler(getWorld(), getPos(), outputDir), maxItems);
        } else {
            notExported = maxItems;
        }
        return notExported < maxItems;
    }

    private int exportToInventory(IItemHandler otherHandler, int maxItems) {
        int remaining = maxItems;
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stack = itemHandler.getStackInSlot(actualSlot(i));
            if (stack.getCount() > leaveMaterialCount) {
                ItemStack exportedStack = ItemHandlerHelper.copyStackWithSize(stack, Math.min(stack.getCount() - leaveMaterialCount, remaining));
                int toExport = exportedStack.getCount();
                ItemStack excess = ItemHandlerHelper.insertItem(otherHandler, exportedStack, false);
                int exportedCount = toExport - excess.getCount();
                if (!isCreative) {
                    stack.shrink(exportedCount);
                    if (exportedCount > 0) itemHandler.invalidateComparatorValue();
                }
                remaining -= exportedCount;
                if (remaining <= leaveMaterialCount) {
                    if (roundRobin) {
                        rrSlot = actualSlot(i) + 1;
                        if (rrSlot >= itemHandler.getSlots()) rrSlot = 0;
                    }
                    break;
                }
            }
        }
        return remaining;
    }

    private int actualSlot(int i) {
        if (roundRobin) {
            int slot = rrSlot + i;
            if (slot >= itemHandler.getSlots()) slot -= itemHandler.getSlots();
            return slot;
        } else {
            return i;
        }
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

        BlockPos inputPos = pos.offset(inputDir);
        if (!Block.hasSolidSide(world.getBlockState(inputPos), world, inputPos, inputDir.getOpposite())) {
            // Suck in item entities
            for (ItemEntity entity : getNeighborItems()) {
                ItemStack remainder = ItemHandlerHelper.insertItem(itemHandler, entity.getItem(), false); //IOHelper.insert(this, entity.getItem(), null, false);
                if (remainder.isEmpty()) {
                    entity.remove();
                    success = true;
                } else if (remainder.getCount() < entity.getItem().getCount()) {
                    // some but not all were inserted
                    entity.setItem(remainder);
                    success = true;
                }
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
        tag.putBoolean("RoundRobin", roundRobin);
        if (roundRobin) tag.putInt("RRSlot", rrSlot);
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        itemHandler.deserializeNBT(tag.getCompound("Items"));
        roundRobin = tag.getBoolean("RoundRobin");
        rrSlot = tag.getInt("RRSlot");
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return itemHandler;
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerOmnidirectionalHopper(i, playerInventory, getPos());
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, PlayerEntity player) {
        if (tag.equals("rr")) {
            roundRobin = !roundRobin;
            markDirty();
        } else {
            super.handleGUIButtonPress(tag, shiftHeld, player);
        }
    }

    private static class DropInWorldHandler implements IItemHandler {
        private final World world;
        private final BlockPos pos;
        private final Direction outputDir;

        public DropInWorldHandler(World world, BlockPos pos, Direction outputDir) {
            this.world = world;
            this.pos = pos.offset(outputDir);
            this.outputDir = outputDir;
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (!Block.hasSolidSide(world.getBlockState(pos), world, pos, outputDir.getOpposite())) {
                if (!simulate) {
                    PneumaticCraftUtils.dropItemOnGroundPrecisely(stack, world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                }
                return ItemStack.EMPTY;
            } else {
                return stack;
            }
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return true;
        }
    }
}
