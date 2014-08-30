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

public class BlockRedstoneSensor implements IBlockAndCoordinatePollSensor{

    @Override
    public String getSensorPath(){
        return "blockTracker_gpsTool/Block/Redstone";
    }

    @Override
    public int getPollFrequency(){
        return 2;
    }

    @Override
    public boolean needsTextBox(){
        return false;
    }

    @Override
    public List<String> getDescription(){
        List<String> text = new ArrayList<String>();
        text.add(EnumChatFormatting.BLACK + "This sensor could be used as a wireless redstone device, as it emits the same redstone signal as being applied at the GPS Tool's saved location.");
        return text;
    }

    @Override
    public int getRedstoneValue(World world, int x, int y, int z, int sensorRange, String textBoxText, int toolX, int toolY, int toolZ){
        // return world.getBlockPowerInput(toolX, toolY, toolZ);
        int redstonePower = world.getStrongestIndirectPower(toolX, toolY, toolZ);
        Block block = world.getBlock(toolX, toolY, toolZ);
        if(block != null) {
            for(int i = 0; i < 6 && redstonePower < 15; i++)
                redstonePower = Math.max(redstonePower, world.getIndirectPowerLevelTo(toolX, toolY, toolZ, i));
        }
        return redstonePower;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawAdditionalInfo(FontRenderer fontRenderer){}

    @Override
    public Rectangle needsSlot(){
        return null;
    }

}