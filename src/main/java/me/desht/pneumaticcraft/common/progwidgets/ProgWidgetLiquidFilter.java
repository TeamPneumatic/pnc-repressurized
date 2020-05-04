package me.desht.pneumaticcraft.common.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetLiquidFilter extends ProgWidget {
    private Fluid fluid = Fluids.EMPTY;

    public ProgWidgetLiquidFilter() {
        super(ModProgWidgets.LIQUID_FILTER);
    }

    public static ProgWidgetLiquidFilter withFilter(Fluid fluid) {
        ProgWidgetLiquidFilter f = new ProgWidgetLiquidFilter();
        f.setFluid(fluid);
        return f;
    }

    @Override
    public void addErrors(List<ITextComponent> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (fluid == Fluids.EMPTY) curInfo.add(xlate("gui.progWidget.liquidFilter.error.noLiquid"));
    }

    @Override
    public boolean hasStepInput() {
        return false;
    }

    @Override
    public ProgWidgetType returnType() {
        return ModProgWidgets.LIQUID_FILTER;
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgets.LIQUID_FILTER);
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_LIQUID_FILTER;
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        fluid = tag.contains("fluid") ?
                ForgeRegistries.FLUIDS.getValue(new ResourceLocation(tag.getString("fluid"))) :
                Fluids.EMPTY;
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        if (fluid != Fluids.EMPTY) tag.putString("fluid", fluid.getRegistryName().toString());
    }

    @Override
    public void writeToPacket(PacketBuffer buf) {
        super.writeToPacket(buf);
        buf.writeFluidStack(new FluidStack(fluid, 1000));
    }

    @Override
    public void readFromPacket(PacketBuffer buf) {
        super.readFromPacket(buf);
        fluid = buf.readFluidStack().getFluid();
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
        ProgWidgetLiquidFilter widget = (ProgWidgetLiquidFilter) mainWidget.getConnectedParameters()[mainWidget.getParameters().size() + filterIndex];
        while (widget != null) {
            if (!widget.isLiquidValid(fluid)) return false;
            widget = (ProgWidgetLiquidFilter) widget.getConnectedParameters()[0];
        }
        widget = (ProgWidgetLiquidFilter) mainWidget.getConnectedParameters()[filterIndex];
        if (widget == null) return true;
        while (widget != null) {
            if (widget.isLiquidValid(fluid)) return true;  // TODO verify this, looks dodgy
            widget = (ProgWidgetLiquidFilter) widget.getConnectedParameters()[0];
        }
        return false;
    }

//    public static boolean isLiquidValid(Fluid fluid, List<ProgWidgetLiquidFilter> whitelist, List<ProgWidgetLiquidFilter> blacklist) {
//        for (ProgWidgetLiquidFilter filter : blacklist) {
//            if (!filter.isLiquidValid(fluid)) return false;
//        }
//        if (whitelist.size() == 0) return true;
//        for (ProgWidgetLiquidFilter filter : whitelist) {
//            if (filter.isLiquidValid(fluid)) return true;
//        }
//        return false;
//    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.EASY;
    }

    @Override
    public String getExtraStringInfo() {
        return fluid != Fluids.EMPTY ?
                new FluidStack(fluid, 1).getDisplayName().getFormattedText() :
                I18n.format("gui.progWidget.liquidFilter.noFluid");
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
