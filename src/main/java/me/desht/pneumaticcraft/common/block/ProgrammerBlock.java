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

import me.desht.pneumaticcraft.common.block.entity.ProgrammerBlockEntity;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketProgrammerSync;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class ProgrammerBlock extends AbstractPneumaticCraftBlock implements PneumaticCraftEntityBlock {
    private static final VoxelShape SHAPE_N = VoxelShapeUtils.or(
            Block.box(0, 8, 0, 16, 10, 16),
            Block.box(13, 0, 13, 15, 8, 15),
            Block.box(1, 0, 13, 3, 8, 15),
            Block.box(1, 10, 9, 11, 11, 15),
            Block.box(1, 10, 1, 15, 11, 7),
            Block.box(12, 10, 11, 15, 11, 15),
            Block.box(12.25, 10, 7, 12.75, 10.5, 11),
            Block.box(9.25, 10, 7, 9.75, 10.5, 9),
            Block.box(3, 10.5, 2, 13, 14.5, 4),
            Block.box(1, 0, 1, 3, 8, 3),
            Block.box(13, 0, 1, 15, 8, 3),
            Block.box(13.25, 10, 7, 13.75, 10.5, 11)
    );
    private static final VoxelShape SHAPE_E = VoxelShapeUtils.rotateY(SHAPE_N, 90);
    private static final VoxelShape SHAPE_S = VoxelShapeUtils.rotateY(SHAPE_E, 90);
    private static final VoxelShape SHAPE_W = VoxelShapeUtils.rotateY(SHAPE_S, 90);
    private static final VoxelShape[] SHAPES = new VoxelShape[] { SHAPE_S, SHAPE_W, SHAPE_N, SHAPE_E };

    public ProgrammerBlock() {
        super(ModBlocks.defaultProps());
    }

    @Override
    protected boolean isWaterloggable() {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        Direction d = state.getValue(directionProperty());
        return SHAPES[d.get2DDataValue()];
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult brtr) {
        if (!world.isClientSide && !player.isShiftKeyDown()) {
            // FIXME this should be sync'd via the container as part of the openGui() call
            PneumaticCraftUtils.getTileEntityAt(world, pos, ProgrammerBlockEntity.class)
                    .ifPresent(te -> NetworkHandler.sendToPlayer(PacketProgrammerSync.forBlockEntity(te), (ServerPlayer) player));
        }
        return super.use(state, world, pos, player, hand, brtr);
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ProgrammerBlockEntity(pPos, pState);
    }
}
