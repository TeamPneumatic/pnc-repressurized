package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IManoMeasurable;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class TileEntityVacuumPump extends TileEntityPneumaticBase implements IRedstoneControlled, IManoMeasurable {
    @GuiSynced
    private final IAirHandler vacuumHandler = PneumaticRegistry.getInstance().getAirHandlerSupplier().createTierOneAirHandler(PneumaticValues.VOLUME_VACUUM_PUMP);
    public int rotation;
    public int oldRotation;
    public int turnTimer = -1;
    @DescSynced
    public boolean turning = false;
    public int rotationSpeed;
    @GuiSynced
    public int redstoneMode;

    public TileEntityVacuumPump() {
        super(PneumaticValues.DANGER_PRESSURE_VACUUM_PUMP, PneumaticValues.MAX_PRESSURE_VACUUM_PUMP, PneumaticValues.VOLUME_VACUUM_PUMP, 4);
        addApplicableUpgrade(EnumUpgrade.SPEED);
    }

    @Override
    public IAirHandler getAirHandler(EnumFacing side) {
        if (side == null || side == getInputSide()) {
            return super.getAirHandler(side);
        } else if (side == getVacuumSide()) {
            return vacuumHandler;
        } else {
            return null;
        }
    }

    @Override
    public void validate() {
        super.validate();
        vacuumHandler.validate(this);
    }

    @Override
    public void onNeighborTileUpdate() {
        super.onNeighborTileUpdate();
        vacuumHandler.onNeighborChange();
    }

    public EnumFacing getInputSide() {
        return getVacuumSide().getOpposite();
    }

    public EnumFacing getVacuumSide() {
        return getRotation();
    }

    @Override
    public void update() {
        if (!getWorld().isRemote && turnTimer >= 0) {
            turnTimer--;
        }
        if (!getWorld().isRemote
                && getAirHandler(getInputSide()).getPressure() > PneumaticValues.MIN_PRESSURE_VACUUM_PUMP
                && getAirHandler(getVacuumSide()).getPressure() > -1F
                && redstoneAllows()) {
            if (!getWorld().isRemote && turnTimer == -1) {
                turning = true;
            }
            getAirHandler(getVacuumSide()).addAir((int) (-PneumaticValues.PRODUCTION_VACUUM_PUMP * getSpeedMultiplierFromUpgrades())); // negative because it's pulling a vacuum.
            getAirHandler(getInputSide()).addAir((int) (-PneumaticValues.USAGE_VACUUM_PUMP * getSpeedUsageMultiplierFromUpgrades()));
            turnTimer = 40;
        }
        if (turnTimer == 0) {
            turning = false;
        }
        oldRotation = rotation;
        if (getWorld().isRemote) {
            if (turning) {
                rotationSpeed = Math.min(rotationSpeed + 1, 20);
            } else {
                rotationSpeed = Math.max(rotationSpeed - 1, 0);
            }
            rotation += rotationSpeed;
        }

        super.update();
        vacuumHandler.update();

        IAirHandler inputHandler = getAirHandler(getInputSide());
        List<Pair<EnumFacing, IAirHandler>> teList = inputHandler.getConnectedPneumatics();
        if (teList.size() == 0) inputHandler.airLeak(getInputSide());
        teList = vacuumHandler.getConnectedPneumatics();
        if (teList.size() == 0) vacuumHandler.airLeak(getVacuumSide());

    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos().getX(), getPos().getY(), getPos().getZ(), getPos().getX() + 1, getPos().getY() + 1, getPos().getZ() + 1);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        NBTTagCompound vacuum = new NBTTagCompound();
        vacuumHandler.writeToNBT(vacuum);
        tag.setTag("vacuum", vacuum);
        tag.setBoolean("turning", turning);
        tag.setInteger("redstoneMode", redstoneMode);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        vacuumHandler.readFromNBT(tag.getCompoundTag("vacuum"));
        turning = tag.getBoolean("turning");
        redstoneMode = tag.getInteger("redstoneMode");
    }

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player) {
        if (buttonID == 0) {
            redstoneMode++;
            if (redstoneMode > 2) redstoneMode = 0;
        }
    }

    @Override
    public void printManometerMessage(EntityPlayer player, List<String> curInfo) {
        curInfo.add(TextFormatting.GREEN + "Input pressure: " + PneumaticCraftUtils.roundNumberTo(getAirHandler(getInputSide()).getPressure(), 1) + " bar. Vacuum pressure: " + PneumaticCraftUtils.roundNumberTo(getAirHandler(getVacuumSide()).getPressure(), 1) + " bar.");
    }

    @Override
    public String getName() {

        return Blockss.VACUUM_PUMP.getUnlocalizedName();
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
    }

}
