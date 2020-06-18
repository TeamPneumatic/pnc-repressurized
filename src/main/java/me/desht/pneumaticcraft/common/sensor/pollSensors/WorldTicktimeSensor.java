package me.desht.pneumaticcraft.common.sensor.pollSensors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.universal_sensor.IPollSensorSetting;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class WorldTicktimeSensor implements IPollSensorSetting {

    @Override
    public String getSensorPath() {
        return "World/Tick time (lag)";
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
        return ImmutableList.of("pneumaticcraft.gui.universalSensor.desc.world_tick_time");
    }

    @Override
    public int getPollFrequency(TileEntity te) {
        return 40;
    }

    @Override
    public int getRedstoneValue(World world, BlockPos pos, int sensorRange, String textBoxText) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        double worldTickTime = mean(server.getTickTime(world.dimension.getType())) * 1.0E-6D;
        try {
            int redstoneStrength = (int) (worldTickTime * Double.parseDouble(textBoxText));
            return Math.min(15, redstoneStrength);
        } catch (Exception e) {
            return 0;
        }
    }

    private static long mean(long[] values) {
        long sum = Arrays.stream(values).sum();
        return sum / values.length;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawAdditionalInfo(FontRenderer fontRenderer) {
        fontRenderer.drawString("Tick Resolution", 70, 48, 0x404040);
    }
}
