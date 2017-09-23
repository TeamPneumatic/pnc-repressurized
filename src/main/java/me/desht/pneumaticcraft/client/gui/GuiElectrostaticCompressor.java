package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.common.TickHandlerPneumaticCraft;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.inventory.Container4UpgradeSlots;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElectrostaticCompressor;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SideOnly(Side.CLIENT)
public class GuiElectrostaticCompressor extends GuiPneumaticContainerBase<TileEntityElectrostaticCompressor> {
    private int connectedCompressors = 1;
    private int ticksExisted;

    public GuiElectrostaticCompressor(InventoryPlayer player, TileEntityElectrostaticCompressor te) {

        super(new Container4UpgradeSlots(player, te), te, Textures.GUI_4UPGRADE_SLOTS);

    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);
        fontRenderer.drawString("Upgr.", 53, 19, 4210752);
    }

    @Override
    public String getRedstoneButtonText(int mode) {
        return mode == 0 ? "gui.tab.redstoneBehaviour.button.never" : "gui.tab.redstoneBehaviour.electrostaticCompressor.button.struckByLightning";
    }

    @Override
    protected void addPressureStatInfo(List<String> pressureStatText) {
        super.addPressureStatInfo(pressureStatText);

        pressureStatText.add(TextFormatting.GRAY + "Energy production:");
        pressureStatText.add(TextFormatting.BLACK + PneumaticCraftUtils.roundNumberTo(PneumaticValues.PRODUCTION_ELECTROSTATIC_COMPRESSOR / connectedCompressors, 1) + " mL/lightning strike");
        pressureStatText.add(TextFormatting.GRAY + "Maximum air redirection:");
        pressureStatText.add(TextFormatting.BLACK + PneumaticCraftUtils.roundNumberTo(PneumaticValues.MAX_REDIRECTION_PER_IRON_BAR * te.ironBarsBeneath, 1) + " mL/lightning strike");
    }

    @Override
    protected void addProblems(List<String> textList) {
        super.addProblems(textList);
        if (PneumaticValues.MAX_REDIRECTION_PER_IRON_BAR * te.ironBarsBeneath < PneumaticValues.PRODUCTION_ELECTROSTATIC_COMPRESSOR / connectedCompressors) {
            textList.add(TextFormatting.GRAY + "When lightning strikes with a full air tank not all the energy can be redirected!");
            textList.add(TextFormatting.BLACK + "Connect up more Iron Bars to the underside of the Electrostatic Compressor.");
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (ticksExisted % 20 == 0) {
            Set<BlockPos> positions = new HashSet<BlockPos>();
            positions.add(te.getPos());
            TickHandlerPneumaticCraft.getElectrostaticGrid(positions, te.getWorld(), te.getPos());
            connectedCompressors = 0;
            for (BlockPos coord : positions) {
                if (te.getWorld().getBlockState(coord).getBlock() == Blockss.ELECTROSTATIC_COMPRESSOR) {
                    connectedCompressors++;
                }
            }
        }
        ticksExisted++;

    }
}
