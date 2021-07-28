package me.desht.pneumaticcraft.common.sensor.pollSensors;

import com.google.common.collect.ImmutableSet;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.universal_sensor.IPollSensorSetting;
import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class WorldGlobalVariableSensor implements IPollSensorSetting {
    protected UUID playerID;

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
        if (playerID == null && !GlobalVariableHelper.hasPrefix(textBoxText)) {
            // TODO legacy - assume server-global - remove in 1.17
            textBoxText = "%" + textBoxText;
        }
        return GlobalVariableHelper.getBool(playerID, textBoxText) ? 15 : 0;
    }

    @Override
    public void getAdditionalInfo(List<ITextComponent> info) {
        info.add(new TranslationTextComponent("pneumaticcraft.gui.progWidget.coordinate.variableName"));
    }

    @Override
    public void setPlayerContext(UUID playerID) {
        this.playerID = playerID;
    }
}
