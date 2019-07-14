package me.desht.pneumaticcraft.common.sensor.pollSensors;

import com.google.common.collect.ImmutableSet;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.api.universal_sensor.IPollSensorSetting;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.Item;
import net.minecraft.server.management.PlayerList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WorldPlayersInServerSensor implements IPollSensorSetting {

    @Override
    public String getSensorPath() {
        return "World/Players in server";
    }

    @Override
    public Set<Item> getRequiredUpgrades() {
        return ImmutableSet.of(IItemRegistry.EnumUpgrade.DISPENSER.getItem());
    }

    @Override
    public boolean needsTextBox() {
        return true;
    }

    @Override
    public List<String> getDescription() {
        List<String> text = new ArrayList<>();
        text.add(TextFormatting.BLACK + "Emits a redstone level for every player logged into the server.");
        text.add(TextFormatting.BLACK + "When you fill in a specific player name, the Universal Sensor will emit a redstone signal of 15 if the player is online and 0 otherwise.");
        return text;
    }

    @Override
    public int getPollFrequency(TileEntity te) {
        return 40;
    }

    @Override
    public int getRedstoneValue(World world, BlockPos pos, int sensorRange, String textBoxText) {
        PlayerList playerList = ServerLifecycleHooks.getCurrentServer().getPlayerList();
        if (textBoxText.equals("")) {
            return Math.min(15, playerList.getCurrentPlayerCount());
        } else {
            for (String userName : playerList.getOnlinePlayerNames()) {
                if (userName.equalsIgnoreCase(textBoxText)) return 15;
            }
            return 0;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawAdditionalInfo(FontRenderer fontRenderer) {
        fontRenderer.drawString("Player Name", 70, 48, 0x404040);
    }
}
