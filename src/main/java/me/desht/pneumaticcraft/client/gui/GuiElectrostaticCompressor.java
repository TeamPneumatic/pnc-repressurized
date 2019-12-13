package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.inventory.ContainerElectrostaticCompressor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElectrostaticCompressor;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GuiElectrostaticCompressor extends GuiPneumaticContainerBase<ContainerElectrostaticCompressor,TileEntityElectrostaticCompressor> {
    private int connectedCompressors = 1;
    private int ticksExisted;
    private WidgetAnimatedStat electrostaticStat;

    public GuiElectrostaticCompressor(ContainerElectrostaticCompressor container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();
        electrostaticStat = addAnimatedStat("gui.tab.electrostaticCompressor.info.title", new ItemStack(ModBlocks.ELECTROSTATIC_COMPRESSOR), 0xFF20A0FF, false);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);
        font.drawString("Upgr.", 53, 19, 4210752);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_4UPGRADE_SLOTS;
    }

    @Override
    protected void addWarnings(List<String> textList) {
        super.addWarnings(textList);
        if (PneumaticValues.MAX_REDIRECTION_PER_IRON_BAR * te.ironBarsBeneath < PneumaticValues.PRODUCTION_ELECTROSTATIC_COMPRESSOR / connectedCompressors) {
            textList.add("gui.tab.problems.electrostatic.notEnoughGrounding");
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (ticksExisted % 20 == 0) {
            Set<BlockPos> positions = new HashSet<>();
            positions.add(te.getPos());
            te.getElectrostaticGrid(positions, te.getWorld(), te.getPos(), null);
            connectedCompressors = 0;
            for (BlockPos coord : positions) {
                if (te.getWorld().getBlockState(coord).getBlock() == ModBlocks.ELECTROSTATIC_COMPRESSOR) {
                    connectedCompressors++;
                }
            }
        }

        ticksExisted++;

        List<String> info = new ArrayList<>();
        info.add(TextFormatting.WHITE + "Energy production:");
        info.add(TextFormatting.BLACK + PneumaticCraftUtils.roundNumberTo(PneumaticValues.PRODUCTION_ELECTROSTATIC_COMPRESSOR / (float) connectedCompressors, 1) + " mL/lightning strike");
        info.add(TextFormatting.BLACK + "(" + connectedCompressors + " connected compressors)");
        info.add(TextFormatting.WHITE + "Maximum air redirection:");
        info.add(TextFormatting.BLACK + PneumaticCraftUtils.roundNumberTo(PneumaticValues.MAX_REDIRECTION_PER_IRON_BAR * te.ironBarsBeneath, 1) + " mL/lightning strike");
        info.add(TextFormatting.WHITE + "Lightning rod length (iron bars above):");
        info.add(TextFormatting.BLACK + "" + te.ironBarsAbove);
        String t = PneumaticCraftUtils.convertTicksToMinutesAndSeconds(te.getStrikeChance(), false);
        info.add(TextFormatting.WHITE + "Average strike time: ");
        info.add(TextFormatting.BLACK + "" + t + " (with optimal-sized grid)");

        electrostaticStat.setText(info);
    }
}
