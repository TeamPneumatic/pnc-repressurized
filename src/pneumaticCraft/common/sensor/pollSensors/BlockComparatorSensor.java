package pneumaticCraft.common.sensor.pollSensors;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.ChunkPosition;
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
        text.add(EnumChatFormatting.BLACK + "This sensor setting simulates the Redstone Comparator at the location(s) stored in the GPS Tool(s). This means that for example the redstone signal is proportional to the contents of inventories stored at the GPS Tool's coordinate. If the comparator output would be side dependant, the highest signal will be emitted. Also in case of multiple positions, the positions with the highest comparator value will be emitted.");
        return text;
    }

    @Override
    public int getRedstoneValue(World world, int x, int y, int z, int sensorRange, String textBoxText, Set<ChunkPosition> positions){
        int maxStrength = 0;
        for(ChunkPosition pos : positions) {
            Block block = world.getBlock(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
            if(block.hasComparatorInputOverride()) {
                for(int i = 0; i < 6; i++) {
                    maxStrength = Math.max(maxStrength, block.getComparatorInputOverride(world, pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ, i));
                }
            }
        }
        return maxStrength;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawAdditionalInfo(FontRenderer fontRenderer){}

    @Override
    public Rectangle needsSlot(){
        return null;
    }

}