package me.desht.pneumaticcraft.common.sensor.pollSensors;

import com.google.common.collect.ImmutableSet;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.universal_sensor.IPollSensorSetting;
import net.minecraft.server.management.PlayerList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.List;
import java.util.Set;

public class WorldPlayersInServerSensor implements IPollSensorSetting {

    @Override
    public String getSensorPath() {
        return "World/Players in server";
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
    public int getPollFrequency(TileEntity te) {
        return 40;
    }

    @Override
    public int getRedstoneValue(World world, BlockPos pos, int sensorRange, String textBoxText) {
        PlayerList playerList = ServerLifecycleHooks.getCurrentServer().getPlayerList();
        if (textBoxText.isEmpty()) {
            return Math.min(15, playerList.getCurrentPlayerCount());
        } else {
            for (String userName : playerList.getOnlinePlayerNames()) {
                if (userName.equalsIgnoreCase(textBoxText)) return 15;
            }
            return 0;
        }
    }

    @Override
    public void getAdditionalInfo(List<ITextComponent> info) {
        info.add(new StringTextComponent("Player Name"));
    }
}
