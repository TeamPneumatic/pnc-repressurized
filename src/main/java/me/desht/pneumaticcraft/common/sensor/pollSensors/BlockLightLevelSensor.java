package me.desht.pneumaticcraft.common.sensor.pollSensors;

import com.google.common.collect.ImmutableSet;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.api.universal_sensor.IBlockAndCoordinatePollSensor;
import me.desht.pneumaticcraft.common.core.ModItems;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BlockLightLevelSensor implements IBlockAndCoordinatePollSensor {

    @Override
    public String getSensorPath() {
        return "Block/Light Level";
    }

    @Override
    public Set<Item> getRequiredUpgrades() {
        return ImmutableSet.of(IItemRegistry.EnumUpgrade.BLOCK_TRACKER.getItem(), ModItems.GPS_TOOL);
    }

    @Override
    public int getPollFrequency() {
        return 5;
    }

    @Override
    public boolean needsTextBox() {
        return false;
    }

    @Override
    public List<String> getDescription() {
        List<String> text = new ArrayList<>();
        text.add(TextFormatting.BLACK + "Emits a redstone of which the strength is equal to the light level at the location stored in the GPS Tool. In case of multiple locations, the location with the highest light value is used.");
        return text;
    }

    @Override
    public int getRedstoneValue(World world, BlockPos pos, int sensorRange, String textBoxText, Set<BlockPos> positions) {
        int lightValue = 0;
        for (BlockPos p : positions) {
            lightValue = Math.max(lightValue, world.getLight(p));
        }
        return lightValue;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawAdditionalInfo(FontRenderer fontRenderer) {
    }
}