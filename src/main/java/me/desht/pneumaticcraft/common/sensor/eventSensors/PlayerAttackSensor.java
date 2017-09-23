package me.desht.pneumaticcraft.common.sensor.eventSensors;

import me.desht.pneumaticcraft.api.universalSensor.PlayerEventSensor;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class PlayerAttackSensor extends PlayerEventSensor {

    @Override
    public String getSensorPath() {
        return super.getSensorPath() + "/Player Attack";
    }

    @Override
    public boolean needsTextBox() {
        return false;
    }

    @Override
    public List<String> getDescription() {
        List<String> text = new ArrayList<String>();
        text.add(TextFormatting.BLACK + "Emits a redstone pulse when a player attacks an entity within range of the sensor.");
        return text;
    }

    @Override
    public int emitRedstoneOnEvent(PlayerEvent event, TileEntity sensor, int range) {
        if (event instanceof AttackEntityEvent) {
            return 15;
        }
        return 0;
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
