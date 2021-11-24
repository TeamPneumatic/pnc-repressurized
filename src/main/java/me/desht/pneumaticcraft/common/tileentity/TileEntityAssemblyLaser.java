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
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class TileEntityAssemblyLaser extends TileEntityAssemblyRobot {
    @DescSynced
    public boolean isLaserOn;
    private int laserStep; //used to progressively draw a circle.
    private static final float ITEM_SIZE = 10F;

    public TileEntityAssemblyLaser() {
        super(ModTileEntities.ASSEMBLY_LASER.get());
    }

    @Override
    public void tick() {
        super.tick();
        if (laserStep > 0) {
            TargetDirections platformDirection = getPlatformDirection();
            if (platformDirection == null) {
                laserStep = 105;
            }
            switch (laserStep) {
                case 1:
                    //                    isLaserOn = false;
                    slowMode = false;
                    //                    gotoHomePosition();
                    break;
                case 2:
                    hoverOverNeighbour(platformDirection);
                    break;
                case 3:
                    slowMode = true;
                    gotoNeighbour(platformDirection);
                    break;
                case 104:
                    hoverOverNeighbour(platformDirection);
                    isLaserOn = false;
                    slowMode = true;
                    TileEntity te = getTileEntityForCurrentDirection();
                    if (te instanceof TileEntityAssemblyPlatform) {
                        TileEntityAssemblyPlatform platform = (TileEntityAssemblyPlatform) te;
                        ItemStack output = getLaseredOutputForItem(platform.getHeldStack());
                        if (!output.isEmpty()) {
                            platform.setHeldStack(output);
                        }
                    }
                    break;
                case 105:
                    slowMode = false;
                    isLaserOn = false;
                    gotoHomePosition();
                    break;
                default: //4-103
                    isLaserOn = true;
                    slowMode = false;
                    targetAngles[EnumAngles.BASE.getIndex()] = 100F - (float) PneumaticCraftUtils.sin[(laserStep - 4) * PneumaticCraftUtils.CIRCLE_POINTS / 100] * ITEM_SIZE;
                    targetAngles[EnumAngles.MIDDLE.getIndex()] = -10F + (float) PneumaticCraftUtils.sin[(laserStep - 4) * PneumaticCraftUtils.CIRCLE_POINTS / 100] * ITEM_SIZE;
                    targetAngles[EnumAngles.TAIL.getIndex()] = 0F;
                    targetAngles[EnumAngles.TURN.getIndex()] += (float) PneumaticCraftUtils.sin[(laserStep - 4) * PneumaticCraftUtils.CIRCLE_POINTS / 100] * ITEM_SIZE * 0.03D;
                    break;
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
    public CompoundNBT save(CompoundNBT tag) {
        super.save(tag);
        tag.putBoolean("laser", isLaserOn);
        tag.putInt("laserStep", laserStep);
        return tag;
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);
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
