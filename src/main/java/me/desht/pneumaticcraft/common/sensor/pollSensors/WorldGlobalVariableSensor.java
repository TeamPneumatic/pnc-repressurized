package me.desht.pneumaticcraft.common.sensor.pollSensors;

import com.google.common.collect.ImmutableSet;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.universal_sensor.IPollSensorSetting;
import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import java.util.List;
import java.util.Set;

public class WorldGlobalVariableSensor implements IPollSensorSetting {

    @Override
    public String getSensorPath() {
        return "World/Global variable";
    }

    @Override
    public Set<EnumUpgrade> getRequiredUpgrades() {
        return ImmutableSet.of(EnumUpgrade.DISPENSER);
    }

    @Override
    public boolean needsTextBox() {
        return true;
    }

    @Override
    public int getPollFrequency(TileEntity te) {
        return 1;
    }

    @Override
    public int getRedstoneValue(World world, BlockPos pos, int sensorRange, String textBoxText) {
        // TODO player-global
        return GlobalVariableHelper.getBool(null, "%" + textBoxText) ? 15 : 0;
//        return GlobalVariableManager.getInstance().getBoolean(textBoxText) ? 15 : 0;
    }

    @Override
    public void getAdditionalInfo(List<ITextComponent> info) {
        info.add(new StringTextComponent("Variable Name"));
    }
}
