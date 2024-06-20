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

package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.common.block.entity.heat.HeatSinkBlockEntity;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class HeatSinkBlock extends AbstractPneumaticCraftBlock implements ColorHandlers.IHeatTintable, PneumaticCraftEntityBlock {

    private static final VoxelShape[] SHAPES = new VoxelShape[] {
        Block.box(0, 0, 0, 16,  8, 16),
        Block.box(0, 8, 0, 16, 16, 16),
        Block.box(0, 0, 0, 16, 16,  8),
        Block.box(0, 0, 8, 16, 16, 16),
        Block.box(0, 0, 0,  8, 16, 16),
        Block.box(8, 0, 0, 16, 16, 16),
    };

    public HeatSinkBlock() {
        super(ModBlocks.defaultProps());
    }

    @Override
    protected boolean isWaterloggable() {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext selectionContext) {
        return SHAPES[getRotation(state).get3DDataValue()];
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState state = super.getStateForPlacement(ctx);
        return state == null ? null : state.setValue(directionProperty(), ctx.getClickedFace().getOpposite());
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    protected boolean canRotateToTopOrBottom() {
        return true;
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        if (!(entity instanceof LivingEntity livingEntity)) return;

        PneumaticCraftUtils.getBlockEntityAt(world, pos, HeatSinkBlockEntity.class).ifPresent(te -> {
            double temp = te.getHeatExchanger().getTemperature();
            if (temp > 333) { // +60C
                entity.hurt(entity.damageSources().hotFloor(), 1f + ((float) temp - 333) * 0.05f);
                if (temp > 373) { // +100C
                    entity.igniteForSeconds(3);
                }
            } else if (temp < 243) { // -30C
                entity.setIsInPowderSnow(true);
                if (temp < 213) { // -60C
                    // Internally, 1 Frozen Tick is added while can freeze, and 2 taken off otherwise (wearing Leather Armor piece for example)
                    boolean canFreeze = entity.canFreeze();
                    int amplifier = (int) ((243 - temp) / 20d) + (canFreeze ? 0 : 3);  // add 3 if !canFreeze to compensate for -2 from vanilla
                    entity.setTicksFrozen(Math.min(entity.getTicksFrozen() + amplifier, entity.getTicksRequiredToFreeze() + 2));
                    if (entity.getTicksFrozen() >= entity.getTicksRequiredToFreeze()) {
                        int durationTicks = (int) ((243 - temp) * 2);
                        livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, durationTicks, amplifier));
                        // more damage if cold-vulnerable, less damage if !canFreeze() (typically armour protection)
                        float dmg = entity.getType().is(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES) ? 5 : 2;
                        if (!canFreeze) dmg /= 2f;
                        entity.hurt(entity.damageSources().freeze(), dmg);
                    }
                }
            }
        });
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new HeatSinkBlockEntity(pPos, pState);
    }
}
