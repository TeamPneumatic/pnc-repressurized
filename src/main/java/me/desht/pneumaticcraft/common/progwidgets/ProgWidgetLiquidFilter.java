package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetLiquidFilter extends ProgWidget {
    private Fluid fluid;

    public static ProgWidgetLiquidFilter withFilter(Fluid fluid) {
        ProgWidgetLiquidFilter f = new ProgWidgetLiquidFilter();
        f.setFluid(fluid);
        return f;
    }

    @Override
    public void addErrors(List<ITextComponent> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (fluid == null) curInfo.add(xlate("gui.progWidget.liquidFilter.error.noLiquid"));
    }

    @Override
    public boolean hasStepInput() {
        return false;
    }

    @Override
    public Class<? extends IProgWidget> returnType() {
        return ProgWidgetLiquidFilter.class;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters() {
        return new Class[]{ProgWidgetLiquidFilter.class};
    }

    @Override
    public String getWidgetString() {
        return "liquidFilter";
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_LIQUID_FILTER;
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        // todo 1.14 fluids
//        fluid = FluidRegistry.getFluid(tag.getString("fluid"));
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        if (fluid != null) tag.putString("fluid", fluid.getName());
    }

    @Override
    public void getTooltip(List<ITextComponent> curTooltip) {
        super.getTooltip(curTooltip);
        if (fluid != null) {
            curTooltip.add(new StringTextComponent("Fluid: " )
                    .applyTextStyle(TextFormatting.AQUA)
                    .appendText(getExtraStringInfo())
                    .applyTextStyle(TextFormatting.WHITE));
        }
    }

    private boolean isLiquidValid(Fluid fluid) {
        return this.fluid == null || fluid == this.fluid;
    }

    static boolean isLiquidValid(Fluid fluid, IProgWidget mainWidget, int filterIndex) {
        ProgWidgetLiquidFilter widget = (ProgWidgetLiquidFilter) mainWidget.getConnectedParameters()[mainWidget.getParameters().length + filterIndex];
        while (widget != null) {
            if (!widget.isLiquidValid(fluid)) return false;
            widget = (ProgWidgetLiquidFilter) widget.getConnectedParameters()[0];
        }
        widget = (ProgWidgetLiquidFilter) mainWidget.getConnectedParameters()[filterIndex];
        if (widget == null) return true;
        while (widget != null) {
            if (widget.isLiquidValid(fluid)) return true;
            widget = (ProgWidgetLiquidFilter) widget.getConnectedParameters()[0];
        }
        return false;
    }

    public static boolean isLiquidValid(Fluid fluid, List<ProgWidgetLiquidFilter> whitelist, List<ProgWidgetLiquidFilter> blacklist) {
        for (ProgWidgetLiquidFilter filter : blacklist) {
            if (!filter.isLiquidValid(fluid)) return false;
        }
        if (whitelist.size() == 0) return true;
        for (ProgWidgetLiquidFilter filter : whitelist) {
            if (filter.isLiquidValid(fluid)) return true;
        }
        return false;
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.EASY;
    }

    @Override
    public String getExtraStringInfo() {
        return fluid != null ? fluid.getLocalizedName(new FluidStack(fluid, 1)) : I18n.format("gui.progWidget.liquidFilter.noFluid");
    }

    public void setFluid(Fluid fluid) {
        this.fluid = fluid;
    }

    public Fluid getFluid() {
        return fluid;
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.RED;
    }
}
