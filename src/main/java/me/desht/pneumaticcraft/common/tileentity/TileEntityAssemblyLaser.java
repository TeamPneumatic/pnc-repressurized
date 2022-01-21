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

import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import me.desht.pneumaticcraft.common.recipes.assembly.AssemblyProgram;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class TileEntityAssemblyLaser extends TileEntityAssemblyRobot {
    @DescSynced
    public boolean isLaserOn;
    private int laserStep; //used to progressively draw a circle.
    private static final float ITEM_SIZE = 10F;

    public TileEntityAssemblyLaser(BlockPos pos, BlockState state) {
        super(ModTileEntities.ASSEMBLY_LASER.get(), pos, state);
    }

    @Override
    public void tickServer() {
        super.tickServer();

        if (laserStep > 0) {
            TargetDirections platformDirection = getPlatformDirection();
            if (platformDirection == null) {
                laserStep = 105;
            }
            switch (laserStep) {
                case 1 -> slowMode = false;
                case 2 -> hoverOverNeighbour(platformDirection);
                case 3 -> {
                    slowMode = true;
                    gotoNeighbour(platformDirection);
                }
                case 104 -> {
                    hoverOverNeighbour(platformDirection);
                    isLaserOn = false;
                    slowMode = true;
                    BlockEntity te = getTileEntityForCurrentDirection();
                    if (te instanceof TileEntityAssemblyPlatform platform) {
                        ItemStack output = getLaseredOutputForItem(platform.getHeldStack());
                        if (!output.isEmpty()) {
                            platform.setHeldStack(output);
                        }
                    }
                }
                case 105 -> {
                    slowMode = false;
                    isLaserOn = false;
                    gotoHomePosition();
                }
                default -> { //4-103
                    isLaserOn = true;
                    slowMode = false;
                    targetAngles[EnumAngles.BASE.getIndex()] = 100F - (float) PneumaticCraftUtils.sin[(laserStep - 4) * PneumaticCraftUtils.CIRCLE_POINTS / 100] * ITEM_SIZE;
                    targetAngles[EnumAngles.MIDDLE.getIndex()] = -10F + (float) PneumaticCraftUtils.sin[(laserStep - 4) * PneumaticCraftUtils.CIRCLE_POINTS / 100] * ITEM_SIZE;
                    targetAngles[EnumAngles.TAIL.getIndex()] = 0F;
                    targetAngles[EnumAngles.TURN.getIndex()] += (float) PneumaticCraftUtils.sin[(laserStep - 4) * PneumaticCraftUtils.CIRCLE_POINTS / 100] * ITEM_SIZE * 0.03D;
                }
            }
            if (isDoneInternal() || laserStep >= 4 && laserStep <= 103) {
                laserStep++;
                if (laserStep > 105) laserStep = 0;
            }
        }
    }

    public void startLasering() {
        if (laserStep == 0) {
            laserStep = 1;
        }
    }

    @Override
    public boolean gotoNeighbour(TargetDirections targetDirections) {
        boolean diagonal = super.gotoNeighbour(targetDirections);
        targetAngles[EnumAngles.TURN.getIndex()] -= ITEM_SIZE * 0.45D;
        return diagonal;
    }

    private boolean isDoneInternal() {
        return super.isDoneMoving();
    }

    @Override
    public boolean isIdle() {
        return laserStep == 0 && isDoneInternal();
    }

    @Override
    public AssemblyProgram.EnumMachine getAssemblyType() {
        return AssemblyProgram.EnumMachine.LASER;
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("laser", isLaserOn);
        tag.putInt("laserStep", laserStep);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        isLaserOn = tag.getBoolean("laser");
        laserStep = tag.getInt("laserStep");
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return null;
    }

    @Override
    public boolean canMoveToDiagonalNeighbours() {
        return false;
    }

    @Nonnull
    private ItemStack getLaseredOutputForItem(ItemStack input) {
        return PneumaticCraftRecipeType.ASSEMBLY_LASER.stream(level)
                .filter(recipe -> recipe.matches(input))
                .findFirst()
                .map(recipe -> recipe.getOutput().copy())
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public boolean reset() {
        if (isIdle()) {
            return true;
        } else {
            isLaserOn = false;
            laserStep = 105;
            return false;
        }
    }
}
