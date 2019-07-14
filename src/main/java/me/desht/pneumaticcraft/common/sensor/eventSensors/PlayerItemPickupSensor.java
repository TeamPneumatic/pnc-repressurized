package me.desht.pneumaticcraft.common.sensor.eventSensors;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;
import java.util.List;

public class PlayerItemPickupSensor extends PlayerEventSensor {

    @Override
    public String getSensorPath() {
        return super.getSensorPath() + "/Item Pickup";
    }

    @Override
    public boolean needsTextBox() {
        return false;
    }

    @Override
    public List<String> getDescription() {
        List<String> text = new ArrayList<>();
        text.add(TextFormatting.BLACK + "Emits a redstone pulse when a player picks up an item off the ground within range.");
        return text;
    }

    @Override
    public int emitRedstoneOnEvent(PlayerEvent event, TileEntity sensor, int range) {
        if (event instanceof EntityItemPickupEvent) {
            return 15;
        }
        return 0;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawAdditionalInfo(FontRenderer fontRenderer) {
    }
}
