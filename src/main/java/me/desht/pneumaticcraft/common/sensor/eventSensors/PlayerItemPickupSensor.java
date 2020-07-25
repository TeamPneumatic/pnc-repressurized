package me.desht.pneumaticcraft.common.sensor.eventSensors;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

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
    public int emitRedstoneOnEvent(PlayerEvent event, TileEntity sensor, int range) {
        if (event instanceof EntityItemPickupEvent) {
            return 15;
        }
        return 0;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawAdditionalInfo(MatrixStack matrixStack, FontRenderer fontRenderer) {
    }
}
