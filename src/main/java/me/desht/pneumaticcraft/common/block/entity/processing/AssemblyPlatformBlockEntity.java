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

package me.desht.pneumaticcraft.common.block.entity.processing;

import me.desht.pneumaticcraft.common.block.entity.AbstractTickingBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.IResettable;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.LazySynced;
import me.desht.pneumaticcraft.common.recipes.assembly.AssemblyProgram;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.lib.BlockEntityConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class AssemblyPlatformBlockEntity extends AbstractTickingBlockEntity implements IAssemblyMachine, IResettable {
    @DescSynced
    private boolean shouldClawClose;
    @DescSynced
    @LazySynced
    public float clawProgress;
    public float oldClawProgress;
    @DescSynced
    private final BaseItemStackHandler itemHandler = new BaseItemStackHandler(this, 1);
    private float speed = 1.0F;
    private BlockPos controllerPos;

    public AssemblyPlatformBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.ASSEMBLY_PLATFORM.get(), pos, state);
    }

    @Override
    public boolean hasItemCapability() {
        return false;  // the inventory is not exposed for capability purposes
    }

    @Override
    public void tickCommonPre() {
        super.tickCommonPre();

        oldClawProgress = clawProgress;
        if (!shouldClawClose && clawProgress > 0F) {
            clawProgress = Math.max(clawProgress - BlockEntityConstants.ASSEMBLY_IO_UNIT_CLAW_SPEED * speed, 0);
        } else if (shouldClawClose && clawProgress < 1F) {
            clawProgress = Math.min(clawProgress + BlockEntityConstants.ASSEMBLY_IO_UNIT_CLAW_SPEED * speed, 1);
        }
    }

    private boolean isClawDone() {
        return clawProgress == (shouldClawClose ? 1F : 0F);
    }

    @Override
    public boolean isIdle() {
        return !shouldClawClose && isClawDone() && getHeldStack().isEmpty();
    }

    @Override
    public boolean reset() {
        openClaw();
        return isIdle();
    }

    boolean closeClaw() {
        shouldClawClose = true;
        sendDescriptionPacket();
        return isClawDone();
    }

    boolean openClaw() {
        shouldClawClose = false;
        sendDescriptionPacket();
        return isClawDone();
    }

    @Nonnull
    public ItemStack getHeldStack() {
        return itemHandler.getStackInSlot(0);
    }

    public void setHeldStack(@Nonnull ItemStack stack) {
        itemHandler.setStackInSlot(0, stack);
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putBoolean("clawClosing", shouldClawClose);
        tag.putFloat("clawProgress", clawProgress);
        tag.putFloat("speed", speed);
        tag.put("Items", itemHandler.serializeNBT(provider));
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        shouldClawClose = tag.getBoolean("clawClosing");
        clawProgress = tag.getFloat("clawProgress");
        speed = tag.getFloat("speed");
        itemHandler.deserializeNBT(provider, tag.getCompound("Items"));
    }

    @Override
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    @Override
    public AssemblyProgram.EnumMachine getAssemblyType() {
        return AssemblyProgram.EnumMachine.PLATFORM;
    }

    @Override
    public void setControllerPos(BlockPos controllerPos) {
        this.controllerPos = controllerPos;
    }

    @Override
    public void onNeighborBlockUpdate(BlockPos fromPos) {
        super.onNeighborBlockUpdate(fromPos);
        invalidateSystem();
    }

    @Override
    public IItemHandler getItemHandler(@Nullable Direction dir) {
        return itemHandler;
    }

    private void invalidateSystem() {
        if (controllerPos != null) {
            BlockEntity te = nonNullLevel().getBlockEntity(controllerPos);
            if (te instanceof AssemblyControllerBlockEntity) {
                ((AssemblyControllerBlockEntity) te).invalidateAssemblySystem();
            }
        }
    }
}
