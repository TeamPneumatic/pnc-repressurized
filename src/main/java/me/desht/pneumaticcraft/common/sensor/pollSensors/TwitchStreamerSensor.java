package me.desht.pneumaticcraft.common.sensor.pollSensors;

import com.google.common.collect.ImmutableSet;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.universalSensor.IPollSensorSetting;
import me.desht.pneumaticcraft.common.item.Itemss;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.lwjgl.util.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TwitchStreamerSensor implements IPollSensorSetting {

    @Override
    public String getSensorPath() {
        return "World/Twitch";
    }

    @Override
    public Set<Item> getRequiredUpgrades() {
        return ImmutableSet.of(Itemss.upgrades.get(EnumUpgrade.DISPENSER));
    }

    @Override
    public boolean needsTextBox() {
        return true;
    }

    @Override
    public void drawAdditionalInfo(FontRenderer fontRenderer) {

    }

    @Override
    public List<String> getDescription() {
        List<String> info = new ArrayList<String>();
        info.add(TextFormatting.BLACK + "Emits a redstone signal when the name of the streamer typed in is streaming at this moment.");
        return info;
    }

    @Override
    public Rectangle needsSlot() {
        return null;
    }

    @Override
    public int getPollFrequency(TileEntity te) {
        return 20;
    }

    @Override
    public int getRedstoneValue(World world, BlockPos pos, int sensorRange, String textBoxText) {
        return TwitchStream.isOnline(textBoxText) ? 15 : 0;
    }

}
