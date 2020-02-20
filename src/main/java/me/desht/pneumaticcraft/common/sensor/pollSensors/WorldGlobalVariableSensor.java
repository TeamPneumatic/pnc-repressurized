package me.desht.pneumaticcraft.common.sensor.pollSensors;

import com.google.common.collect.ImmutableSet;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.universal_sensor.IPollSensorSetting;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Collections;
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
    public List<String> getDescription() {
        return Collections.singletonList(TextFormatting.BLACK + "Emits a full-strength redstone signal when the linked global variable has any non-zero X value");
    }

    @Override
    public int getPollFrequency(TileEntity te) {
        return 1;
    }

    @Override
    public int getRedstoneValue(World world, BlockPos pos, int sensorRange, String textBoxText) {
        return GlobalVariableManager.getInstance().getBoolean(textBoxText) ? 15 : 0;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawAdditionalInfo(FontRenderer fontRenderer) {
        fontRenderer.drawString("Variable Name", 70, 48, 0x404040);
    }
}
