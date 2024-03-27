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

import me.desht.pneumaticcraft.common.block.entity.CamouflageableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.model.data.ModelProperty;

/**
 * Base class for blocks which may be camouflaged, storing the camouflaged block state in the
 * CAMO_STATE model property.
 */
//@Optional.Interface (iface = "team.chisel.ctm.api.IFacade", modid = "ctm-api")
public abstract class AbstractCamouflageBlock extends AbstractPneumaticCraftBlock /*implements IFacade*/ {
    public static final ModelProperty<BlockState> CAMO_STATE = new ModelProperty<>();
    public static final ModelProperty<BlockGetter> BLOCK_ACCESS = new ModelProperty<>();
    public static final ModelProperty<BlockPos> BLOCK_POS = new ModelProperty<>();

    protected AbstractCamouflageBlock(Properties props) {
        super(props);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level world, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof CamouflageableBlockEntity camo && !player.isCreative()) {
            BlockState camoState = camo.getCamouflage();
            if (camoState != null) {
                ItemStack camoStack = CamouflageableBlockEntity.getStackForState(camoState);
                camo.setCamouflage(null);
                world.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, pos, getId(camoState));
                ItemEntity entity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, camoStack);
                world.addFreshEntity(entity);
                return false;
            }
        }
        return super.onDestroyedByPlayer(state, world, pos, player, willHarvest, fluid);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext ctx) {
        CamouflageableBlockEntity camo = getCamoState(reader, pos);
        return camo == null ? getUncamouflagedShape(state, reader, pos, ctx) : camo.getCamouflage().getShape(reader, pos, ctx);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext ctx) {
        CamouflageableBlockEntity camo = getCamoState(reader, pos);
        return camo == null ? getUncamouflagedCollisionShape(state, reader, pos, ctx) : camo.getCamouflage().getCollisionShape(reader, pos, ctx);
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter worldIn, BlockPos pos) {
        CamouflageableBlockEntity camo = getCamoState(worldIn, pos);
        return camo == null ? getUncamouflagedRaytraceShape(state, worldIn, pos) : camo.getCamouflage().getVisualShape(worldIn, pos, CollisionContext.empty());
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter worldIn, BlockPos pos) {
        CamouflageableBlockEntity camo = getCamoState(worldIn, pos);
        return camo == null ? getUncamouflagedRenderShape(state, worldIn, pos) : camo.getCamouflage().getBlockSupportShape(worldIn, pos);
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter world, BlockPos pos) {
        CamouflageableBlockEntity camo = getCamoState(world, pos);
        return camo == null ? super.getLightBlock(state, world, pos) : camo.getCamouflage().getLightBlock(world, pos);
    }

    @Override
    public boolean hasDynamicShape() {
        return true;  // prevent blockstate caching side solidity
    }

    private CamouflageableBlockEntity getCamoState(BlockGetter blockAccess, BlockPos pos) {
        if (blockAccess == null || pos == null) return null;
        BlockEntity be = blockAccess.getBlockEntity(pos);
        return be instanceof CamouflageableBlockEntity camo && camo.getCamouflage() != null ? (CamouflageableBlockEntity) be : null;
    }

    /**
     * The equivalent of {@link net.minecraft.world.level.block.Block#getShape(BlockState, BlockGetter, BlockPos, CollisionContext)},
     * but for uncamouflaged camo blocks.
     * @param state the blockstate
     * @param reader the world
     * @param pos the block pos
     * @param ctx the selection context
     * @return the block's actual shape, when it isn't camouflaged
     */
    public abstract VoxelShape getUncamouflagedShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext ctx);

    protected VoxelShape getUncamouflagedCollisionShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext ctx) {
        return getUncamouflagedShape(state, reader, pos, ctx);
    }

    protected VoxelShape getUncamouflagedRenderShape(BlockState state, BlockGetter reader, BlockPos pos) {
        return getUncamouflagedShape(state, reader, pos, CollisionContext.empty());
    }

    protected VoxelShape getUncamouflagedRaytraceShape(BlockState state, BlockGetter reader, BlockPos pos) {
        return Shapes.empty();
    }

//
//    @Nonnull
//    @Override
//    @Optional.Method(modid = "ctm-api")
//    public BlockState getFacade(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable Direction side) {
//        TileEntity te = world.getTileEntity(pos);
//        if (te instanceof ICamouflageableTE && ((ICamouflageableTE) te).getCamouflage() != null) {
//            return ((ICamouflageableTE) te).getCamouflage();
//        }
//        return world.getBlockState(pos);
//    }
}
