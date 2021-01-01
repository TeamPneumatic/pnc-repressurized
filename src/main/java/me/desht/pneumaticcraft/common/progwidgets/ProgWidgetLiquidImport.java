package me.desht.pneumaticcraft.common.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.ai.DroneAILiquidImport;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

public class ProgWidgetLiquidImport extends ProgWidgetInventoryBase implements ILiquidFiltered, IBlockOrdered {
    private Ordering order = Ordering.HIGH_TO_LOW;

    public ProgWidgetLiquidImport() {
        super(ModProgWidgets.LIQUID_IMPORT.get());
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_LIQUID_IM;
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgets.AREA.get(), ModProgWidgets.LIQUID_FILTER.get());
    }

    @Override
    public boolean isFluidValid(Fluid fluid) {
        return ProgWidgetLiquidFilter.isLiquidValid(fluid, this, 1);
    }

    @Override
    public Goal getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return new DroneAILiquidImport(drone, (ProgWidgetInventoryBase) widget);
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.BLUE;
    }

    @Override
    public Ordering getOrder() {
        return order;
    }

    @Override
    public void setOrder(Ordering order) {
        this.order = order;
    }

    @Override
    public void getTooltip(List<ITextComponent> curTooltip) {
        super.getTooltip(curTooltip);
        curTooltip.add(new StringTextComponent("Order: ").append(new TranslationTextComponent(order.getTranslationKey())));
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        tag.putInt("order", order.ordinal());
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        order = Ordering.values()[tag.getInt("order")];
    }

    @Override
    public void writeToPacket(PacketBuffer buf) {
        super.writeToPacket(buf);
        buf.writeByte(order.ordinal());
    }

    @Override
    public void readFromPacket(PacketBuffer buf) {
        super.readFromPacket(buf);
        order = Ordering.values()[buf.readByte()];
    }
}
