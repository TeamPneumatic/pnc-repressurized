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

package me.desht.pneumaticcraft.common.block.entity;

import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.LazySynced;
import me.desht.pneumaticcraft.common.recipes.assembly.AssemblyProgram;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.BlockEntityConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AssemblyDrillBlockEntity extends AbstractAssemblyRobotBlockEntity {
    @DescSynced
    private boolean isDrillOn;
    @DescSynced
    @LazySynced
    private float drillSpeed;
    public float drillRotation;
    public float oldDrillRotation;
    private int drillStep;

    public AssemblyDrillBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.ASSEMBLY_DRILL.get(), pos, state);
    }

    @Override
    public void tickCommonPre() {
        super.tickCommonPre();

        oldDrillRotation = drillRotation;
        if (isDrillOn) {
            drillSpeed = Math.min(drillSpeed + BlockEntityConstants.ASSEMBLY_DRILL_ACCELERATION * speed, BlockEntityConstants.ASSEMBLY_DRILL_MAX_SPEED);
        } else {
            drillSpeed = Math.max(drillSpeed - BlockEntityConstants.ASSEMBLY_DRILL_ACCELERATION * speed, 0);
        }
        drillRotation += drillSpeed;
        while (drillRotation >= 360) {
            drillRotation -= 360;
        }
    }

    @Override
    public void tickServer() {
        super.tickServer();

        if (drillStep > 0) {
            TargetDirections platformDirection = getPlatformDirection();
            if (platformDirection == null) drillStep = 1;
            switch (drillStep) {
                case 1, 6 -> {
                    slowMode = false;
                    gotoHomePosition();
                }
                case 2 -> hoverOverNeighbour(platformDirection);
                case 3 -> isDrillOn = true;
                case 4 -> {
                    slowMode = true;
                    gotoNeighbour(platformDirection);
                }
                case 5 -> {
                    hoverOverNeighbour(platformDirection);
                    isDrillOn = false;
                    BlockEntity te = getTileEntityForCurrentDirection();
                    if (te instanceof AssemblyPlatformBlockEntity platform) {
                        ItemStack output = getDrilledOutputForItem(platform.getHeldStack());
                        if (!output.isEmpty()) {
                            platform.setHeldStack(output);
                        }
                    }
                }
            }
            if (isDoneInternal()) {
                drillStep++;
                if (drillStep > 6) drillStep = 0;
            }
        }
    }

    public void goDrilling() {
        if (drillStep == 0) {
            drillStep = 1;
            setChanged();
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("drill", isDrillOn);
        tag.putFloat("drillSpeed", drillSpeed);
        tag.putInt("drillStep", drillStep);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        isDrillOn = tag.getBoolean("drill");
        drillSpeed = tag.getFloat("drillSpeed");
        drillStep = tag.getInt("drillStep");
    }

    @Override
    public boolean hasItemCapability() {
        return false;
    }

    @Override
    public boolean isIdle() {
        return drillStep == 0 && isDoneInternal();
    }

    @Override
    public AssemblyProgram.EnumMachine getAssemblyType() {
        return AssemblyProgram.EnumMachine.DRILL;
    }

    private boolean isDoneInternal() {
        if (super.isDoneMoving()) {
            return isDrillOn ? drillSpeed > BlockEntityConstants.ASSEMBLY_DRILL_MAX_SPEED - 1F : PneumaticCraftUtils.epsilonEquals(drillSpeed, 0F);
        } else {
            return false;
        }
    }

    @Override
    public boolean canMoveToDiagonalNeighbours() {
        return false;
    }

    private ItemStack getDrilledOutputForItem(ItemStack input) {
        return ModRecipeTypes.ASSEMBLY_DRILL.get().stream(level)
                .map(RecipeHolder::value)
                .filter(recipe -> recipe.matches(input))
                .findFirst()
                .map(recipe -> recipe.getOutput().copy())
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public boolean reset() {
        if (isIdle()) return true;
        else {
            isDrillOn = false;
            drillStep = 6;
            return false;
        }
    }
}
