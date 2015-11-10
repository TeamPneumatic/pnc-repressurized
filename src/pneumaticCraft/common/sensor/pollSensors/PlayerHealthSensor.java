package pneumaticCraft.common.sensor.pollSensors;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import org.lwjgl.util.Rectangle;

import pneumaticCraft.api.universalSensor.IPollSensorSetting;

public class PlayerHealthSensor implements IPollSensorSetting{

    @Override
    public String getSensorPath(){
        return "entityTracker/Player/Player Health";
    }

    @Override
    public boolean needsTextBox(){
        return true;
    }

    @Override
    public void drawAdditionalInfo(FontRenderer fontRenderer){

    }

    @Override
    public List<String> getDescription(){
        List<String> text = new ArrayList<String>();
        text.add("gui.universalSensor.desc.playerHealth");
        return text;
    }

    @Override
    public Rectangle needsSlot(){
        return null;
    }

    @Override
    public int getPollFrequency(TileEntity te){
        return 10;
    }

    @Override
    public int getRedstoneValue(World world, int x, int y, int z, int sensorRange, String textBoxText){
        EntityPlayer player = MinecraftServer.getServer().getConfigurationManager().func_152612_a(textBoxText);
        if(player != null) {
            return (int)(15 * player.getHealth() / player.getMaxHealth());
        } else {
            return 0;
        }
    }

}
