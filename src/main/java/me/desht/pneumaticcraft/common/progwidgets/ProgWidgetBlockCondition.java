package me.desht.pneumaticcraft.common.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.ai.DroneAIBlockCondition;
import me.desht.pneumaticcraft.common.ai.DroneAIDig;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class ProgWidgetBlockCondition extends ProgWidgetCondition {
    public boolean checkingForAir;
    public boolean checkingForLiquids;

    public ProgWidgetBlockCondition() {
        super(ModProgWidgets.CONDITION_BLOCK);
    }

    @Override
    public List<ProgWidgetType> getParameters() {
        return ImmutableList.of(ModProgWidgets.AREA, ModProgWidgets.ITEM_FILTER, ModProgWidgets.TEXT);
    }

    @Override
    protected DroneAIBlockCondition getEvaluator(IDroneBase drone, IProgWidget widget) {
        return new DroneAIBlockCondition(drone, (ProgWidgetAreaItemBase) widget) {

            @Override
            protected boolean evaluate(BlockPos pos) {
                if (checkingForAir && drone.world().isAirBlock(pos)) return true;
                if (checkingForLiquids && PneumaticCraftUtils.isBlockLiquid(drone.world().getBlockState(pos).getBlock()))
                    return true;
                if (!checkingForAir && !checkingForLiquids || getConnectedParameters()[1] != null) {
                    return DroneAIDig.isBlockValidForFilter(drone.world(), pos, drone, progWidget);
                } else {
                    return false;
                }
            }
        };
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_CONDITION_BLOCK;
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        tag.putBoolean("checkingForAir", checkingForAir);
        tag.putBoolean("checkingForLiquids", checkingForLiquids);
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        checkingForAir = tag.getBoolean("checkingForAir");
        checkingForLiquids = tag.getBoolean("checkingForLiquids");
    }

}
