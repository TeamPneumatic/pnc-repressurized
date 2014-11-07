package pneumaticCraft.client.gui;

import java.awt.Point;
import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import pneumaticCraft.common.inventory.ContainerVacuumPump;
import pneumaticCraft.common.tileentity.TileEntityVacuumPump;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiVacuumPump extends GuiPneumaticContainerBase<TileEntityVacuumPump>{

    public GuiVacuumPump(InventoryPlayer player, TileEntityVacuumPump te){
        super(new ContainerVacuumPump(player, te), te, Textures.GUI_VACUUM_PUMP_LOCATION);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){
        super.drawGuiContainerForegroundLayer(x, y);
        fontRendererObj.drawString("Upgr.", 76, 19, 4210752);

        fontRendererObj.drawString("+", 32, 47, 0xFF00AA00);
        fontRendererObj.drawString("-", 138, 47, 0xFFFF0000);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float opacity, int x, int y){
        super.drawGuiContainerBackgroundLayer(opacity, x, y);

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        GuiUtils.drawPressureGauge(fontRendererObj, -1, PneumaticValues.MAX_PRESSURE_VACUUM_PUMP, PneumaticValues.DANGER_PRESSURE_VACUUM_PUMP, PneumaticValues.MIN_PRESSURE_VACUUM_PUMP, te.getPressure(te.getInputSide()), xStart + xSize * 1 / 5, yStart + ySize * 1 / 5 + 4, zLevel);
        GuiUtils.drawPressureGauge(fontRendererObj, -1, PneumaticValues.MAX_PRESSURE_VACUUM_PUMP, PneumaticValues.DANGER_PRESSURE_VACUUM_PUMP, -1, te.getPressure(te.getVacuumSide()), xStart + xSize * 4 / 5, yStart + ySize * 1 / 5 + 4, zLevel);
    }

    @Override
    protected Point getGaugeLocation(){
        return null;
    }

    @Override
    protected void addPressureStatInfo(List<String> pressureStatText){
        pressureStatText.add("\u00a77Current Input Pressure:");
        pressureStatText.add("\u00a70" + (double)Math.round(te.getPressure(te.getInputSide()) * 10) / 10 + " bar.");
        pressureStatText.add("\u00a77Current Input Air:");
        pressureStatText.add("\u00a70" + (double)Math.round(te.currentAir + te.volume) + " mL.");
        pressureStatText.add("\u00a77Current Vacuum Pressure:");
        pressureStatText.add("\u00a70" + (double)Math.round(te.getPressure(te.getVacuumSide()) * 10) / 10 + " bar.");
        pressureStatText.add("\u00a77Current Vacuum Air:");
        pressureStatText.add("\u00a70" + (double)Math.round(te.getCurrentAir(te.getVacuumSide()) + te.volume) + " mL.");
        pressureStatText.add("\u00a77Volume:");
        pressureStatText.add("\u00a70" + (double)Math.round(PneumaticValues.VOLUME_VACUUM_PUMP) + " mL.");
        float pressureLeft = te.volume - PneumaticValues.VOLUME_VACUUM_PUMP;
        if(pressureLeft > 0) {
            pressureStatText.add("\u00a70" + (double)Math.round(pressureLeft) + " mL. (Volume Upgrades)");
            pressureStatText.add("\u00a70--------+");
            pressureStatText.add("\u00a70" + (double)Math.round(te.volume) + " mL.");
        }

        if(te.turning) {
            pressureStatText.add("\u00a77Currently sucking at:");
            pressureStatText.add("\u00a70" + (double)Math.round(PneumaticValues.PRODUCTION_VACUUM_PUMP * te.getSpeedMultiplierFromUpgrades(te.getUpgradeSlots())) + " mL/tick.");
        }
    }

    @Override
    protected void addProblems(List<String> textList){
        super.addProblems(textList);
        if(te.getPressure(te.getInputSide()) < PneumaticValues.MIN_PRESSURE_VACUUM_PUMP) {
            textList.add("gui.tab.problems.notEnoughPressure");
            textList.add(I18n.format("gui.tab.problems.applyPressure", PneumaticValues.MIN_PRESSURE_VACUUM_PUMP));
        }
    }

}
