package me.desht.pneumaticcraft.common.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.common.ai.DroneAIBlockCondition;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class ProgWidgetPressureCondition extends ProgWidgetCondition {

    public ProgWidgetPressureCondition() {
        super(ModProgWidgets.CONDITION_PRESSURE);
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgets.AREA, ModProgWidgets.TEXT);
    }

    @Override
    protected DroneAIBlockCondition getEvaluator(IDroneBase drone, IProgWidget widget) {
        return new DroneAIBlockCondition(drone, (ProgWidgetAreaItemBase) widget) {

            @Override
            protected boolean evaluate(BlockPos pos) {
                TileEntity te = drone.world().getTileEntity(pos);
                if (te != null) {
                    float pressure = Float.MIN_VALUE;
                    for (Direction d : Direction.VALUES) {
                        if (getSides()[d.ordinal()]) {
                            float p = te.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, d)
                                    .map(IAirHandlerMachine::getPressure)
                                    .orElse(0f);
                            pressure = Math.max(pressure, p);
                        }
                    }
                    return ((ICondition) progWidget).getOperator().evaluate(pressure, ((ICondition) progWidget).getRequiredCount());
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
