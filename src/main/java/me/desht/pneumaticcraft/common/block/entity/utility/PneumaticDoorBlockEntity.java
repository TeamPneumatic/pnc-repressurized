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

package me.desht.pneumaticcraft.common.block.entity.utility;

import me.desht.pneumaticcraft.common.block.PneumaticDoorBlock;
import me.desht.pneumaticcraft.common.block.entity.AbstractTickingBlockEntity;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.LazySynced;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class PneumaticDoorBlockEntity extends AbstractTickingBlockEntity {
    @DescSynced
    @LazySynced
    public float rotationAngle;
    public float oldRotationAngle;
    @DescSynced
    public boolean rightGoing;  // true = door rotates clockwise when door base arm extends
    @DescSynced
    public int color = DyeColor.WHITE.getId();

    public PneumaticDoorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.PNEUMATIC_DOOR.get(), pos, state);
    }

    @Override
    public boolean hasItemCapability() {
        return false;
    }

    public void setRotationAngle(float rotationAngle) {
        if (oldRotationAngle == rotationAngle) return;

        oldRotationAngle = this.rotationAngle;
        this.rotationAngle = rotationAngle;

        if (oldRotationAngle < 90f && rotationAngle == 90f) {
            nonNullLevel().setBlockAndUpdate(worldPosition, getBlockState().setValue(DoorBlock.OPEN, true));
        } else if (oldRotationAngle == 90f && rotationAngle < 90f) {
            nonNullLevel().setBlockAndUpdate(worldPosition, getBlockState().setValue(DoorBlock.OPEN, false));
        }

        // also rotate the BE for the other half of the door
        BlockPos otherPos = getBlockPos().relative(isTopDoor() ? Direction.DOWN : Direction.UP);
        PneumaticCraftUtils.getTileEntityAt(getLevel(), otherPos, PneumaticDoorBlockEntity.class).ifPresent(otherDoorHalf -> {
            otherDoorHalf.rightGoing = rightGoing;
            otherDoorHalf.setChanged();
            if (rotationAngle != otherDoorHalf.rotationAngle) {
                otherDoorHalf.setRotationAngle(rotationAngle);
            }
        });
    }

    public boolean setColor(DyeColor dyeColor) {
        if (color != dyeColor.getId() && !getBlockState().getValue(PneumaticDoorBlock.TOP_DOOR)) {
            color = (byte) dyeColor.getId();
            nonNullLevel().getBlockEntity(getBlockPos(), ModBlockEntityTypes.PNEUMATIC_DOOR.get()).ifPresent(topHalf -> {
                topHalf.color = color;
                if (!nonNullLevel().isClientSide) {
                    setChanged();
                    topHalf.setChanged();
                    sendDescriptionPacket();
                }
            });
            return true;
        }
        return false;
    }

    private boolean isTopDoor() {
        return PneumaticDoorBlock.isTopDoor(nonNullLevel().getBlockState(getBlockPos()));
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        tag.putBoolean("rightGoing", rightGoing);
        tag.putInt("color", color);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        rightGoing = tag.getBoolean("rightGoing");
        color = tag.getInt("color");
        scheduleDescriptionPacket();
    }

    @Override
    public void serializeExtraItemData(CompoundTag blockEntityTag, boolean preserveState) {
        super.serializeExtraItemData(blockEntityTag, preserveState);

        blockEntityTag.putInt("color", color);
    }

    @Override
    public IItemHandler getItemHandler(@Nullable Direction dir) {
        return null;
    }

    @Override
    public boolean shouldPreserveStateOnBreak() {
        // keep color even if pickaxed
        return true;
    }

    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(),
                getBlockPos().getX() + 1, getBlockPos().getY() + 2, getBlockPos().getZ() + 1);
    }
}
