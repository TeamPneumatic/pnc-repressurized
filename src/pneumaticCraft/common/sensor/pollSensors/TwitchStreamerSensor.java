package pneumaticCraft.common.sensor.pollSensors;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import org.lwjgl.util.Rectangle;

import pneumaticCraft.api.universalSensor.IPollSensorSetting;

public class TwitchStreamerSensor implements IPollSensorSetting{

    @Override
    public String getSensorPath(){
        return "dispenser/World/Twitch";
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
        List<String> info = new ArrayList<String>();
        info.add(EnumChatFormatting.BLACK + "Emits a redstone signal when the name of the streamer typed in is streaming at this moment.");
        return info;
    }

    @Override
    public Rectangle needsSlot(){
        return null;
    }

    @Override
    public int getPollFrequency(TileEntity te){
        return 20;
    }

    @Override
    public int getRedstoneValue(World world, int x, int y, int z, int sensorRange, String textBoxText){
        return TwitchStream.isOnline(textBoxText) ? 15 : 0;
    }

}
