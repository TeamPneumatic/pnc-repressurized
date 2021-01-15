package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.common.ai.DroneEntityAIPickupItems;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public class ProgWidgetPickupItem extends ProgWidgetAreaItemBase implements IItemPickupWidget {
    private boolean canSteal = false;

    public ProgWidgetPickupItem() {
        super(ModProgWidgets.PICKUP_ITEM.get());
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_PICK_ITEM;
    }

    @Override
    public Goal getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return new DroneEntityAIPickupItems(drone, (ProgWidgetAreaItemBase) widget);
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.PINK;
    }

    @Override
    public boolean canSteal() {
        return canSteal;
    }

    @Override
    public void setCanSteal(boolean canSteal) {
        this.canSteal = canSteal;
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        tag.putBoolean("canSteal", canSteal);
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        canSteal = tag.getBoolean("canSteal");
    }

    @Override
    public void writeToPacket(PacketBuffer buf) {
        super.writeToPacket(buf);
        buf.writeBoolean(canSteal);
    }

    @Override
    public void readFromPacket(PacketBuffer buf) {
        super.readFromPacket(buf);
        canSteal = buf.readBoolean();
    }
}
