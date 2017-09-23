package me.desht.pneumaticcraft.common.sensor.pollSensors;

import com.google.common.collect.ImmutableSet;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.universalSensor.IBlockAndCoordinatePollSensor;
import me.desht.pneumaticcraft.common.item.Itemss;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BlockComparatorSensor implements IBlockAndCoordinatePollSensor {

    @Override
    public String getSensorPath() {
        return "Block/Comparator";
    }

    @Override
    public Set<Item> getRequiredUpgrades() {
        return ImmutableSet.of(Itemss.upgrades.get(EnumUpgrade.BLOCK_TRACKER), Itemss.GPS_TOOL);
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
        List<String> text = new ArrayList<String>();
        text.add(TextFormatting.BLACK + "This sensor setting simulates the Redstone Comparator at the location(s) stored in the GPS Tool(s). This means that for example the redstone signal is proportional to the contents of inventories stored at the GPS Tool's coordinate. If the comparator output would be side dependant, the highest signal will be emitted. Also in case of multiple positions, the positions with the highest comparator value will be emitted.");
        return text;
    }

    @Override
    public int getRedstoneValue(World world, BlockPos pos, int sensorRange, String textBoxText, Set<BlockPos> positions) {
        int maxStrength = 0;
        for (BlockPos p : positions) {
            IBlockState state = world.getBlockState(p);
            if (state.hasComparatorInputOverride()) {
                maxStrength = Math.max(maxStrength, state.getComparatorInputOverride(world, p));
            }
        }
        return maxStrength;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawAdditionalInfo(FontRenderer fontRenderer) {
    }

    @Override
    public Rectangle needsSlot() {
        return null;
    }

}