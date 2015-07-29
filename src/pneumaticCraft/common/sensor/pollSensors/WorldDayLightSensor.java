package pneumaticCraft.common.sensor.pollSensors;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

import org.lwjgl.util.Rectangle;

import pneumaticCraft.api.universalSensor.IPollSensorSetting;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class WorldDayLightSensor implements IPollSensorSetting{

    @Override
    public String getSensorPath(){
        return "dispenser/World/Daylight";
    }

    @Override
    public boolean needsTextBox(){
        return false;
    }

    @Override
    public List<String> getDescription(){
        List<String> text = new ArrayList<String>();
        text.add(EnumChatFormatting.BLACK + "A straight copy of the Daylight Sensor.");
        return text;
    }

    @Override
    public int getPollFrequency(TileEntity te){
        return 40;
    }

    @Override
    public int getRedstoneValue(World world, int x, int y, int z, int sensorRange, String textBoxText){
        return updateLightLevel(world, x, y, z);
    }

    private int updateLightLevel(World par1World, int par2, int par3, int par4){
        if(!par1World.provider.hasNoSky) {
            int i1 = par1World.getSavedLightValue(EnumSkyBlock.Sky, par2, par3, par4) - par1World.skylightSubtracted;
            float f = par1World.getCelestialAngleRadians(1.0F);

            if(f < (float)Math.PI) {
                f += (0.0F - f) * 0.2F;
            } else {
                f += ((float)Math.PI * 2F - f) * 0.2F;
            }

            i1 = Math.round(i1 * MathHelper.cos(f));

            if(i1 < 0) {
                i1 = 0;
            }

            if(i1 > 15) {
                i1 = 15;
            }

            return i1;
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
