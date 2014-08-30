package pneumaticCraft.common.sensor.pollSensors;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import org.lwjgl.util.Rectangle;

import pneumaticCraft.api.universalSensor.IBlockAndCoordinatePollSensor;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockComparatorSensor implements IBlockAndCoordinatePollSensor{

    @Override
    public String getSensorPath(){
        return "blockTracker_gpsTool/Block/Comparator";
    }

    @Override
    public int getPollFrequency(){
        return 5;
    }

    @Override
    public boolean needsTextBox(){
        return false;
    }

    @Override
    public List<String> getDescription(){
        List<String> text = new ArrayList<String>();
        text.add(EnumChatFormatting.BLACK + "This sensor setting simulates the Redstone Comparator at the location stored in the GPS Tool. This means that for example the redstone signal is proportional to the contents of inventories stored at the GPS Tool's coordinate. If the comparator output would be side dependant, the highest signal will be emitted.");
        return text;
    }

    @Override
    public int getRedstoneValue(World world, int x, int y, int z, int sensorRange, String textBoxText, int toolX, int toolY, int toolZ){
        Block block = world.getBlock(toolX, toolY, toolZ);
        if(block.hasComparatorInputOverride()) {
            int maxStrength = 0;
            for(int i = 0; i < 6; i++) {
                maxStrength = Math.max(maxStrength, block.getComparatorInputOverride(world, toolX, toolY, toolZ, i));
            }
            return maxStrength;
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