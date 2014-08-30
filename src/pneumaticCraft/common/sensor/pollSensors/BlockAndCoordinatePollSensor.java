package pneumaticCraft.common.sensor.pollSensors;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import pneumaticCraft.api.universalSensor.IPollSensorSetting;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.tileentity.TileEntityUniversalSensor;

public abstract class BlockAndCoordinatePollSensor implements IPollSensorSetting{
    @Override
    public String getSensorPath(){
        return "blockTracker_gpsTool";
    }

    @Override
    public int getRedstoneValue(World world, int x, int y, int z, int sensorRange, String textBoxText){
        TileEntity te = world.getTileEntity(x, y, z);
        if(te instanceof TileEntityUniversalSensor) {
            TileEntityUniversalSensor teUs = (TileEntityUniversalSensor)te;

            for(int i = TileEntityUniversalSensor.UPGRADE_SLOT_1; i <= TileEntityUniversalSensor.UPGRADE_SLOT_4; i++) {
                if(teUs.getStackInSlot(i) != null && teUs.getStackInSlot(i).getItem() == Itemss.GPSTool && teUs.getStackInSlot(i).hasTagCompound()) {
                    NBTTagCompound gpsTag = teUs.getStackInSlot(i).getTagCompound();
                    int toolX = gpsTag.getInteger("x");
                    int toolY = gpsTag.getInteger("y");
                    int toolZ = gpsTag.getInteger("z");
                    if(Math.abs(toolX - x) <= sensorRange && Math.abs(toolY - y) <= sensorRange && Math.abs(toolZ - z) <= sensorRange) {
                        return getRedstoneValue(world, x, y, z, sensorRange, textBoxText, toolX, toolY, toolZ);
                    }
                }
            }
        }
        return 0;
    }

    public abstract int getRedstoneValue(World world, int x, int y, int z, int sensorRange, String textBoxText, int toolX, int toolY, int toolZ);

}
