package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.common.ai.DroneAIDropItem;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetDropItem extends ProgWidgetInventoryBase implements IItemDropper {
    private boolean dropStraight;
    private boolean pickupDelay = true;

    public ProgWidgetDropItem() {
        super(ModProgWidgets.DROP_ITEM);
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.MAGENTA;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_DROP_ITEM;
    }

    @Override
    public boolean dropStraight() {
        return dropStraight;
    }

    @Override
    public void setDropStraight(boolean dropStraight) {
        this.dropStraight = dropStraight;
    }

    @Override
    public boolean hasPickupDelay() {
        return pickupDelay;
    }

    @Override
    public void setPickupDelay(boolean pickupDelay) {
        this.pickupDelay = pickupDelay;
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        tag.putBoolean("dropStraight", dropStraight);
        tag.putBoolean("pickupDelay", pickupDelay);
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        dropStraight = tag.getBoolean("dropStraight");
        pickupDelay = tag.getBoolean("pickupDelay");
    }

    @Override
    public void writeToPacket(PacketBuffer buf) {
        super.writeToPacket(buf);
        buf.writeBoolean(dropStraight);
        buf.writeBoolean(pickupDelay);
    }

    @Override
    public void readFromPacket(PacketBuffer buf) {
        super.readFromPacket(buf);
        dropStraight = buf.readBoolean();
        pickupDelay = buf.readBoolean();
    }

    @Override
    public void getTooltip(List<ITextComponent> curTooltip) {
        super.getTooltip(curTooltip);
        if (pickupDelay) {
            curTooltip.add(new TranslationTextComponent("pneumaticcraft.gui.progWidget.drop.hasPickupDelay"));
        } else {
            curTooltip.add(new TranslationTextComponent("pneumaticcraft.gui.progWidget.drop.noPickupDelay"));
        }
    }

    @Override
    public Goal getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return new DroneAIDropItem(drone, (ProgWidgetInventoryBase) widget);
    }

    @Override
    protected boolean isUsingSides() {
        return false;
    }

    @Override
    public List<ITextComponent> getExtraStringInfo() {
        return Collections.singletonList(xlate("pneumaticcraft.gui.progWidget.drop.dropMethod." + (dropStraight() ? "straight" : "random")));
    }
}
