package pneumaticCraft.client.gui;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.api.tileentity.IHeatExchanger;
import pneumaticCraft.client.gui.widget.WidgetTemperature;
import pneumaticCraft.common.inventory.ContainerAdvancedLiquidCompressor;
import pneumaticCraft.common.tileentity.TileEntityAdvancedLiquidCompressor;
import pneumaticCraft.lib.Textures;

public class GuiAdvancedLiquidCompressor extends GuiLiquidCompressor{

    public GuiAdvancedLiquidCompressor(InventoryPlayer player, TileEntityAdvancedLiquidCompressor te){
        super(new ContainerAdvancedLiquidCompressor(player, te), te, Textures.GUI_ADVANCED_LIQUID_COMPRESSOR);
    }

    @Override
    public void initGui(){
        super.initGui();
        addWidget(new WidgetTemperature(0, guiLeft + 92, guiTop + 20, 273, 675, ((IHeatExchanger)te).getHeatExchangerLogic(ForgeDirection.UNKNOWN), 325, 625));
    }

    @Override
    protected int getFluidOffset(){
        return 72;
    }

    @Override
    public void addProblems(List<String> curInfo){
        super.addProblems(curInfo);
        if(te.getEfficiency() < 100) {
            curInfo.add(I18n.format("gui.tab.problems.advancedAirCompressor.efficiency", te.getEfficiency() + "%%"));
        }
    }
}
