package me.desht.pneumaticcraft.common.sensor.pollSensors;

import me.desht.pneumaticcraft.api.universalSensor.IPollSensorSetting;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.tileentity.TileEntityUniversalSensor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class BlockAndCoordinatePollSensor implements IPollSensorSetting {
    @Override
    public String getSensorPath() {
        return "blockTracker_gpsTool";
    }

    @Override
    public int getRedstoneValue(World world, BlockPos pos, int sensorRange, String textBoxText) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityUniversalSensor) {
            TileEntityUniversalSensor teUs = (TileEntityUniversalSensor) te;

            for (int i = 0; i < teUs.getUpgradesInventory().getSlots(); i++) {
                ItemStack stack = teUs.getUpgradesInventory().getStackInSlot(i);
                if (stack.getItem() == Itemss.GPS_TOOL && stack.hasTagCompound()) {
                    NBTTagCompound gpsTag = stack.getTagCompound();
                    int toolX = gpsTag.getInteger("x");
                    int toolY = gpsTag.getInteger("y");
                    int toolZ = gpsTag.getInteger("z");
                    if (Math.abs(toolX - pos.getX()) <= sensorRange && Math.abs(toolY - pos.getY()) <= sensorRange && Math.abs(toolZ - pos.getZ()) <= sensorRange) {
                        return getRedstoneValue(world, pos, sensorRange, textBoxText, new BlockPos(toolX, toolY, toolZ));
                    }
                }
            }
        }
        return 0;
    }

    public abstract int getRedstoneValue(World world, BlockPos pos, int sensorRange, String textBoxText, BlockPos toolPos);

}
