package me.desht.pneumaticcraft.common.sensor.pollSensors;

import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.api.universal_sensor.IPollSensorSetting;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WorldRainingSensor implements IPollSensorSetting {

    @Override
    public String getSensorPath() {
        return "World/Is Raining";
    }

    @Override
    public Set<Item> getRequiredUpgrades() {
        Set<Item> upgrades = new HashSet<>();
        upgrades.add(IItemRegistry.EnumUpgrade.DISPENSER.getItem());
        return upgrades;
    }

    @Override
    public boolean needsTextBox() {
        return false;
    }

    @Override
    public List<String> getDescription() {
        List<String> text = new ArrayList<>();
        text.add(TextFormatting.BLACK + "Emits a redstone signal if it's raining in the world.");
        return text;
    }

    @Override
    public int getPollFrequency(TileEntity te) {
        return 40;
    }

    @Override
    public int getRedstoneValue(World world, BlockPos pos, int sensorRange, String textBoxText) {
        return world.isRaining() ? 15 : 0;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawAdditionalInfo(FontRenderer fontRenderer) {
    }
}
