package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.core.ModTileEntityTypes;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.LazySynced;
import me.desht.pneumaticcraft.common.recipes.AssemblyRecipe;
import me.desht.pneumaticcraft.common.recipes.programs.AssemblyProgram;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.items.IItemHandlerModifiable;

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
        super(ModTileEntityTypes.ASSEMBLY_DRILL);
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

        if (!getWorld().isRemote && drillStep > 0) {
            Direction[] platformDirection = getPlatformDirection();
            if (platformDirection == null) drillStep = 1;
            switch (drillStep) {
                case 1:
                    slowMode = false;
                    gotoHomePosition();
                    break;
                case 2:
                    hoverOverNeighbour(platformDirection[0], platformDirection[1]);
                    break;
                case 3:
                    isDrillOn = true;
                    break;
                case 4:
                    slowMode = true;
                    gotoNeighbour(platformDirection[0], platformDirection[1]);
                    break;
                case 5:
                    hoverOverNeighbour(platformDirection[0], platformDirection[1]);
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
                case 6:
                    slowMode = false;
                    gotoHomePosition();
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
            markDirty();
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        tag.putBoolean("drill", isDrillOn);
        tag.putFloat("drillSpeed", drillSpeed);
        tag.putInt("drillStep", drillStep);
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        isDrillOn = tag.getBoolean("drill");
        drillSpeed = tag.getFloat("drillSpeed");
        drillStep = tag.getInt("drillStep");
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
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
            return isDrillOn ? drillSpeed > TileEntityConstants.ASSEMBLY_DRILL_MAX_SPEED - 1F : PneumaticCraftUtils.areFloatsEqual(drillSpeed, 0F);
        } else {
            return false;
        }
    }

    @Override
    public boolean canMoveToDiagonalNeighbours() {
        return false;
    }

    private static ItemStack getDrilledOutputForItem(ItemStack input) {
        for (AssemblyRecipe recipe : AssemblyRecipe.drillRecipes) {
            if (AssemblyProgram.isValidInput(recipe, input)) return recipe.getOutput().copy();
        }
        return ItemStack.EMPTY;
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
