package me.desht.pneumaticcraft.common.sensor.pollSensors;

import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.universalSensor.IPollSensorSetting;
import me.desht.pneumaticcraft.common.item.Itemss;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.Rectangle;

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
        Set<Item> upgrades = new HashSet<Item>();
        upgrades.add(Itemss.upgrades.get(EnumUpgrade.DISPENSER));
        return upgrades;
    }

    @Override
    public boolean needsTextBox() {
        return false;
    }

    @Override
    public List<String> getDescription() {
        List<String> text = new ArrayList<String>();
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
    @SideOnly(Side.CLIENT)
    public void drawAdditionalInfo(FontRenderer fontRenderer) {
    }

    @Override
    public Rectangle needsSlot() {
        return null;
    }

}
