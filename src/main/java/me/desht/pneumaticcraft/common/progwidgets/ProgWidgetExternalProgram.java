package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.common.ai.DroneAIExternalProgram;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public class ProgWidgetExternalProgram extends ProgWidgetAreaItemBase {
    public boolean shareVariables;

    @Override
    public String getWidgetString() {
        return "externalProgram";
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.PURPLE;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_EXTERNAL_PROGRAM;
    }

    @Override
    public Goal getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return new DroneAIExternalProgram(drone, aiManager, (ProgWidgetExternalProgram) widget);
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters() {
        return new Class[]{ProgWidgetArea.class};
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        tag.putBoolean("shareVariables", shareVariables);
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        shareVariables = tag.getBoolean("shareVariables");
    }

    @Override
    public boolean canBeRunByComputers(IDroneBase drone, IProgWidget widget) {
        return false;
    }
}
