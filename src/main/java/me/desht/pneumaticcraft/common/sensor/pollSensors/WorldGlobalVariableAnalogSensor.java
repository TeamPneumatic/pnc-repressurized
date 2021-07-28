package me.desht.pneumaticcraft.common.sensor.pollSensors;

import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
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
        if (playerID == null && !GlobalVariableHelper.hasPrefix(textBoxText)) {
            // TODO legacy - assume server-global - remove in 1.17
            textBoxText = "%" + textBoxText;
        }
        return MathHelper.clamp(GlobalVariableHelper.getInt(playerID, textBoxText), 0, 15);
    }
}
