package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class TileEntityElectrostaticCompressor extends TileEntityPneumaticBase implements IRedstoneControl {

    private boolean lastRedstoneState;
    @GuiSynced
    public int redstoneMode = 0;
    public int ironBarsBeneath = 0;
    private int struckByLightningCooldown; //used by the redstone.

    public TileEntityElectrostaticCompressor() {
        super(PneumaticValues.DANGER_PRESSURE_ELECTROSTATIC_COMPRESSOR, PneumaticValues.MAX_PRESSURE_ELECTROSTATIC_COMPRESSOR, PneumaticValues.VOLUME_ELECTROSTATIC_COMPRESSOR, 4);
    }

    @Override
    public void update() {
        /*
         * Most of the Electrostatic Compressor's logic can be found in TickHandlerPneumaticCraft#handleElectrostaticGeneration().
         */
        if (getWorld().getTotalWorldTime() % 40 == 0) {
            for (ironBarsBeneath = 0; ironBarsBeneath < 128; ironBarsBeneath++) {
                if (getWorld().getBlockState(getPos().offset(EnumFacing.DOWN, ironBarsBeneath + 1)).getBlock() != Blocks.IRON_BARS) {
                    break;
                }
            }
        }
        super.update();
        if (!getWorld().isRemote) {
            if (lastRedstoneState != shouldEmitRedstone()) {
                lastRedstoneState = !lastRedstoneState;
                updateNeighbours();
            }
            struckByLightningCooldown--;
        }

    }

    @Override
    public boolean isConnectedTo(EnumFacing dir) {
        return dir != EnumFacing.UP;
    }

    private boolean shouldEmitRedstone() {
        switch (redstoneMode) {
            case 0:
                return false;
            case 1:
                return struckByLightningCooldown > 0;
        }
        return false;
    }

    public void onStruckByLightning() {
        struckByLightningCooldown = 10;
        if (getPressure() > PneumaticValues.DANGER_PRESSURE_ELECTROSTATIC_COMPRESSOR) {
            int maxRedirection = PneumaticValues.MAX_REDIRECTION_PER_IRON_BAR * ironBarsBeneath;
            int tooMuchAir = (int) ((getPressure() - PneumaticValues.DANGER_PRESSURE_ELECTROSTATIC_COMPRESSOR) * getAirHandler(null).getVolume());
            addAir(-Math.min(maxRedirection, tooMuchAir));
        }
    }

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player) {
        if (buttonID == 0) {
            redstoneMode++;
            if (redstoneMode > 1) redstoneMode = 0;
        }
    }

    @Override
    public String getName() {
        return Blockss.ELECTROSTATIC_COMPRESSOR.getUnlocalizedName();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        super.readFromNBT(nbtTagCompound);
        redstoneMode = nbtTagCompound.getInteger("redstoneMode");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound) {
        super.writeToNBT(nbtTagCompound);
        nbtTagCompound.setInteger("redstoneMode", redstoneMode);
        return nbtTagCompound;
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
    }
}
