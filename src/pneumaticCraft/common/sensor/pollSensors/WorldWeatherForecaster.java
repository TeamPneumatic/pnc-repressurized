package pneumaticCraft.common.sensor.pollSensors;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import org.lwjgl.util.Rectangle;

import pneumaticCraft.api.universalSensor.IPollSensorSetting;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class WorldWeatherForecaster implements IPollSensorSetting{

    @Override
    public String getSensorPath(){
        return "dispenser/World/Weather Forecast";
    }

    @Override
    public boolean needsTextBox(){
        return false;
    }

    @Override
    public List<String> getDescription(){
        List<String> text = new ArrayList<String>();
        text.add(EnumChatFormatting.BLACK + "Emits a redstone signal of which the strength gets higher how closer the rain gets. The strenght increases by one for every minute.");
        text.add(EnumChatFormatting.RED + "strength = 15 - time till rain (min)");
        text.add(EnumChatFormatting.GREEN + "Example: If it will rain in 10 minutes, the strength is 5.");
        return text;
    }

    @Override
    public int getPollFrequency(TileEntity te){
        return 40;
    }

    @Override
    public int getRedstoneValue(World world, int x, int y, int z, int sensorRange, String textBoxText){
        return Math.max(0, 15 - world.getWorldInfo().getRainTime() / 1200);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawAdditionalInfo(FontRenderer fontRenderer){}

    @Override
    public Rectangle needsSlot(){
        return null;
    }
}
