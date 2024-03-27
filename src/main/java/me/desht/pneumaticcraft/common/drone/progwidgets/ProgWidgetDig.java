/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.drone.progwidgets;

import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.drone.ai.DroneAIDig;
import me.desht.pneumaticcraft.common.registry.ModProgWidgets;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.DyeColor;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetDig extends ProgWidgetDigAndPlace implements IToolUser {
    private boolean requireDiggingTool;
    
    public ProgWidgetDig() {
        super(ModProgWidgets.DIG.get(), Ordering.CLOSEST);
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_DIG;
    }

    @Override
    public Goal getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return setupMaxActions(new DroneAIDig(drone, (ProgWidgetAreaItemBase) widget), (IMaxActions) widget);
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.BROWN;
    }

    @Override
    public boolean requiresTool(){
        return requireDiggingTool;
    }
    
    @Override
    public void setRequiresTool(boolean requireDiggingTool){
        this.requireDiggingTool = requireDiggingTool;
    }
    
    @Override
    public void getTooltip(List<Component> curTooltip) {
        super.getTooltip(curTooltip);
        
        if (requiresTool()) {
            curTooltip.add(xlate("pneumaticcraft.gui.progWidget.dig.requiresDiggingTool"));
        }
    }
    
    @Override
    public void writeToNBT(CompoundTag tag){
        super.writeToNBT(tag);
        if (requireDiggingTool) tag.putBoolean("requireDiggingTool", true);
    }

    @Override
    public void readFromNBT(CompoundTag tag){
        super.readFromNBT(tag);
        requireDiggingTool = tag.getBoolean("requireDiggingTool");
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buf) {
        super.writeToPacket(buf);
        buf.writeBoolean(requireDiggingTool);
    }

    @Override
    public void readFromPacket(FriendlyByteBuf buf) {
        super.readFromPacket(buf);
        requireDiggingTool = buf.readBoolean();
    }
}
