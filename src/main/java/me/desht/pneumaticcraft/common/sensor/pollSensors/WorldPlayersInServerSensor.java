package me.desht.pneumaticcraft.common.sensor.pollSensors;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.universal_sensor.IPollSensorSetting;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

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
    public void drawAdditionalInfo(MatrixStack matrixStack, FontRenderer fontRenderer) {
        fontRenderer.drawString(matrixStack, "Player Name", 70, 48, 0x404040);
    }
}
