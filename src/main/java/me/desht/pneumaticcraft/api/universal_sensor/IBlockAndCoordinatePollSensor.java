package me.desht.pneumaticcraft.api.universal_sensor;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Set;

public interface IBlockAndCoordinatePollSensor {
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
    Set<EnumUpgrade> getRequiredUpgrades();

    /**
     * See {@link ISensorSetting#needsTextBox()}
     *
     * @return
     */
    boolean needsTextBox();

    /**
     * See {@link ISensorSetting#getDescription()}
     *
     * @return
     */
    default List<String> getDescription() {
        return ISensorSetting._getDescription(getSensorPath());
    }

    /**
     * See {@link IPollSensorSetting#getRedstoneValue(World, BlockPos, int, String)} , but this has the GPS tracked coordinates
     * as extra parameters. This method will only invoke with a valid GPS tool, and when all the coordinates are within range.
     *
     * @param world
     * @param pos
     * @param sensorRange
     * @param textBoxText
     * @param positions   When only one GPS Tool is inserted this contains the position of just that tool. If two GPS Tools are inserted, These are both corners of a box, and every coordinate in this box is added to the positions argument.
     * @return
     */
    int getRedstoneValue(World world, BlockPos pos, int sensorRange, String textBoxText, Set<BlockPos> positions);

    /**
     * See {@link IPollSensorSetting#getPollFrequency(TileEntity)}
     *
     * @return
     */
    int getPollFrequency();

    /**
     * Called by GuiScreen#drawScreen this method can be used to render additional things like status/info text.
     *
     * @param matrixStack
     * @param fontRenderer
     */
    void drawAdditionalInfo(MatrixStack matrixStack, FontRenderer fontRenderer);

}
