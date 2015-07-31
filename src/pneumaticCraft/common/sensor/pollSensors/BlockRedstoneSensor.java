package pneumaticCraft.common.sensor.pollSensors;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.ChunkPosition;
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
        text.add(EnumChatFormatting.BLACK + "This sensor could be used as a wireless redstone device, as it emits the same redstone signal as being applied at the GPS Tool's saved location. In case of multiple locations, the location with the highest redstone value is emitted.");
        return text;
    }

    @Override
    public int getRedstoneValue(World world, int x, int y, int z, int sensorRange, String textBoxText, Set<ChunkPosition> positions){
        int redstonePower = 0;
        for(ChunkPosition pos : positions) {
            redstonePower = Math.max(redstonePower, world.getStrongestIndirectPower(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ));
            for(int i = 0; i < 6 && redstonePower < 15; i++)
                redstonePower = Math.max(redstonePower, world.getIndirectPowerLevelTo(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ, i));
            if(redstonePower == 15) return 15;
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