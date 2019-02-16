package me.desht.pneumaticcraft.common.sensor.pollSensors;

import me.desht.pneumaticcraft.common.remote.GlobalVariableManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

public class WorldGlobalVariableAnalogSensor extends WorldGlobalVariableSensor {
    @Override
    public String getSensorPath() {
        return "World/Global Analog Var.";
    }

    @Override
    public List<String> getDescription() {
        return Collections.singletonList(TextFormatting.BLACK + "Emits a redstone signal of the strength of the linked global variable's X value (clamped to 0..15)");
    }

    @Override
    public int getRedstoneValue(World world, BlockPos pos, int sensorRange, String textBoxText) {
        return MathHelper.clamp(GlobalVariableManager.getInstance().getInteger(textBoxText), 0, 15);
    }
}
