package me.desht.pneumaticcraft.common.thirdparty.coldsweat;

import com.momosoftworks.coldsweat.api.temperature.block_temp.BlockTemp;
import com.momosoftworks.coldsweat.api.util.Temperature;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.block.entity.IHeatExchangingTE;
import me.desht.pneumaticcraft.common.block.entity.heat.HeatPipeBlockEntity;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.heat.HeatExchangerManager;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class PNCHeatBlockTemp extends BlockTemp {
    public PNCHeatBlockTemp() {
        super(ConfigHelper.common().integration.coldSweatMinBlockEffect.get(), ConfigHelper.common().integration.coldSweatMaxBlockEffect.get(),
                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
                3, true, false,
                heatBlocks()
        );
    }

    private static Block[] heatBlocks() {
        return ModBlockEntityTypes.streamBlockEntities()
                .filter(be -> be instanceof IHeatExchangingTE && !exclude(be))
                .flatMap(be -> be.getType().getValidBlocks().stream())
                .toArray(Block[]::new);
    }

    private static boolean exclude(BlockEntity be) {
        // TODO could use a better way of identifying insulated block entities
        return be instanceof HeatPipeBlockEntity;
    }

    @Override
    public double getTemperature(Level level, @Nullable LivingEntity entity, BlockState state, BlockPos pos, double distance) {
        if (entity instanceof Player) {
            Vec3 p = Vec3.atCenterOf(pos).subtract(entity.position());
            Direction dir = Direction.getNearest(p).getOpposite();
            return HeatExchangerManager.getInstance().getLogic(level, pos, dir)
                    .map(this::convertTemp)
                    .orElse(0.0);
        }
        return 0.0;
    }

    private double convertTemp(IHeatExchangerLogic logic) {
        int delta = logic.getTemperatureAsInt() - (int) logic.getAmbientTemperature();
        return Temperature.convert(delta, Temperature.Units.C, Temperature.Units.MC, false);
    }
}
