package me.desht.pneumaticcraft.common.sensor.pollSensors;

import com.google.common.collect.ImmutableSet;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.universalSensor.IBlockAndCoordinatePollSensor;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
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

public class BlockRedstoneSensor implements IBlockAndCoordinatePollSensor {

    @Override
    public String getSensorPath() {
        return "Block/Redstone";
    }

    @Override
    public Set<Item> getRequiredUpgrades() {
        return ImmutableSet.of(Itemss.upgrades.get(EnumUpgrade.BLOCK_TRACKER), Itemss.GPS_TOOL);
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
        List<String> text = new ArrayList<String>();
        text.add(TextFormatting.BLACK + "This sensor could be used as a wireless redstone device, as it emits the same redstone signal as being applied at the GPS Tool's saved location. In case of multiple locations, the location with the highest redstone value is emitted.");
        return text;
    }

    @Override
    public int getRedstoneValue(World world, BlockPos pos, int sensorRange, String textBoxText, Set<BlockPos> positions) {
        int redstonePower = 0;
        for (BlockPos p : positions) {
            redstonePower = Math.max(redstonePower, PneumaticCraftUtils.getRedstoneLevel(world, p));
            if (redstonePower == 15) return 15;
        }
        return redstonePower;
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