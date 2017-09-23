package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.IOHelper.LocatedItemStack;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;

public class TileEntityOmnidirectionalHopper extends TileEntityBase implements IRedstoneControlled {
    public static final int INVENTORY_SIZE = 5;
    @DescSynced
    protected EnumFacing inputDir = EnumFacing.UP;
    @DescSynced
    protected EnumFacing outputDir = EnumFacing.UP;
    private ItemStackHandler inventory = new ItemStackHandler(getInvSize());
    @GuiSynced
    public int redstoneMode;
    private int cooldown;
    @GuiSynced
    protected boolean leaveMaterial;//leave items/liquids (used as filter)

    public TileEntityOmnidirectionalHopper() {
        this(4);
    }

    public TileEntityOmnidirectionalHopper(int upgradeSlots) {
        super(upgradeSlots);
        addApplicableUpgrade(EnumUpgrade.SPEED);
    }

    protected int getInvSize() {
        return INVENTORY_SIZE;
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
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
        }
    }

    protected boolean doExport(int maxItems) {
        EnumFacing dir = getRotation();
        TileEntity neighbor = IOHelper.getNeighbor(this, dir);
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty() && (!leaveMaterial || stack.getCount() > 1)) {
                ItemStack exportedStack = stack.copy();
                if (leaveMaterial) exportedStack.shrink(1);
                if (exportedStack.getCount() > maxItems) exportedStack.setCount(maxItems);
                int count = exportedStack.getCount();

                ItemStack remainder = IOHelper.insert(neighbor, exportedStack, dir.getOpposite(), false);
                int exportedItems = count - remainder.getCount();

                stack.shrink(exportedItems);
                if (stack.getCount() <= 0) {
                    inventory.setStackInSlot(i, ItemStack.EMPTY);
                }
                maxItems -= exportedItems;
                if (maxItems <= 0) return true;
            }
        }
        return false;
    }

    protected boolean doImport(int maxItems) {
        boolean success = false;

        // Suck from input inventory
        IItemHandler handler = IOHelper.getInventoryForTE(IOHelper.getNeighbor(this, inputDir));
        if (handler != null) {
            for (int i = 0; i < maxItems; i++) {
                LocatedItemStack extracted = IOHelper.extractOneItem(handler, true);
                if (!extracted.stack.isEmpty()) {
                    ItemStack excess = ItemHandlerHelper.insertItem(inventory, extracted.stack, false);
                    if (excess.isEmpty()) {
                        handler.extractItem(extracted.slot, 1, false);
                        success = true;
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
        }

        // Suck in item entities
        for (EntityItem entity : getNeighborItems(this, inputDir)) {
            if (!entity.isDead) {
                ItemStack remainder = IOHelper.insert(this, entity.getItem(), null, false);
                if (remainder.isEmpty()) {
                    entity.setDead();
                    success = true;
                }
            }
        }

        return success;
    }

    static List<EntityItem> getNeighborItems(TileEntity te, EnumFacing inputDir) {
        AxisAlignedBB box = new AxisAlignedBB(te.getPos().offset(inputDir), te.getPos().offset(inputDir).add(1, 1, 1));
        return te.getWorld().getEntitiesWithinAABB(EntityItem.class, box);
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
        tag.setBoolean("leaveMaterial", leaveMaterial);
        tag.setTag("Items", inventory.serializeNBT());
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        inputDir = EnumFacing.getFront(tag.getInteger("inputDir"));
        outputDir = EnumFacing.getFront(tag.getInteger("outputDir"));
        redstoneMode = tag.getInteger("redstoneMode");
        leaveMaterial = tag.getBoolean("leaveMaterial");
        inventory.deserializeNBT(tag.getCompoundTag("Items"));
    }

    /**
     * Returns the name of the inventory.
     */
    @Override
    public String getName() {
        return Blockss.OMNIDIRECTIONAL_HOPPER.getUnlocalizedName();
    }

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player) {
        if (buttonID == 0) {
            redstoneMode++;
            if (redstoneMode > 2) redstoneMode = 0;
        } else if (buttonID == 1) {
            leaveMaterial = false;
        } else if (buttonID == 2) {
            leaveMaterial = true;
        }
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
    }

    public boolean doesLeaveMaterial() {
        return leaveMaterial;
    }
}
