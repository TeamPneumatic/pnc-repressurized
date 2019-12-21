package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.inventory.ContainerVacuumPump;
import me.desht.pneumaticcraft.common.tileentity.TileEntityVacuumPump;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public class GuiVacuumPump extends GuiPneumaticContainerBase<ContainerVacuumPump,TileEntityVacuumPump> {

    public GuiVacuumPump(ContainerVacuumPump container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_VACUUM_PUMP_LOCATION;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);
        font.drawString("Upgr.", 76, 19, 4210752);

        font.drawString("+", 32, 47, 0xFF00AA00);
        font.drawString("-", 138, 47, 0xFFFF0000);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float opacity, int x, int y) {
        super.drawGuiContainerBackgroundLayer(opacity, x, y);

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        GuiUtils.drawPressureGauge(font, -1, PneumaticValues.MAX_PRESSURE_VACUUM_PUMP, PneumaticValues.DANGER_PRESSURE_VACUUM_PUMP, PneumaticValues.MIN_PRESSURE_VACUUM_PUMP, te.getAirHandler(te.getInputSide()).getPressure(), xStart + xSize / 5, yStart + ySize / 5 + 4);
        GuiUtils.drawPressureGauge(font, -1, PneumaticValues.MAX_PRESSURE_VACUUM_PUMP, PneumaticValues.DANGER_PRESSURE_VACUUM_PUMP, -1, te.getAirHandler(te.getVacuumSide()).getPressure(), xStart + xSize * 4 / 5, yStart + ySize / 5 + 4);
    }

    @Override
    protected PointXY getGaugeLocation() {
        return null;
    }

    @Override
    protected void addPressureStatInfo(List<String> pressureStatText) {
        IAirHandler inputHandler = te.getAirHandler(te.getInputSide());
        IAirHandler vacuumHandler = te.getAirHandler(te.getVacuumSide());
        pressureStatText.add("\u00a77Current Input Pressure:");
        pressureStatText.add("\u00a70" + PneumaticCraftUtils.roundNumberTo(inputHandler.getPressure(), 1) + " bar.");
        pressureStatText.add("\u00a77Current Input Air:");
        pressureStatText.add("\u00a70" + (inputHandler.getAir() + inputHandler.getVolume()) + " mL.");
        pressureStatText.add("\u00a77Current Vacuum Pressure:");
        pressureStatText.add("\u00a70" + PneumaticCraftUtils.roundNumberTo(vacuumHandler.getPressure(), 1) + " bar.");
        pressureStatText.add("\u00a77Current Vacuum Air:");
        pressureStatText.add("\u00a70" + (vacuumHandler.getAir() + vacuumHandler.getVolume()) + " mL.");
        pressureStatText.add("\u00a77Volume:");
        pressureStatText.add("\u00a70" + (double) Math.round(PneumaticValues.VOLUME_VACUUM_PUMP) + " mL.");
        int volumeLeft = inputHandler.getVolume() - PneumaticValues.VOLUME_VACUUM_PUMP;
        if (volumeLeft > 0) {
            pressureStatText.add("\u00a70" + volumeLeft + " mL. (Volume Upgrades)");
            pressureStatText.add("\u00a70--------+");
            pressureStatText.add("\u00a70" + inputHandler.getVolume() + " mL.");
        }

        if (te.turning) {
            pressureStatText.add("\u00a77Currently sucking at:");
            pressureStatText.add("\u00a70" + (double) Math.round(PneumaticValues.PRODUCTION_VACUUM_PUMP * te.getSpeedMultiplierFromUpgrades()) + " mL/tick.");
        }
    }

    @Override
    protected void addProblems(List<String> textList) {
        super.addProblems(textList);
        if (te.getAirHandler(te.getInputSide()).getPressure() < PneumaticValues.MIN_PRESSURE_VACUUM_PUMP) {
            textList.add("gui.tab.problems.notEnoughPressure");
            textList.add(I18n.format("gui.tab.problems.applyPressure", PneumaticValues.MIN_PRESSURE_VACUUM_PUMP));
        }
    }

}
