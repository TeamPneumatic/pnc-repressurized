package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.common.config.Config;
import me.desht.pneumaticcraft.common.core.ModTileEntityTypes;
import me.desht.pneumaticcraft.common.inventory.ContainerOmnidirectionalHopper;
import me.desht.pneumaticcraft.common.inventory.handler.ComparatorItemStackHandler;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.List;

public class TileEntityOmnidirectionalHopper extends TileEntityTickableBase implements IRedstoneControlled, IComparatorSupport, INamedContainerProvider {
    public static final int INVENTORY_SIZE = 5;
    @DescSynced
    Direction inputDir = Direction.UP;
    @DescSynced
    private Direction outputDir = Direction.UP;
    private final ComparatorItemStackHandler itemHandler = new ComparatorItemStackHandler(this, getInvSize());
    private final LazyOptional<IItemHandlerModifiable> invCap = LazyOptional.of(() -> itemHandler);
    private int lastComparatorValue = -1;
    @GuiSynced
    public int redstoneMode;
    private int cooldown;
    @GuiSynced
    int leaveMaterialCount; // leave items/liquids (used as filter)
    @DescSynced
    public boolean isCreative; // has a creative upgrade installed

    public TileEntityOmnidirectionalHopper() {
        super(ModTileEntityTypes.OMNIDIRECTIONAL_HOPPER, 4);
        addApplicableUpgrade(EnumUpgrade.SPEED);
        addApplicableUpgrade(EnumUpgrade.CREATIVE);
        if (Config.Common.Machines.omniHopperDispenser) addApplicableUpgrade(EnumUpgrade.DISPENSER);
    }

    protected int getInvSize() {
        return INVENTORY_SIZE;
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
    }

    @Override
    protected void onFirstServerUpdate() {
        super.onFirstServerUpdate();

        isCreative = getUpgrades(EnumUpgrade.CREATIVE) > 0;
    }

    @Override
    public LazyOptional<IItemHandlerModifiable> getInventoryCap() {
        return invCap;
    }

    @Override
    public void tick() {
        super.tick();

        if (!getWorld().isRemote && --cooldown <= 0 && redstoneAllows()) {
            int maxItems = getMaxItems();
            boolean success = doImport(maxItems);
            success |= doExport(maxItems);

            // If we couldn't pull or push, slow down a bit for performance reasons
            cooldown = success ? getItemTransferInterval() : 8;

            if (lastComparatorValue != getComparatorValueInternal()) {
                lastComparatorValue = getComparatorValueInternal();
                updateNeighbours();
            }
        }
    }

    protected int getComparatorValueInternal() {
        return itemHandler.getComparatorValue();
    }

    protected boolean doExport(final int maxItems) {
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
        } else if (Config.Common.Machines.omniHopperDispenser && getUpgrades(EnumUpgrade.DISPENSER) > 0) {
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

    static List<ItemEntity> getNeighborItems(TileEntity te, Direction dir) {
        AxisAlignedBB box = new AxisAlignedBB(te.getPos().offset(dir));
        return te.getWorld().getEntitiesWithinAABB(ItemEntity.class, box, EntityPredicates.IS_ALIVE);
    }

    public int getMaxItems() {
        int upgrades = getUpgrades(EnumUpgrade.SPEED);
        if (upgrades > 3) {
            return Math.min(1 << (upgrades - 3), 256);
        } else {
            return 1;
        }
    }

    public int getItemTransferInterval() {
        return 8 / (1 << getUpgrades(EnumUpgrade.SPEED));
    }

    public void setInputDirection(Direction dir) {
        inputDir = dir;
    }

    public Direction getInputDirection() {
        return inputDir;
    }

    @Override
    public Direction getRotation() {
        return outputDir;
    }

    public void setRotation(Direction rotation) {
        outputDir = rotation;
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        tag.putInt("inputDir", inputDir.ordinal());
        tag.putInt("outputDir", outputDir.ordinal());
        tag.putInt("redstoneMode", redstoneMode);
        tag.putInt("leaveMaterialCount", leaveMaterialCount);
        tag.put("Items", itemHandler.serializeNBT());
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        inputDir = Direction.byIndex(tag.getInt("inputDir"));
        outputDir = Direction.byIndex(tag.getInt("outputDir"));
        redstoneMode = tag.getInt("redstoneMode");
        if (tag.contains("leaveMaterial")) {
            leaveMaterialCount = (byte)(tag.getBoolean("leaveMaterial") ? 1 : 0);
        } else {
            leaveMaterialCount = tag.getInt("leaveMaterialCount");
        }
        itemHandler.deserializeNBT(tag.getCompound("Items"));
    }

    @Override
    public void handleGUIButtonPress(String tag, PlayerEntity player) {
        switch (tag) {
            case IGUIButtonSensitive.REDSTONE_TAG:
                redstoneMode++;
                if (redstoneMode > 2) redstoneMode = 0;
                break;
            case "empty":
                leaveMaterialCount = 0;
                break;
            case "leave":
                leaveMaterialCount = 1;
                break;
        }
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return itemHandler;
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
    }

    public boolean doesLeaveMaterial() {
        return leaveMaterialCount > 0;
    }

    @Override
    public int getComparatorValue() {
        return getComparatorValueInternal();
    }

    @Override
    protected void onUpgradesChanged() {
        super.onUpgradesChanged();

        if (world != null && !world.isRemote) {
            isCreative = getUpgrades(EnumUpgrade.CREATIVE) > 0;
        }
    }

    @Override
    public boolean shouldPreserveStateOnBreak() {
        // always preserve state, since we can't sneak-wrench this machine (sneak-wrench rotates output)
        return true;
    }

    @Override
    public ITextComponent getDisplayName() {
        return getDisplayNameInternal();
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerOmnidirectionalHopper(i, playerInventory, getPos());
    }
}
