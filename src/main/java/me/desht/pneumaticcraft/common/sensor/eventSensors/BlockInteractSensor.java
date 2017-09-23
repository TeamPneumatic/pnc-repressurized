package me.desht.pneumaticcraft.common.sensor.eventSensors;

import com.google.common.collect.ImmutableSet;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.universalSensor.IBlockAndCoordinateEventSensor;
import me.desht.pneumaticcraft.common.item.Itemss;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BlockInteractSensor implements IBlockAndCoordinateEventSensor {

    @Override
    public String getSensorPath() {
        return "Player/Right Click Block";
    }

    @Override
    public Set<Item> getRequiredUpgrades() {
        return ImmutableSet.of(Itemss.upgrades.get(EnumUpgrade.BLOCK_TRACKER), Itemss.GPS_TOOL);
    }

    @Override
    public boolean needsTextBox() {
        return false;
    }

    @Override
    public List<String> getDescription() {
        List<String> text = new ArrayList<String>();
        text.add(TextFormatting.BLACK + "Emits a redstone pulse when a player right clicks the block at the coordinate(s) selected by the GPS Tool(s) (within range).");
        return text;
    }

    @Override
    public int emitRedstoneOnEvent(Event event, TileEntity sensor, int range, Set<BlockPos> positions) {
        if (event instanceof PlayerInteractEvent) {
            PlayerInteractEvent interactEvent = (PlayerInteractEvent) event;
            return positions.contains(interactEvent.getPos()) ? 15 : 0;
        }
        return 0;
    }

    @Override
    public int getRedstonePulseLength() {
        return 5;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawAdditionalInfo(FontRenderer fontRenderer) {
    }

    @Override
    public Rectangle needsSlot() {
        return null;
    }

}
