package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.common.ai.DroneAIBlockCondition;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class ProgWidgetEnergyCondition extends ProgWidgetCondition {
    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_CONDITION_ENERGY;
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
                for (Direction face : Direction.VALUES) {
                    if (getSides()[face.getIndex()]) {
                        energy = Math.max(energy, getEnergy(te, face));
                    }
                }
                return ((ICondition) progWidget).getOperator().evaluate(energy,((ICondition) progWidget).getRequiredCount());
            }

            private int getEnergy(TileEntity te, Direction side) {
                return te.getCapability(CapabilityEnergy.ENERGY, side).map(IEnergyStorage::getEnergyStored).orElse(0);
            }
        };
    }
}
