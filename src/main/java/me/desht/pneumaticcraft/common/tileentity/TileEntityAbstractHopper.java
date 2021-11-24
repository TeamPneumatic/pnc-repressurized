/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.block.BlockOmnidirectionalHopper;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;

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
        return getBlockState().getValue(BlockOmnidirectionalHopper.INPUT_FACING);
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

        if (!getLevel().isClientSide && getRedstoneController().shouldRun()) {
            if (--entityScanCooldown <= 0) {
                cachedInputEntities.clear();
                if (shouldScanForEntities(inputDir)) {
                    cachedInputEntities.addAll(level.getEntitiesOfClass(Entity.class, inputAABB, EntityPredicates.ENTITY_STILL_ALIVE));
                }
                cachedOutputEntities.clear();
                if (shouldScanForEntities(getRotation())) {
                    cachedOutputEntities.addAll(level.getEntitiesOfClass(Entity.class, outputAABB, EntityPredicates.ENTITY_STILL_ALIVE));
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
    public CompoundNBT save(CompoundNBT tag) {
        super.save(tag);
        tag.putInt("leaveMaterialCount", leaveMaterialCount);
        return tag;
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);

        if (tag.contains("leaveMaterial")) {
            leaveMaterialCount = (byte)(tag.getBoolean("leaveMaterial") ? 1 : 0);
        } else {
            leaveMaterialCount = tag.getInt("leaveMaterialCount");
        }
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayerEntity player) {
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

        setChanged();

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

        if (level != null && !level.isClientSide) {
            isCreative = getUpgrades(EnumUpgrade.CREATIVE) > 0;
        }
    }

    @Override
    public boolean shouldPreserveStateOnBreak() {
        // always preserve state, since we can't sneak-wrench this machine (sneak-wrench rotates output)
        return true;
    }

    List<ItemEntity> getNeighborItems(AxisAlignedBB aabb) {
        return aabb == null ? Collections.emptyList() : level.getEntitiesOfClass(ItemEntity.class, aabb, EntityPredicates.ENTITY_STILL_ALIVE);
    }

    abstract boolean shouldScanForEntities(Direction dir);
}
