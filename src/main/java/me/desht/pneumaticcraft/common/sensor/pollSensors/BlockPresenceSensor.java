package me.desht.pneumaticcraft.common.sensor.pollSensors;

import com.google.common.collect.ImmutableSet;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
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

public class BlockPresenceSensor implements IBlockAndCoordinatePollSensor {

    @Override
    public String getSensorPath() {
        return "Block/Presence";
    }

    @Override
    public Set<Item> getRequiredUpgrades() {
        return ImmutableSet.of(EnumUpgrade.BLOCK_TRACKER.getItem(), ModItems.GPS_TOOL);
    }

    @Override
    public int getPollFrequency() {
        return 2;
    }

    @Override
    public boolean needsTextBox() {
        return false;
    }

    @Override
    public List<String> getDescription() {
        List<String> text = new ArrayList<>();
        text.add(TextFormatting.BLACK + "Emits a redstone signal if there's a block (no air) at the location stored in the GPS Tool. In case of multiple locations, if any of the locations contains a block a redstone signal will be emitted.");
        return text;
    }

    @Override
    public int getRedstoneValue(World world, BlockPos pos, int sensorRange, String textBoxText, Set<BlockPos> positions) {
        for (BlockPos p : positions) {
            if (!world.isAirBlock(p)) return 15;
        }
        return 0;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawAdditionalInfo(FontRenderer fontRenderer) {
    }
}