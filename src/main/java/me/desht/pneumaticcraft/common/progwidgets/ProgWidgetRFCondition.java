package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.common.ai.DroneAIBlockCondition;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class ProgWidgetRFCondition extends ProgWidgetCondition {
    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_CONDITION_RF;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters() {
        return new Class[]{ProgWidgetArea.class, ProgWidgetString.class};
    }

    @Override
    public String getWidgetString() {
        return "conditionRF";
    }

    @Override
    protected DroneAIBlockCondition getEvaluator(IDroneBase drone, IProgWidget widget) {
        return new DroneAIBlockCondition(drone, (ProgWidgetAreaItemBase) widget) {
            @Override
            protected boolean evaluate(BlockPos pos) {
                TileEntity te = drone.world().getTileEntity(pos);
                if (te == null) return false;
                int energy = 0;
                for (EnumFacing face : EnumFacing.VALUES) {
                    if (getSides()[face.getIndex()] && te.hasCapability(CapabilityEnergy.ENERGY, face)) {
                        IEnergyStorage storage = te.getCapability(CapabilityEnergy.ENERGY, face);
                        energy = Math.max(storage.getEnergyStored(), energy);
                    }
                }
                return ((ICondition) widget).getOperator() == ICondition.Operator.EQUALS ?
                        energy == ((ICondition) widget).getRequiredCount() :
                        energy >= ((ICondition) widget).getRequiredCount();
            }
        };
    }
}
