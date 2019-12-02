package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.common.block.BlockOmnidirectionalHopper;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public abstract class TileEntityAbstractHopper extends TileEntityTickableBase implements IRedstoneControlled, IComparatorSupport, INamedContainerProvider {
    private int lastComparatorValue = -1;
    @GuiSynced
    public int redstoneMode;
    private int cooldown;
    @GuiSynced
    int leaveMaterialCount; // leave items/liquids (used as filter)
    @DescSynced
    public boolean isCreative; // has a creative upgrade installed
    protected Direction inputDir = Direction.UP;

    public TileEntityAbstractHopper(TileEntityType type) {
        super(type, 4);
        addApplicableUpgrade(EnumUpgrade.SPEED);
        addApplicableUpgrade(EnumUpgrade.CREATIVE);
    }

    public Direction getInputDirection() {
        return getBlockState().get(BlockOmnidirectionalHopper.INPUT_FACING);
    }

    @Override
    protected void onFirstServerUpdate() {
        super.onFirstServerUpdate();

        isCreative = getUpgrades(EnumUpgrade.CREATIVE) > 0;
    }

    @Override
    public void tick() {
        super.tick();

        inputDir = getInputDirection();

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

    protected abstract boolean doExport(int maxItems);

    protected abstract boolean doImport(int maxItems);

    protected abstract int getComparatorValueInternal();

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        tag.putInt("redstoneMode", redstoneMode);
        tag.putInt("leaveMaterialCount", leaveMaterialCount);
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        redstoneMode = tag.getInt("redstoneMode");
        if (tag.contains("leaveMaterial")) {
            leaveMaterialCount = (byte)(tag.getBoolean("leaveMaterial") ? 1 : 0);
        } else {
            leaveMaterialCount = tag.getInt("leaveMaterialCount");
        }
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

    static List<ItemEntity> getNeighborItems(TileEntity te, Direction dir) {
        AxisAlignedBB box = new AxisAlignedBB(te.getPos().offset(dir));
        return te.getWorld().getEntitiesWithinAABB(ItemEntity.class, box, EntityPredicates.IS_ALIVE);
    }

}
