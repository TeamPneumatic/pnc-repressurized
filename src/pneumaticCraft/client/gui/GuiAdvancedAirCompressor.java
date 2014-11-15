package pneumaticCraft.client.gui;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.api.tileentity.IHeatExchanger;
import pneumaticCraft.client.gui.widget.WidgetTemperature;
import pneumaticCraft.common.inventory.ContainerAdvancedAirCompressor;
import pneumaticCraft.common.tileentity.TileEntityAdvancedAirCompressor;
import pneumaticCraft.lib.Textures;

public class GuiAdvancedAirCompressor extends GuiAirCompressor{

    public GuiAdvancedAirCompressor(InventoryPlayer player, TileEntityAdvancedAirCompressor te){
        super(new ContainerAdvancedAirCompressor(player, te), te, Textures.GUI_ADVANCED_AIR_COMPRESSOR_LOCATION);
    }

    @Override
    public void initGui(){
        super.initGui();
        addWidget(new WidgetTemperature(0, guiLeft + 87, guiTop + 20, 273, 675, ((IHeatExchanger)te).getHeatExchangerLogic(ForgeDirection.UNKNOWN), 325, 625));
    }

    @Override
    protected int getFuelSlotXOffset(){
        return 69;
    }

    @Override
    public void addProblems(List<String> curInfo){
        super.addProblems(curInfo);
        if(te.getEfficiency() < 100) {
            curInfo.add(I18n.format("gui.tab.problems.advancedAirCompressor.efficiency", te.getEfficiency() + "%%"));
        }
    }
}
