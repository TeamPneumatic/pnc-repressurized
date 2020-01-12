package me.desht.pneumaticcraft.common.sensor.pollSensors;

import me.desht.pneumaticcraft.api.universal_sensor.IPollSensorSetting;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ItemGPSTool;
import me.desht.pneumaticcraft.common.tileentity.TileEntityUniversalSensor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
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

            for (int i = 0; i < teUs.getUpgradeHandler().getSlots(); i++) {
                ItemStack stack = teUs.getUpgradeHandler().getStackInSlot(i);
                if (stack.getItem() == ModItems.GPS_TOOL.get() && stack.hasTag()) {
                    BlockPos gpsPos = ItemGPSTool.getGPSLocation(stack);
                    if (gpsPos != null) {
                        AxisAlignedBB aabb = new AxisAlignedBB(gpsPos).grow(sensorRange);
                        if (aabb.contains(pos.getX(), pos.getY(), pos.getZ())) {
                            return getRedstoneValue(world, pos, sensorRange, textBoxText, gpsPos);
                        }
                    }
                }
            }
        }
        return 0;
    }

    public abstract int getRedstoneValue(World world, BlockPos pos, int sensorRange, String textBoxText, BlockPos toolPos);

}
