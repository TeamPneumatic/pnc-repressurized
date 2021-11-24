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
import me.desht.pneumaticcraft.common.network.LazySynced;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import me.desht.pneumaticcraft.common.recipes.assembly.AssemblyProgram;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.IItemHandler;

public class TileEntityAssemblyDrill extends TileEntityAssemblyRobot {
    @DescSynced
    private boolean isDrillOn;
    @DescSynced
    @LazySynced
    private float drillSpeed;
    public float drillRotation;
    public float oldDrillRotation;
    private int drillStep;

    public TileEntityAssemblyDrill() {
        super(ModTileEntities.ASSEMBLY_DRILL.get());
    }

    @Override
    public void tick() {
        oldDrillRotation = drillRotation;
        super.tick();
        if (isDrillOn) {
            drillSpeed = Math.min(drillSpeed + TileEntityConstants.ASSEMBLY_DRILL_ACCELERATION * speed, TileEntityConstants.ASSEMBLY_DRILL_MAX_SPEED);
        } else {
            drillSpeed = Math.max(drillSpeed - TileEntityConstants.ASSEMBLY_DRILL_ACCELERATION, 0);
        }
        drillRotation += drillSpeed;
        while (drillRotation >= 360) {
            drillRotation -= 360;
        }

        if (!getLevel().isClientSide && drillStep > 0) {
            TargetDirections platformDirection = getPlatformDirection();
            if (platformDirection == null) drillStep = 1;
            switch (drillStep) {
                case 1:
                case 6:
                    slowMode = false;
                    gotoHomePosition();
                    break;
                case 2:
                    hoverOverNeighbour(platformDirection);
                    break;
                case 3:
                    isDrillOn = true;
                    break;
                case 4:
                    slowMode = true;
                    gotoNeighbour(platformDirection);
                    break;
                case 5:
                    hoverOverNeighbour(platformDirection);
                    isDrillOn = false;
                    TileEntity te = getTileEntityForCurrentDirection();
                    if (te instanceof TileEntityAssemblyPlatform) {
                        TileEntityAssemblyPlatform platform = (TileEntityAssemblyPlatform) te;
                        ItemStack output = getDrilledOutputForItem(platform.getHeldStack());
                        if (!output.isEmpty()) {
                            platform.setHeldStack(output);
                        }
                    }
                    break;
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
    public CompoundNBT save(CompoundNBT tag) {
        super.save(tag);
        tag.putBoolean("drill", isDrillOn);
        tag.putFloat("drillSpeed", drillSpeed);
        tag.putInt("drillStep", drillStep);
        return tag;
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);

        isDrillOn = tag.getBoolean("drill");
        drillSpeed = tag.getFloat("drillSpeed");
        drillStep = tag.getInt("drillStep");
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return null;
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
            return isDrillOn ? drillSpeed > TileEntityConstants.ASSEMBLY_DRILL_MAX_SPEED - 1F : PneumaticCraftUtils.epsilonEquals(drillSpeed, 0F);
        } else {
            return false;
        }
    }

    @Override
    public boolean canMoveToDiagonalNeighbours() {
        return false;
    }

    private ItemStack getDrilledOutputForItem(ItemStack input) {
        return PneumaticCraftRecipeType.ASSEMBLY_DRILL.stream(level)
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
