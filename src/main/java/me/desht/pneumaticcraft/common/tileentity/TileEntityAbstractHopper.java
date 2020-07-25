package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.block.BlockOmnidirectionalHopper;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.lib.NBTKeys;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;

import java.util.Collections;
import java.util.List;

public abstract class TileEntityAbstractHopper extends TileEntityTickableBase
        implements IRedstoneControlled, IComparatorSupport, INamedContainerProvider {
    private int lastComparatorValue = -1;
    @GuiSynced
    public int redstoneMode;
    private int cooldown;
    @GuiSynced
    int leaveMaterialCount; // leave items/liquids (used as filter)
    @DescSynced
    public boolean isCreative; // has a creative upgrade installed
    private boolean wasCreative = false;
    Direction inputDir = Direction.UP;
    private AxisAlignedBB inputAABB; // region to check for item entities

    TileEntityAbstractHopper(TileEntityType type) {
        super(type, 4);
    }

    public Direction getInputDirection() {
        return getBlockState().get(BlockOmnidirectionalHopper.INPUT_FACING);
    }

    @Override
    protected void onFirstServerTick() {
        super.onFirstServerTick();

        isCreative = getUpgrades(EnumUpgrade.CREATIVE) > 0;
        setupInputAABB();
    }

    private void setupInputAABB() {
        // The 0.625 and 1.375 values here ensure an accurate bounding box; the input bowl of the hopper's blockspace
        // plus the block in front of the input direction. Items in the hopper's blockspace but not in the input bowl
        // won't get sucked in.
        inputDir = getInputDirection();
        inputAABB = new AxisAlignedBB(pos)
                .offset(inputDir.getXOffset() * 0.625, inputDir.getYOffset() * 0.625, inputDir.getZOffset() * 0.625)
                .expand(inputDir.getXOffset() * 1.375, inputDir.getYOffset() * 1.375, inputDir.getZOffset() * 1.375);
    }

    @Override
    public void onBlockRotated() {
        super.onBlockRotated();

        setupInputAABB();
    }

    @Override
    public void tick() {
        inputDir = getInputDirection();

        super.tick();

        if (!getWorld().isRemote && --cooldown <= 0 && redstoneAllows()) {
            int maxItems = getMaxItems();
            boolean success = doImport(maxItems);
            success |= doExport(maxItems);

            // If we couldn't pull or push, slow down a bit for performance reasons
            cooldown = success ? getItemTransferInterval() : 8;

            if (lastComparatorValue != getComparatorValueInternal()) {
                lastComparatorValue = getComparatorValueInternal();
            }
        }
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        boolean rerender = wasCreative != isCreative;
        wasCreative = isCreative;
        return rerender;
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
        tag.putInt(NBTKeys.NBT_REDSTONE_MODE, redstoneMode);
        tag.putInt("leaveMaterialCount", leaveMaterialCount);
        return tag;
    }

    @Override
    public void read(BlockState state, CompoundNBT tag) {
        super.read(state, tag);

        redstoneMode = tag.getInt(NBTKeys.NBT_REDSTONE_MODE);
        if (tag.contains("leaveMaterial")) {
            leaveMaterialCount = (byte)(tag.getBoolean("leaveMaterial") ? 1 : 0);
        } else {
            leaveMaterialCount = tag.getInt("leaveMaterialCount");
        }
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, PlayerEntity player) {
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
    public void onUpgradesChanged() {
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

    List<ItemEntity> getNeighborItems() {
        return inputAABB == null ? Collections.emptyList() : world.getEntitiesWithinAABB(ItemEntity.class, inputAABB, EntityPredicates.IS_ALIVE);
    }
}
