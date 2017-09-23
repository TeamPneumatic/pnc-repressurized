package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IPneumaticMachine;
import me.desht.pneumaticcraft.common.ai.DroneAIBlockCondition;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class ProgWidgetPressureCondition extends ProgWidgetCondition {

    @Override
    public String getWidgetString() {
        return "conditionPressure";
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters() {
        return new Class[]{ProgWidgetArea.class, ProgWidgetString.class};
    }

    @Override
    protected DroneAIBlockCondition getEvaluator(IDroneBase drone, IProgWidget widget) {
        return new DroneAIBlockCondition(drone, (ProgWidgetAreaItemBase) widget) {

            @Override
            protected boolean evaluate(BlockPos pos) {
                TileEntity te = drone.world().getTileEntity(pos);
                if (te instanceof IPneumaticMachine) {
                    float pressure = Float.MIN_VALUE;
                    for (EnumFacing d : EnumFacing.VALUES) {
                        if (getSides()[d.ordinal()]) {
                            IAirHandler airHandler = ((IPneumaticMachine) te).getAirHandler(d);
                            if (airHandler != null) pressure = Math.max(airHandler.getPressure(), pressure);
                        }
                    }
                    return ((ICondition) widget).getOperator() == ICondition.Operator.EQUALS ? pressure == ((ICondition) widget).getRequiredCount() : pressure >= ((ICondition) widget).getRequiredCount();
                }
                return false;
            }

        };
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_CONDITION_PRESSURE;
    }

}
