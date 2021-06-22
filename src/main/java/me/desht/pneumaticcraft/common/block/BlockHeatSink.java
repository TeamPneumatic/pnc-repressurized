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
        Block.makeCuboidShape(0, 0, 0, 16,  8, 16),
        Block.makeCuboidShape(0, 8, 0, 16, 16, 16),
        Block.makeCuboidShape(0, 0, 0, 16, 16,  8),
        Block.makeCuboidShape(0, 0, 8, 16, 16, 16),
        Block.makeCuboidShape(0, 0, 0,  8, 16, 16),
        Block.makeCuboidShape(8, 0, 0, 16, 16, 16),
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
        return SHAPES[getRotation(state).getIndex()];
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext ctx) {
        return super.getStateForPlacement(ctx).with(directionProperty(), ctx.getFace().getOpposite());
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
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!(entity instanceof LivingEntity)) return;

        PneumaticCraftUtils.getTileEntityAt(world, pos, TileEntityHeatSink.class).ifPresent(te -> {
            double temp = te.getHeatExchanger().getTemperature();
            if (temp > 333) { // +60C
                entity.attackEntityFrom(DamageSource.HOT_FLOOR, 1f + ((float) temp - 333) * 0.05f);
                if (temp > 373) { // +100C
                    entity.setFire(3);
                }
            } else if (temp < 243) { // -30C
                int durationTicks = (int) ((243 - temp) * 2);
                int amplifier = (int) ((243 - temp) / 20);
                ((LivingEntity) entity).addPotionEffect(new EffectInstance(Effects.SLOWNESS, durationTicks, amplifier));
                if (temp < 213) { // -60C
                    entity.attackEntityFrom(DamageSourcePneumaticCraft.FREEZING, 2);
                }
            }
        });
    }
}
