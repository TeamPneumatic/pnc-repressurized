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

package me.desht.pneumaticcraft.common.heat;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.crafting.recipe.HeatPropertiesRecipe;
import me.desht.pneumaticcraft.api.heat.HeatBehaviour;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.heat.IHeatRegistry;
import me.desht.pneumaticcraft.api.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.heat.behaviour.HeatBehaviourManager;
import me.desht.pneumaticcraft.common.semiblock.SemiblockTracker;
import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

public enum HeatExchangerManager implements IHeatRegistry {
    INSTANCE;

    public static HeatExchangerManager getInstance() {
        return INSTANCE;
    }

    @Nonnull
    public Optional<IHeatExchangerLogic> getLogic(Level world, BlockPos pos, Direction side) {
        return getLogic(world, pos, side, IHeatExchangerLogic.ALL_BLOCKS);
    }

    @Nonnull
    public Optional<IHeatExchangerLogic> getLogic(Level world, BlockPos pos, Direction side, BiPredicate<LevelAccessor,BlockPos> blockFilter) {
        if (!world.isLoaded(pos)) return Optional.empty();
        BlockEntity te = world.getBlockEntity(pos);
        // important: use cap here, not IHeatExchangingTE interface
        Optional<IHeatExchangerLogic> heatCap = IOHelper.getCap(te, PNCCapabilities.HEAT_EXCHANGER_BLOCK, side);
        if (heatCap.isPresent()) {
            return heatCap;
        } else {
            if (!blockFilter.test(world, pos)) {
                return Optional.empty();
            }
            List<ISemiBlock> l = SemiblockTracker.getInstance().getAllSemiblocks(world, pos)
                    .filter(s -> s.getSemiblockCapability(PNCCapabilities.HEAT_EXCHANGER_ENTITY).isPresent())
                    .toList();
            if (!l.isEmpty()) {
                return l.getFirst().getSemiblockCapability(PNCCapabilities.HEAT_EXCHANGER_ENTITY);
            }
            if (world.isEmptyBlock(pos)) {
                return Optional.of(HeatExchangerLogicAmbient.atPosition(world, pos));
            }
            HeatPropertiesRecipe entry = BlockHeatProperties.getInstance().getCustomHeatEntry(world, world.getBlockState(pos));
            return entry != null ? Optional.of(entry.getLogic()) : Optional.empty();
        }
    }

    @Override
    public void registerHeatBehaviour(ResourceLocation id, Supplier<? extends HeatBehaviour> heatBehaviour) {
        HeatBehaviourManager.getInstance().registerBehaviour(id, heatBehaviour);
    }

    @Override
    public IHeatExchangerLogic makeHeatExchangerLogic() {
        return new HeatExchangerLogicTicking();
    }
}
