package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.common.PneumaticCraftAPIHandler;
import me.desht.pneumaticcraft.common.inventory.ContainerLiquidCompressor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityLiquidCompressor;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.awt.*;
import java.util.*;
import java.util.List;

public class GuiLiquidCompressor extends GuiPneumaticContainerBase<TileEntityLiquidCompressor> {

    public GuiLiquidCompressor(InventoryPlayer player, TileEntityLiquidCompressor te) {
        super(new ContainerLiquidCompressor(player, te), te, Textures.GUI_LIQUID_COMPRESSOR);
    }

    public GuiLiquidCompressor(Container container, TileEntityLiquidCompressor te, String texture) {
        super(container, te, texture);
    }

    @Override
    public void initGui() {
        super.initGui();
        addWidget(new WidgetTank(0, guiLeft + getFluidOffset(), guiTop + 15, te.getTank()));
        addAnimatedStat("gui.tab.liquidCompressor.fuel", new ItemStack(Items.LAVA_BUCKET), 0xFFFF6600, true).setTextWithoutCuttingString(getAllFuels());
    }

    @Override
    protected void addPressureStatInfo(List<String> pressureStatText) {
        super.addPressureStatInfo(pressureStatText);
        if (te.isProducing) {
            pressureStatText.add("\u00a77Currently producing:");
            pressureStatText.add("\u00a70" + (double) Math.round(te.getBaseProduction() * te.getEfficiency() * te.getSpeedMultiplierFromUpgrades() / 100) + " mL/tick.");
        }
    }

    protected int getFluidOffset() {
        return 86;
    }

    @Override
    protected Point getInvNameOffset() {
        return new Point(0, -2);
    }

    @Override
    protected Point getGaugeLocation() {
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        return new Point(xStart + xSize * 3 / 4 + 5, yStart + ySize * 1 / 4 + 4);
    }

    private List<String> getAllFuels() {
        List<String> fuels = new ArrayList<String>();
        fuels.add("L/Bucket | Fluid");
        for (Map.Entry<String, Integer> map : sortByValue(PneumaticCraftAPIHandler.getInstance().liquidFuels).entrySet()) {
            String value = map.getValue() / 1000 + "";
            while (fontRenderer.getStringWidth(value) < 25) {
                value = value + " ";
            }
            Fluid fluid = FluidRegistry.getFluid(map.getKey());
            fuels.add(value + "| " + fluid.getLocalizedName(new FluidStack(fluid, 1)));
        }
        return fuels;
    }

    private static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        list.sort((o1, o2) -> -o1.getValue().compareTo(o2.getValue()));

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);

        fontRenderer.drawString("Upgr.", 15, 19, 4210752);
    }

    @Override
    public void addProblems(List<String> curInfo) {
        super.addProblems(curInfo);
        IFluidHandler fluidHandler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
        if (!te.isProducing && (fluidHandler == null || fluidHandler.getTankProperties()[0].getContents() == null)) {
            curInfo.add("gui.tab.problems.liquidCompressor.noFuel");
        }
    }
}
