package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.inventory.Container4UpgradeSlots;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElectrostaticCompressor;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SideOnly(Side.CLIENT)
public class GuiElectrostaticCompressor extends GuiPneumaticContainerBase<TileEntityElectrostaticCompressor> {
    private int connectedCompressors = 1;
    private int ticksExisted;
    private GuiAnimatedStat electrostaticStat;

    public GuiElectrostaticCompressor(InventoryPlayer player, TileEntityElectrostaticCompressor te) {
        super(new Container4UpgradeSlots(player, te), te, Textures.GUI_4UPGRADE_SLOTS);
    }

    @Override
    public void initGui() {
        super.initGui();
        electrostaticStat = addAnimatedStat("gui.tab.electrostaticCompressor.info.title", new ItemStack(Blockss.ELECTROSTATIC_COMPRESSOR), 0xFF20A0FF, false);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);
        fontRenderer.drawString("Upgr.", 53, 19, 4210752);
    }

    @Override
    protected void addWarnings(List<String> textList) {
        super.addWarnings(textList);
        if (PneumaticValues.MAX_REDIRECTION_PER_IRON_BAR * te.ironBarsBeneath < PneumaticValues.PRODUCTION_ELECTROSTATIC_COMPRESSOR / connectedCompressors) {
            textList.add("gui.tab.problems.electrostatic.notEnoughGrounding");
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (ticksExisted % 20 == 0) {
            Set<BlockPos> positions = new HashSet<>();
            positions.add(te.getPos());
            te.getElectrostaticGrid(positions, te.getWorld(), te.getPos(), null);
            connectedCompressors = 0;
            for (BlockPos coord : positions) {
                if (te.getWorld().getBlockState(coord).getBlock() == Blockss.ELECTROSTATIC_COMPRESSOR) {
                    connectedCompressors++;
                }
            }
        }

        ticksExisted++;

        List<String> info = new ArrayList<>();
        info.add(TextFormatting.GRAY + "Energy production:");
        info.add(TextFormatting.BLACK + PneumaticCraftUtils.roundNumberTo(PneumaticValues.PRODUCTION_ELECTROSTATIC_COMPRESSOR / connectedCompressors, 1) + " mL/lightning strike");
        info.add(TextFormatting.BLACK + "(" + connectedCompressors + " connected compressors)");
        info.add(TextFormatting.GRAY + "Maximum air redirection:");
        info.add(TextFormatting.BLACK + PneumaticCraftUtils.roundNumberTo(PneumaticValues.MAX_REDIRECTION_PER_IRON_BAR * te.ironBarsBeneath, 1) + " mL/lightning strike");
        info.add(TextFormatting.GRAY + "Lightning rod length (iron bars above):");
        info.add(TextFormatting.BLACK + "" + te.ironBarsAbove);
        String t = PneumaticCraftUtils.convertTicksToMinutesAndSeconds(te.getStrikeChance(), false);
        info.add(TextFormatting.GRAY + "Average strike time: ");
        info.add(TextFormatting.BLACK + "" + t + " (with optimal-sized grid)");

        electrostaticStat.setText(info);
    }
}
