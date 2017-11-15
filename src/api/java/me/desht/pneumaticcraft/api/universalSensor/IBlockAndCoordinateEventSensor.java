package me.desht.pneumaticcraft.api.universalSensor;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.lwjgl.util.Rectangle;

import java.util.List;
import java.util.Set;

public interface IBlockAndCoordinateEventSensor {
    /**
     * See {@link ISensorSetting#getSensorPath()}
     *
     * @return
     */
    String getSensorPath();

    /**
     * See {@link ISensorSetting#getRequiredUpgrades()}
     *
     * @return
     */
    Set<Item> getRequiredUpgrades();

    /**
     * Extended version of the normal emitRedstoneOnEvent. This method will only invoke with a valid GPS tool, and when all the coordinates are within range.
     *
     * @param event
     * @param sensor
     * @param range
     * @param positions When only one GPS Tool is inserted this contains the position of just that tool. If two GPS Tools are inserted, These are both corners of a box, and every coordinate in this box is added to the positions argument.
     * @return
     */
    int emitRedstoneOnEvent(Event event, TileEntity sensor, int range, Set<BlockPos> positions);

    /**
     * See {@link IEventSensorSetting#getRedstonePulseLength()}
     *
     * @return
     */
    int getRedstonePulseLength();

    /**
     * See {@link ISensorSetting#needsTextBox()}
     *
     * @return
     */
    boolean needsTextBox();

    /**
     * See {@link ISensorSetting#needsSlot()}
     */
    Rectangle needsSlot();

    /**
     * See {@link ISensorSetting#getDescription()}
     *
     * @return
     */
    List<String> getDescription();

    /**
     * Called by GuiScreen#drawScreen this method can be used to render additional things like status/info text.
     *
     * @param fontRenderer
     */
    void drawAdditionalInfo(FontRenderer fontRenderer);
}
