package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.block.BlockOmnidirectionalHopper;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class TileEntityAbstractHopper<T extends TileEntity & IRedstoneControl<T>> extends TileEntityTickableBase
        implements IRedstoneControl<T>, IComparatorSupport, INamedContainerProvider {
    private static final int BASE_TICK_RATE = 8;

    private int lastComparatorValue = -1;
    private int cooldown;
    private int entityScanCooldown;
    @GuiSynced
    int leaveMaterialCount; // leave items/liquids (used as filter)
    @DescSynced
    public boolean isCreative; // has a creative upgrade installed
    private boolean wasCreative = false;
    Direction inputDir = Direction.UP;
    // regions to check for entities (items, or maybe entities with an item/fluid capability)
    AxisAlignedBB inputAABB;
    AxisAlignedBB outputAABB;
    final List<Entity> cachedInputEntities = new ArrayList<>();
    final List<Entity> cachedOutputEntities = new ArrayList<>();

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
        setupInputOutputRegions();
    }

    @Override
    public void onBlockRotated() {
        super.onBlockRotated();

        inputDir = getInputDirection();
        setupInputOutputRegions();
    }

    @Override
    public void tick() {
        inputDir = getInputDirection();

        super.tick();

        if (!getWorld().isRemote && getRedstoneController().shouldRun()) {
            if (--entityScanCooldown <= 0) {
                cachedInputEntities.clear();
                if (shouldScanForEntities(inputDir)) {
                    cachedInputEntities.addAll(world.getEntitiesWithinAABB(Entity.class, inputAABB, EntityPredicates.IS_ALIVE));
                }
                cachedOutputEntities.clear();
                if (shouldScanForEntities(getRotation())) {
                    cachedOutputEntities.addAll(world.getEntitiesWithinAABB(Entity.class, outputAABB, EntityPredicates.IS_ALIVE));
                }
                entityScanCooldown = BASE_TICK_RATE;
            }

            if (--cooldown <= 0) {
                int maxItems = getMaxItems();
                boolean success = doImport(maxItems);
                success |= doExport(maxItems);

                // If we couldn't pull or push, slow down a bit for performance reasons
                cooldown = success ? getItemTransferInterval() : BASE_TICK_RATE;

                if (lastComparatorValue != getComparatorValueInternal()) {
                    lastComparatorValue = getComparatorValueInternal();
                }
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
        return BASE_TICK_RATE / (1 << getUpgrades(EnumUpgrade.SPEED));
    }

    protected abstract void setupInputOutputRegions();

    protected abstract boolean doExport(int maxItems);

    protected abstract boolean doImport(int maxItems);

    protected abstract int getComparatorValueInternal();

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        tag.putInt("leaveMaterialCount", leaveMaterialCount);
        return tag;
    }

    @Override
    public void read(BlockState state, CompoundNBT tag) {
        super.read(state, tag);

        if (tag.contains("leaveMaterial")) {
            leaveMaterialCount = (byte)(tag.getBoolean("leaveMaterial") ? 1 : 0);
        } else {
            leaveMaterialCount = tag.getInt("leaveMaterialCount");
        }
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, PlayerEntity player) {
        if (getRedstoneController().parseRedstoneMode(tag))
            return;

        switch (tag) {
            case "empty":
                leaveMaterialCount = 0;
                break;
            case "leave":
                leaveMaterialCount = 1;
                break;
        }

        markDirty();

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

    List<ItemEntity> getNeighborItems(AxisAlignedBB aabb) {
        return aabb == null ? Collections.emptyList() : world.getEntitiesWithinAABB(ItemEntity.class, aabb, EntityPredicates.IS_ALIVE);
    }

    abstract boolean shouldScanForEntities(Direction dir);
}
