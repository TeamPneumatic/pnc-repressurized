package pneumaticCraft.common.sensor.eventSensors;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import org.lwjgl.util.Rectangle;

import pneumaticCraft.api.universalSensor.PlayerEventSensor;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PlayerAttackSensor extends PlayerEventSensor{

    @Override
    public String getSensorPath(){
        return super.getSensorPath() + "/Player Attack";
    }

    @Override
    public boolean needsTextBox(){
        return false;
    }

    @Override
    public List<String> getDescription(){
        List<String> text = new ArrayList<String>();
        text.add(EnumChatFormatting.BLACK + "Emits a redstone pulse when a player attacks an entity within range of the sensor.");
        return text;
    }

    @Override
    public int emitRedstoneOnEvent(PlayerEvent event, TileEntity sensor, int range){
        if(event instanceof AttackEntityEvent) {
            return 15;
        }
        return 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawAdditionalInfo(FontRenderer fontRenderer){}

    @Override
    public Rectangle needsSlot(){
        return null;
    }
}
