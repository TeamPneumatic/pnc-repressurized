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

public class ProgWidgetPickupItem extends ProgWidgetAreaItemBase {
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
        return new DroneEntityAIPickupItems(drone, (ProgWidgetPickupItem) widget);
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.PINK;
    }

    /**
     * Should this widget ignore PreventRemoteMovement tags on item entities?
     * @return true if items can be "stolen" e.g. off conveyor belts, false to keep the drone honest
     */
    public boolean canSteal() {
        return canSteal;
    }

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
