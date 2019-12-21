package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.PneumaticCraftAPIHandler;
import me.desht.pneumaticcraft.common.inventory.ContainerLiquidCompressor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityLiquidCompressor;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class GuiLiquidCompressor extends GuiPneumaticContainerBase<ContainerLiquidCompressor,TileEntityLiquidCompressor> {
    public GuiLiquidCompressor(ContainerLiquidCompressor container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();
        addButton(new WidgetTank(guiLeft + getFluidOffset(), guiTop + 15, te.getTank()));
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
    protected PointXY getInvNameOffset() {
        return new PointXY(0, -2);
    }

    @Override
    protected PointXY getGaugeLocation() {
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        return new PointXY(xStart + xSize * 3 / 4 + 5, yStart + ySize / 4 + 4);
    }

    private List<String> getAllFuels() {
        List<String> fuels = new ArrayList<>();
        fuels.add(TextFormatting.UNDERLINE + "mL/mB | Fluid");

        for (Map.Entry<ResourceLocation, Integer> map : sortByValue(PneumaticCraftAPIHandler.getInstance().liquidFuels).entrySet()) {
            String value = String.format("%4d", map.getValue() / 1000);
            while (font.getStringWidth(value) < 30) {
                value = value + " ";
            }
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(map.getKey());
            FluidStack stack = new FluidStack(fluid, 1);
            fuels.add(value + "| " + StringUtils.abbreviate(stack.getDisplayName().getFormattedText(), 25));

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

        font.drawString("Upgr.", 15, 19, 4210752);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_LIQUID_COMPRESSOR;
    }

    @Override
    public void addProblems(List<String> curInfo) {
        super.addProblems(curInfo);

        if (te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).isPresent()) {
            te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).ifPresent(fluidHandler -> {
                if (!te.isProducing && fluidHandler.getFluidInTank(0).isEmpty()) {
                    curInfo.add("gui.tab.problems.liquidCompressor.noFuel");
                }
            });
        } else {
            curInfo.add("gui.tab.problems.liquidCompressor.noFuel");
        }
    }
}
