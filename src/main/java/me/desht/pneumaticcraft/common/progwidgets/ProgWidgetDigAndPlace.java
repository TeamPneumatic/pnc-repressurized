package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.ai.DroneAIBlockInteraction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public abstract class ProgWidgetDigAndPlace extends ProgWidgetAreaItemBase implements IBlockOrdered, IMaxActions {
    private Ordering order;
    private int maxActions = 1;
    private boolean useMaxActions;

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

    ProgWidgetDigAndPlace(ProgWidgetType<?> type, Ordering order) {
        super(type);
        this.order = order;
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        tag.putInt("order", order.ordinal());
        if (useMaxActions) tag.putBoolean("useMaxActions", true);
        tag.putInt("maxActions", maxActions);
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        order = Ordering.values()[tag.getInt("order")];
        useMaxActions = tag.getBoolean("useMaxActions");
        maxActions = tag.getInt("maxActions");
    }

    @Override
    public void writeToPacket(PacketBuffer buf) {
        super.writeToPacket(buf);
        buf.writeByte(order.ordinal());
        buf.writeBoolean(useMaxActions);
        buf.writeVarInt(maxActions);
    }

    @Override
    public void readFromPacket(PacketBuffer buf) {
        super.readFromPacket(buf);
        order = Ordering.values()[buf.readByte()];
        useMaxActions = buf.readBoolean();
        maxActions = buf.readVarInt();
    }

    @Override
    public List<ITextComponent> getExtraStringInfo() {
        return Collections.singletonList(xlate(order.getTranslationKey()));
    }

    @Override
    public void setMaxActions(int maxActions) {
        this.maxActions = maxActions;
    }

    @Override
    public int getMaxActions() {
        return maxActions;
    }

    @Override
    public void setUseMaxActions(boolean useMaxActions) {
        this.useMaxActions = useMaxActions;
    }

    @Override
    public boolean useMaxActions() {
        return useMaxActions;
    }

    DroneAIBlockInteraction<?> setupMaxActions(DroneAIBlockInteraction<?> ai, IMaxActions widget) {
        return widget.useMaxActions() ? ai.setMaxActions(widget.getMaxActions()) : ai;
    }
}
