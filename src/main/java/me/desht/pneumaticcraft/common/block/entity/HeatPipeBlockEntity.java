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

package me.desht.pneumaticcraft.common.block.entity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiPredicate;

public class HeatPipeBlockEntity extends AbstractTickingBlockEntity implements CamouflageableBlockEntity, IHeatExchangingTE {
    public static final BiPredicate<LevelAccessor, BlockPos> NO_AIR_OR_LIQUIDS =
            (world, pos) -> !world.isEmptyBlock(pos) && !(world.getBlockState(pos).getBlock() instanceof LiquidBlock);

    private final IHeatExchangerLogic heatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().makeHeatExchangerLogic();

    private BlockState camoState;

    public HeatPipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.HEAT_PIPE.get(), pos, state);
    }

    @Override
    public boolean hasItemCapability() {
        return false;
    }

    @Override
    public BiPredicate<LevelAccessor, BlockPos> heatExchangerBlockFilter() {
        return NO_AIR_OR_LIQUIDS;
    }

    @Override
    public void writeToPacket(CompoundTag tag) {
        super.writeToPacket(tag);

        CamouflageableBlockEntity.writeCamo(tag, camoState);
    }

    @Override
    public void readFromPacket(CompoundTag tag) {
        super.readFromPacket(tag);

        camoState = CamouflageableBlockEntity.readCamo(tag);
    }

    @Override
    public BlockState getCamouflage() {
        return camoState;
    }

    @Override
    public void setCamouflage(BlockState state) {
        camoState = state;
        CamouflageableBlockEntity.syncToClient(this);
    }

    @Override
    public IHeatExchangerLogic getHeatExchanger(Direction dir) {
        return heatExchanger;
    }
}
