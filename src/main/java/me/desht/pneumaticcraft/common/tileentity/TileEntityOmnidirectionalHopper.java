package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.inventory.ComparatorItemStackHandler;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;

public class TileEntityOmnidirectionalHopper extends TileEntityTickableBase implements IRedstoneControlled, IComparatorSupport {
    public static final int INVENTORY_SIZE = 5;
    @DescSynced
    EnumFacing inputDir = EnumFacing.UP;
    @DescSynced
    private EnumFacing outputDir = EnumFacing.UP;
    private final ComparatorItemStackHandler inventory = new ComparatorItemStackHandler(this, getInvSize());
    private int lastComparatorValue = -1;
    @GuiSynced
    public int redstoneMode;
    private int cooldown;
    @GuiSynced
    int leaveMaterialCount; // leave items/liquids (used as filter)
    private int importSlot = 0;
    @DescSynced
    public boolean isCreative; // has a creative upgrade installed

    public TileEntityOmnidirectionalHopper() {
        super(4);
        addApplicableUpgrade(EnumUpgrade.SPEED);
        addApplicableUpgrade(EnumUpgrade.CREATIVE);
        if (ConfigHandler.machineProperties.omniHopperDispenser) addApplicableUpgrade(EnumUpgrade.DISPENSER);
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
    public IItemHandlerModifiable getPrimaryInventory() {
        return inventory;
    }

    @Override
    public void update() {
        super.update();

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
        return inventory.getComparatorValue();
    }

    protected boolean doExport(int maxItems) {
        IItemHandler handler = IOHelper.getInventoryForTE(getCachedNeighbor(outputDir), outputDir.getOpposite());
        if (handler != null) {
            for (int i = 0; i < inventory.getSlots(); i++) {
                ItemStack stack = inventory.getStackInSlot(i);
                if (stack.getCount() > leaveMaterialCount) {
                    ItemStack exportedStack = ItemHandlerHelper.copyStackWithSize(stack, Math.min(maxItems, stack.getCount() - leaveMaterialCount));
                    int toExport = exportedStack.getCount();
                    ItemStack excess = ItemHandlerHelper.insertItem(handler, exportedStack, false);
                    int exportedCount = toExport - excess.getCount();
                    if (!isCreative) {
                        stack.shrink(exportedCount);
                        if (exportedCount > 0) inventory.invalidateComparatorValue();
                    }
                    maxItems -= exportedCount;
                    if (maxItems <= 0) return true;
                }
            }
        } else if (ConfigHandler.machineProperties.omniHopperDispenser && getUpgrades(EnumUpgrade.DISPENSER) > 0) {
            BlockPos pos = getPos().offset(outputDir);
            int remaining = maxItems;
            if (!world.isBlockFullCube(pos)) {
                for (int i = 0; i < inventory.getSlots(); i++) {
                    ItemStack inSlot = inventory.getStackInSlot(i);
                    ItemStack stack = inventory.extractItem(i, Math.min(inSlot.getCount() - leaveMaterialCount, remaining), isCreative);
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

    protected boolean doImport(int maxItems) {
        boolean success = false;

        if (isInventoryFull()) {
            return false;
        }

        // Suck from input inventory
        IItemHandler handler = IOHelper.getInventoryForTE(getCachedNeighbor(inputDir), inputDir.getOpposite());
        if (handler != null) {
            int remaining = maxItems;
            for (int i = 0; i < handler.getSlots(); i++) {
                if (handler.getStackInSlot(i).isEmpty()) continue;
                ItemStack toExtract = handler.extractItem(i, remaining, true);
                ItemStack excess = ItemHandlerHelper.insertItemStacked(inventory, toExtract, false);
                int transferred = toExtract.getCount() - excess.getCount();
                if (transferred > 0) {
                    handler.extractItem(i, transferred, false);
                    remaining -= transferred;
                    if (remaining <= 0) {
                        return true;
                    }
                }
            }
            return remaining < maxItems;
        }

        // Suck in item entities
        for (EntityItem entity : getNeighborItems(this, inputDir)) {
            ItemStack remainder = IOHelper.insert(this, entity.getItem(), null, false);
            if (remainder.isEmpty()) {
                entity.setDead();
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
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty() || stack.getCount() < stack.getMaxStackSize()) {
                return false;
            }
        }
        return true;
    }

    static List<EntityItem> getNeighborItems(TileEntity te, EnumFacing dir) {
        AxisAlignedBB box = new AxisAlignedBB(te.getPos().offset(dir));
        return te.getWorld().getEntitiesWithinAABB(EntityItem.class, box, EntitySelectors.IS_ALIVE);
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

    public void setInputDirection(EnumFacing dir) {
        inputDir = dir;
    }

    public EnumFacing getInputDirection() {
        return inputDir;
    }

    @Override
    public EnumFacing getRotation() {
        return outputDir;
    }

    public void setRotation(EnumFacing rotation) {
        outputDir = rotation;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("inputDir", inputDir.ordinal());
        tag.setInteger("outputDir", outputDir.ordinal());
        tag.setInteger("redstoneMode", redstoneMode);
        tag.setInteger("leaveMaterialCount", leaveMaterialCount);
        tag.setTag("Items", inventory.serializeNBT());
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        inputDir = EnumFacing.byIndex(tag.getInteger("inputDir"));
        outputDir = EnumFacing.byIndex(tag.getInteger("outputDir"));
        redstoneMode = tag.getInteger("redstoneMode");
        if (tag.hasKey("leaveMaterial")) {
            leaveMaterialCount = (byte)(tag.getBoolean("leaveMaterial") ? 1 : 0);
        } else {
            leaveMaterialCount = tag.getInteger("leaveMaterialCount");
        }
        inventory.deserializeNBT(tag.getCompoundTag("Items"));
    }

    /**
     * Returns the name of the inventory.
     */
    @Override
    public String getName() {
        return Blockss.OMNIDIRECTIONAL_HOPPER.getTranslationKey();
    }

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player) {
        if (buttonID == 0) {
            redstoneMode++;
            if (redstoneMode > 2) redstoneMode = 0;
        } else if (buttonID == 1) {
            leaveMaterialCount = 0;
        } else if (buttonID == 2) {
            leaveMaterialCount = 1;
        }
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
}
