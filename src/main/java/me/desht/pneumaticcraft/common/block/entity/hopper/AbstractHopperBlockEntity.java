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

package me.desht.pneumaticcraft.common.block.entity.hopper;

import me.desht.pneumaticcraft.common.block.OmnidirectionalHopperBlock;
import me.desht.pneumaticcraft.common.block.entity.AbstractTickingBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.IComparatorSupport;
import me.desht.pneumaticcraft.common.block.entity.IRedstoneControl;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractHopperBlockEntity<T extends BlockEntity & IRedstoneControl<T>> extends AbstractTickingBlockEntity
        implements IRedstoneControl<T>, IComparatorSupport, MenuProvider {
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
    AABB inputAABB;
    AABB outputAABB;
    final List<Entity> cachedInputEntities = new ArrayList<>();
    final List<Entity> cachedOutputEntities = new ArrayList<>();
    private boolean firstTick = true;

    AbstractHopperBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 4);
    }

    public Direction getInputDirection() {
        return getBlockState().getValue(OmnidirectionalHopperBlock.INPUT_FACING);
    }

    @Override
    public void onBlockRotated() {
        super.onBlockRotated();

        inputDir = getInputDirection();
        setupInputOutputRegions();
    }

    @Override
    public void tickServer() {
        inputDir = getInputDirection();

        if (firstTick) {
            isCreative = getUpgrades(ModUpgrades.CREATIVE.get()) > 0;
            setupInputOutputRegions();
            firstTick = false;
        }

        super.tickServer();

        if (getRedstoneController().shouldRun()) {
            if (--entityScanCooldown <= 0) {
                cachedInputEntities.clear();
                if (shouldScanForEntities(inputDir)) {
                    cachedInputEntities.addAll(nonNullLevel().getEntitiesOfClass(Entity.class, inputAABB, EntitySelector.ENTITY_STILL_ALIVE));
                }
                cachedOutputEntities.clear();
                if (shouldScanForEntities(getRotation())) {
                    cachedOutputEntities.addAll(nonNullLevel().getEntitiesOfClass(Entity.class, outputAABB, EntitySelector.ENTITY_STILL_ALIVE));
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
        int upgrades = getUpgrades(ModUpgrades.SPEED.get());
        if (upgrades > 3) {
            return Math.min(1 << (upgrades - 3), 256);
        } else {
            return 1;
        }
    }

    public int getItemTransferInterval() {
        return BASE_TICK_RATE / (1 << getUpgrades(ModUpgrades.SPEED.get()));
    }

    protected abstract void setupInputOutputRegions();

    protected abstract boolean doExport(int maxItems);

    protected abstract boolean doImport(int maxItems);

    protected abstract int getComparatorValueInternal();

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("leaveMaterialCount", leaveMaterialCount);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        if (tag.contains("leaveMaterial")) {
            leaveMaterialCount = (byte)(tag.getBoolean("leaveMaterial") ? 1 : 0);
        } else {
            leaveMaterialCount = tag.getInt("leaveMaterialCount");
        }
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player) {
        if (getRedstoneController().parseRedstoneMode(tag))
            return;

        switch (tag) {
            case "empty" -> leaveMaterialCount = 0;
            case "leave" -> leaveMaterialCount = 1;
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
            isCreative = getUpgrades(ModUpgrades.CREATIVE.get()) > 0;
        }
    }

    @Override
    public boolean shouldPreserveStateOnBreak() {
        // always preserve state, since we can't sneak-wrench this machine (sneak-wrench rotates output)
        return true;
    }

    abstract boolean shouldScanForEntities(Direction dir);

    protected final boolean isInputBlocked() {
        BlockPos inputPos = worldPosition.relative(inputDir);
        return Block.isShapeFullBlock(nonNullLevel().getBlockState(inputPos).getShape(nonNullLevel(), inputPos));
    }
}
