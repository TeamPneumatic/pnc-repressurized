package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.common.inventory.ContainerAirCompressor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAirCompressor;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class GuiAirCompressor extends GuiPneumaticContainerBase<ContainerAirCompressor,TileEntityAirCompressor> {

    public GuiAirCompressor(ContainerAirCompressor container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_AIR_COMPRESSOR;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float opacity, int x, int y) {
        super.drawGuiContainerBackgroundLayer(opacity, x, y);

        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        int i1 = te.getBurnTimeRemainingScaled(12);
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        if (te.burnTime >= te.curFuelUsage) {
            blit(xStart + getFuelSlotXOffset(), yStart + 38 + 12 - i1, 176, 12 - i1, 14, i1 + 2);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);

        font.drawString("Upgr.", 28, 19, 4210752);
    }

    protected int getFuelSlotXOffset() {
        return 80;
    }

    @Override
    protected void addPressureStatInfo(List<String> pressureStatText) {
        super.addPressureStatInfo(pressureStatText);
        if (te.isActive()) {
            float prod = Math.round(te.getBaseProduction() * te.getEfficiency() * te.getSpeedMultiplierFromUpgrades() / 100);
            pressureStatText.add(TextFormatting.BLACK + I18n.format("gui.tooltip.producingAir", PneumaticCraftUtils.roundNumberTo(prod, 1)));
        }
    }

    @Override
    protected void addProblems(List<String> textList) {
        super.addProblems(textList);
        if (te.burnTime <= te.curFuelUsage && !FurnaceTileEntity.isFuel(te.getPrimaryInventory().getStackInSlot(0))) {
            textList.add(I18n.format("gui.tab.problems.airCompressor.noFuel"));
        }

        if (te.isLeaking()) {
            textList.add(I18n.format("gui.tab.problems.airLeak"));
        }
    }

    @Override
    protected String upgradeCategory() {
        return "air_compressor";
    }
}
