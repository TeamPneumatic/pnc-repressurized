package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.common.inventory.ContainerAirCompressor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAirCompressor;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

import static mcjty.theoneprobe.rendering.RenderHelper.drawTexturedModalRect;

public class GuiAirCompressor extends GuiPneumaticContainerBase<ContainerAirCompressor,TileEntityAirCompressor> {

    public GuiAirCompressor(ContainerAirCompressor container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_AIR_COMPRESSOR_LOCATION;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float opacity, int x, int y) {
        super.drawGuiContainerBackgroundLayer(opacity, x, y);

        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        int i1 = te.getBurnTimeRemainingScaled(12);
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        if (te.burnTime >= te.curFuelUsage) {
            drawTexturedModalRect(xStart + getFuelSlotXOffset(), yStart + 38 + 12 - i1, 176, 12 - i1, 14, i1 + 2);
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
        if (te.getBurnTimeRemainingScaled(12) > 0 || FurnaceTileEntity.isFuel(te.getPrimaryInventory().getStackInSlot(0)) && te.redstoneAllows()) {
            pressureStatText.add("\u00a77Currently producing:");
            pressureStatText.add("\u00a70" + (double) Math.round(te.getBaseProduction() * te.getEfficiency() * te.getSpeedMultiplierFromUpgrades() / 100) + " mL/tick.");
        }
    }

    @Override
    protected void addProblems(List<String> textList) {
        super.addProblems(textList);
        if (te.burnTime <= 0 && !FurnaceTileEntity.isFuel(te.getPrimaryInventory().getStackInSlot(0))) {
            textList.add("\u00a77No fuel!");
            textList.add("\u00a70Insert any burnable item.");
        }
        List<Pair<Direction, IAirHandler>> teSurrounding = te.getAirHandler(null).getConnectedPneumatics();
        if (teSurrounding.isEmpty()) {
            textList.add("\u00a77Air leaking!");
            textList.add("\u00a70Add pipes / machines");
            textList.add("\u00a70to the output.");
        }
    }
}
