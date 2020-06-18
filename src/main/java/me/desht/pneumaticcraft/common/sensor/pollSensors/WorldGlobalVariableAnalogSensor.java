package me.desht.pneumaticcraft.common.sensor.pollSensors;

import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class WorldGlobalVariableAnalogSensor extends WorldGlobalVariableSensor {
    @Override
    public String getSensorPath() {
        return "World/Global Analog Var.";
    }

    @Override
    public int getRedstoneValue(World world, BlockPos pos, int sensorRange, String textBoxText) {
        return MathHelper.clamp(GlobalVariableManager.getInstance().getInteger(textBoxText), 0, 15);
    }
}
