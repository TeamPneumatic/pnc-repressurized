package pneumaticCraft.client.gui;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import pneumaticCraft.client.gui.widget.WidgetTank;
import pneumaticCraft.common.PneumaticCraftAPIHandler;
import pneumaticCraft.common.inventory.ContainerLiquidCompressor;
import pneumaticCraft.common.tileentity.TileEntityLiquidCompressor;
import pneumaticCraft.lib.Textures;

public class GuiLiquidCompressor extends GuiPneumaticContainerBase<TileEntityLiquidCompressor>{

    public GuiLiquidCompressor(InventoryPlayer player, TileEntityLiquidCompressor te){
        super(new ContainerLiquidCompressor(player, te), te, Textures.GUI_LIQUID_COMPRESSOR);
    }

    public GuiLiquidCompressor(Container container, TileEntityLiquidCompressor te, String texture){
        super(container, te, texture);
    }

    @Override
    public void initGui(){
        super.initGui();
        addWidget(new WidgetTank(0, guiLeft + getFluidOffset(), guiTop + 15, te.getFluidTank()));
        addAnimatedStat("gui.tab.liquidCompressor.fuel", new ItemStack(Items.lava_bucket), 0xFFFF6600, true).setTextWithoutCuttingString(getAllFuels());
    }

    @Override
    protected void addPressureStatInfo(List<String> pressureStatText){
        super.addPressureStatInfo(pressureStatText);
        if(te.isProducing) {
            pressureStatText.add("\u00a77Currently producing:");
            pressureStatText.add("\u00a70" + (double)Math.round(te.getBaseProduction() * te.getEfficiency() * te.getSpeedMultiplierFromUpgrades(te.getUpgradeSlots()) / 100) + " mL/tick.");
        }
    }

    protected int getFluidOffset(){
        return 86;
    }

    @Override
    protected Point getInvNameOffset(){
        return new Point(0, -2);
    }

    @Override
    protected Point getGaugeLocation(){
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        return new Point(xStart + xSize * 3 / 4 + 5, yStart + ySize * 1 / 4 + 4);
    }

    private List<String> getAllFuels(){
        List<String> fuels = new ArrayList<String>();
        fuels.add("L/Bucket | Fluid");
        for(Map.Entry<String, Integer> map : sortByValue(PneumaticCraftAPIHandler.getInstance().liquidFuels).entrySet()) {
            String value = map.getValue() / 1000 + "";
            while(fontRendererObj.getStringWidth(value) < 25) {
                value = value + " ";
            }
            Fluid fluid = FluidRegistry.getFluid(map.getKey());
            fuels.add(value + "| " + fluid.getLocalizedName(new FluidStack(fluid, 1)));
        }
        return fuels;
    }

    private static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map){
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>(){
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2){
                return -o1.getValue().compareTo(o2.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for(Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){
        super.drawGuiContainerForegroundLayer(x, y);

        fontRendererObj.drawString("Upgr.", 15, 19, 4210752);
    }

    @Override
    public void addProblems(List<String> curInfo){
        super.addProblems(curInfo);
        if(!te.isProducing && te.getTankInfo(null)[0].fluid == null) {
            curInfo.add("gui.tab.problems.liquidCompressor.noFuel");
        }
    }
}
