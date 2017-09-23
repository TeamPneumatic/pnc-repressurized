package me.desht.pneumaticcraft.common.sensor.pollSensors;

import com.google.common.collect.ImmutableSet;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.api.universalSensor.IBlockAndCoordinatePollSensor;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.tileentity.TileEntityCompressedIronBlock;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.math.NumberUtils;
import org.lwjgl.util.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BlockHeatSensor implements IBlockAndCoordinatePollSensor {

    @Override
    public String getSensorPath() {
        return "Block/Heat";
    }

    @Override
    public Set<Item> getRequiredUpgrades() {
        return ImmutableSet.of(Itemss.upgrades.get(EnumUpgrade.BLOCK_TRACKER), Itemss.GPS_TOOL);
    }

    @Override
    public int getPollFrequency() {
        return 20;
    }

    @Override
    public boolean needsTextBox() {
        return true;
    }

    @Override
    public List<String> getDescription() {
        List<String> text = new ArrayList<String>();
        text.add(TextFormatting.BLACK + I18n.format("gui.universalSensor.desc.heatSensor"));
        return text;
    }

    @Override
    public int getRedstoneValue(World world, BlockPos pos, int sensorRange, String textBoxText, Set<BlockPos> positions) {
        double temperature = Double.MIN_VALUE;
        for (BlockPos p : positions) {
            TileEntity te = world.getTileEntity(p);
            if (te instanceof IHeatExchanger) {
                IHeatExchanger exchanger = (IHeatExchanger) te;
                for (EnumFacing d : EnumFacing.VALUES) {
                    IHeatExchangerLogic logic = exchanger.getHeatExchangerLogic(d);
                    if (logic != null) temperature = Math.max(temperature, logic.getTemperature());
                }
            }
        }
        return NumberUtils.isCreatable(textBoxText) ?
                temperature - 273 > NumberUtils.toInt(textBoxText) ? 15 : 0 :
                TileEntityCompressedIronBlock.getComparatorOutput((int) temperature);
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