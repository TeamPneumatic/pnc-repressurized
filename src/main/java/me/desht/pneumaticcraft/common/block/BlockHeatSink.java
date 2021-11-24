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

import me.desht.pneumaticcraft.api.DamageSourcePneumaticCraft;
import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntityHeatSink;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockHeatSink extends BlockPneumaticCraft implements ColorHandlers.IHeatTintable {

    private static final VoxelShape[] SHAPES = new VoxelShape[] {
        Block.box(0, 0, 0, 16,  8, 16),
        Block.box(0, 8, 0, 16, 16, 16),
        Block.box(0, 0, 0, 16, 16,  8),
        Block.box(0, 0, 8, 16, 16, 16),
        Block.box(0, 0, 0,  8, 16, 16),
        Block.box(8, 0, 0, 16, 16, 16),
    };

    public BlockHeatSink() {
        super(ModBlocks.defaultProps());
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityHeatSink.class;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext selectionContext) {
        return SHAPES[getRotation(state).get3DDataValue()];
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext ctx) {
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
    public void entityInside(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!(entity instanceof LivingEntity)) return;

        PneumaticCraftUtils.getTileEntityAt(world, pos, TileEntityHeatSink.class).ifPresent(te -> {
            double temp = te.getHeatExchanger().getTemperature();
            if (temp > 333) { // +60C
                entity.hurt(DamageSource.HOT_FLOOR, 1f + ((float) temp - 333) * 0.05f);
                if (temp > 373) { // +100C
                    entity.setSecondsOnFire(3);
                }
            } else if (temp < 243) { // -30C
                int durationTicks = (int) ((243 - temp) * 2);
                int amplifier = (int) ((243 - temp) / 20);
                ((LivingEntity) entity).addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, durationTicks, amplifier));
                if (temp < 213) { // -60C
                    entity.hurt(DamageSourcePneumaticCraft.FREEZING, 2);
                }
            }
        });
    }
}
