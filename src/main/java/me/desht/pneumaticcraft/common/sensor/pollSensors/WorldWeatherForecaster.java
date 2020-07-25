package me.desht.pneumaticcraft.common.sensor.pollSensors;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.universal_sensor.IPollSensorSetting;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.IServerWorldInfo;
import net.minecraft.world.storage.IWorldInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Set;

public class WorldWeatherForecaster implements IPollSensorSetting {

    @Override
    public String getSensorPath() {
        return "World/Weather Forecast";
    }

    @Override
    public Set<EnumUpgrade> getRequiredUpgrades() {
        return ImmutableSet.of(EnumUpgrade.DISPENSER);
    }

    @Override
    public boolean needsTextBox() {
        return false;
    }

    @Override
    public int getPollFrequency(TileEntity te) {
        return 40;
    }

    @Override
    public int getRedstoneValue(World world, BlockPos pos, int sensorRange, String textBoxText) {
        IWorldInfo info = world.getWorldInfo();
        if (info instanceof IServerWorldInfo) {
            return Math.max(0, 15 - ((IServerWorldInfo) info).getRainTime() / 1200);
        } else {
            return 0;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawAdditionalInfo(MatrixStack matrixStack, FontRenderer fontRenderer) {
    }
}
