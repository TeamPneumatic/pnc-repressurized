package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.fuel.IFuelRegistry;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.inventory.ContainerLiquidCompressor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityLiquidCompressor;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiLiquidCompressor extends GuiPneumaticContainerBase<ContainerLiquidCompressor,TileEntityLiquidCompressor> {
    public GuiLiquidCompressor(ContainerLiquidCompressor container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();
        addButton(new WidgetTank(guiLeft + getFluidOffset(), guiTop + 15, te.getTank()));
        WidgetAnimatedStat stat = addAnimatedStat(xlate("pneumaticcraft.gui.tab.liquidCompressor.fuel"), new ItemStack(ModItems.LPG_BUCKET.get()), 0xFFC04400, true);
        Pair<Integer, List<ITextComponent>> p = getAllFuels();
        stat.setMinimumExpandedDimensions(p.getLeft(), 17);
        stat.setText(p.getRight());
    }

    @Override
    protected void addPressureStatInfo(List<ITextComponent> pressureStatText) {
        super.addPressureStatInfo(pressureStatText);

        pressureStatText.add(xlate("pneumaticcraft.gui.tooltip.maxProduction",
                PneumaticCraftUtils.roundNumberTo(te.airPerTick, 2)).mergeStyle(TextFormatting.BLACK));
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

    @Override
    protected String upgradeCategory() {
        return "liquid_compressor";
    }

    private Pair<Integer,List<ITextComponent>> getAllFuels() {
        List<ITextComponent> text = new ArrayList<>();
        TranslationTextComponent header = xlate("pneumaticcraft.gui.liquidCompressor.fuelsHeader");
        text.add(header.mergeStyle(TextFormatting.UNDERLINE, TextFormatting.AQUA));
        int maxWidth = font.getStringPropertyWidth(header);

        IFuelRegistry api = PneumaticRegistry.getInstance().getFuelRegistry();
        List<Fluid> fluids = new ArrayList<>(api.registeredFuels());
        fluids.sort((o1, o2) -> Integer.compare(api.getFuelValue(o2), api.getFuelValue(o1)));
        int w = font.getStringWidth(".");
        for (Fluid fluid : fluids) {
            String value = String.format("%4d", api.getFuelValue(fluid) / 1000);
            int nSpc = (32 - font.getStringWidth(value)) / w;
            value = value + StringUtils.repeat('.', nSpc);
            FluidStack stack = new FluidStack(fluid, 1);
            float mul = api.getBurnRateMultiplier(fluid);
            StringTextComponent line = mul == 1 ?
                    new StringTextComponent(value + "| " + StringUtils.abbreviate(stack.getDisplayName().getString(), 25)) :
                    new StringTextComponent(value + "| " + StringUtils.abbreviate(stack.getDisplayName().getString(), 20) + " (x" + PneumaticCraftUtils.roundNumberTo(mul, 2) + ")");
            maxWidth = Math.max(maxWidth, font.getStringPropertyWidth(line));
            text.add(line);
        }

        return Pair.of(Math.min(maxWidth, guiLeft - 10), text);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_LIQUID_COMPRESSOR;
    }

    @Override
    public void addProblems(List<ITextComponent> curInfo) {
        super.addProblems(curInfo);

        if (te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).isPresent()) {
            te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).ifPresent(fluidHandler -> {
                if (!te.isProducing && fluidHandler.getFluidInTank(0).isEmpty()) {
                    curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.liquidCompressor.noFuel"));
                }
            });
        } else {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.liquidCompressor.noFuel"));
        }
    }
}
