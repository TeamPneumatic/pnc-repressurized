package pneumaticCraft.common.sensor.eventSensors;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.ChunkPosition;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import org.lwjgl.util.Rectangle;

import pneumaticCraft.api.universalSensor.IBlockAndCoordinateEventSensor;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockInteractSensor implements IBlockAndCoordinateEventSensor{

    @Override
    public String getSensorPath(){
        return "blockTracker_gpsTool/Player/Right Click Block";
    }

    @Override
    public boolean needsTextBox(){
        return false;
    }

    @Override
    public List<String> getDescription(){
        List<String> text = new ArrayList<String>();
        text.add(EnumChatFormatting.BLACK + "Emits a redstone pulse when a player right clicks the block at the coordinate(s) selected by the GPS Tool(s) (within range).");
        return text;
    }

    @Override
    public int emitRedstoneOnEvent(Event event, TileEntity sensor, int range, Set<ChunkPosition> positions){
        if(event instanceof PlayerInteractEvent) {
            PlayerInteractEvent interactEvent = (PlayerInteractEvent)event;
            return positions.contains(new ChunkPosition(interactEvent.x, interactEvent.y, interactEvent.z)) ? 15 : 0;
        }
        return 0;
    }

    @Override
    public int getRedstonePulseLength(){
        return 5;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawAdditionalInfo(FontRenderer fontRenderer){}

    @Override
    public Rectangle needsSlot(){
        return null;
    }

}
