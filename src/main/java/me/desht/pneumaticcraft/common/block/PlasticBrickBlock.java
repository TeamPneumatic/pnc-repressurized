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
import me.desht.pneumaticcraft.common.PNCDamageSource;
import me.desht.pneumaticcraft.common.item.ICustomTooltipName;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;

public class PlasticBrickBlock extends Block implements ColorHandlers.ITintableBlock {
    private static final VoxelShape COLLISION_SHAPE = box(0, 0, 0, 16, 15, 16);

    private static final EnumProperty<PartType> X_PART = EnumProperty.create("x_part", PartType.class);
    private static final EnumProperty<PartType> Z_PART = EnumProperty.create("z_part", PartType.class);
    private final DyeColor dyeColor;
    private final int tintColor;

    public PlasticBrickBlock(DyeColor dyeColor) {
        this(ModBlocks.defaultProps().sound(SoundType.WOOD).strength(2f), dyeColor);
    }

    PlasticBrickBlock(BlockBehaviour.Properties props, DyeColor dyeColor) {
        super(props);

        this.dyeColor = dyeColor;
        this.tintColor = PneumaticCraftUtils.getDyeColorAsRGB(dyeColor);

        registerDefaultState(defaultBlockState()
                .setValue(X_PART, PartType.NONE)
                .setValue(Z_PART, PartType.NONE)
        );
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    public DyeColor getDyeColor() {
        return dyeColor;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        builder.add(X_PART, Z_PART);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPE;
    }

    public VoxelShape getBlockSupportShape(BlockState pState, BlockGetter pReader, BlockPos pPos) {
        return Shapes.block();
    }

    public VoxelShape getVisualShape(BlockState pState, BlockGetter pReader, BlockPos pPos, CollisionContext pContext) {
        return Shapes.block();
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState state = super.getStateForPlacement(ctx);

        return calcParts(ctx.getLevel(), ctx.getClickedPos(), state);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        return calcParts(worldIn, currentPos, stateIn);
    }

    protected boolean hurtsToStepOn() {
        return true;
    }

    private BlockState calcParts(LevelAccessor world, BlockPos pos, BlockState stateIn) {
        Block w = world.getBlockState(pos.west()).getBlock();
        Block e = world.getBlockState(pos.east()).getBlock();
        PartType xType = PartType.NONE;
        boolean xRight = ((pos.getX() + pos.getY()) & 0x1) == 0;
        if (xRight && w == this) {
            xType = PartType.RIGHT;
        } else if (!xRight && e == this) {
            xType = PartType.LEFT;
        }

        Block n = world.getBlockState(pos.north()).getBlock();
        Block s = world.getBlockState(pos.south()).getBlock();
        PartType zType = PartType.NONE;
        boolean zRight = ((pos.getZ() + pos.getY()) & 0x1) == 0;
        if (zRight && s == this) {
            zType = PartType.RIGHT;
        } else if (!zRight && n == this) {
            zType = PartType.LEFT;
        }

        return stateIn.setValue(X_PART, xType).setValue(Z_PART, zType);
    }

    @Override
    public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn) {
        if (hurtsToStepOn() && entityIn instanceof LivingEntity livingEntity) {
            ItemStack stack = livingEntity.getItemBySlot(EquipmentSlot.FEET);
            if (stack.isEmpty()) {
                entityIn.hurt(PNCDamageSource.plasticBlock(worldIn), 3);
                livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
            }
        }
    }

    @Override
    public int getTintColor(BlockState state, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos, int tintIndex) {
        return tintColor;
    }

    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return false;
    }

    enum PartType implements StringRepresentable {
        NONE("none"),
        LEFT("left"),
        RIGHT("right");

        private final String name;

        PartType(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public static class ItemPlasticBrick extends BlockItem implements ICustomTooltipName {
        public ItemPlasticBrick(PlasticBrickBlock blockPlasticBrick) {
            super(blockPlasticBrick, ModItems.defaultProps());
        }

        @Override
        public String getCustomTooltipTranslationKey() {
            return "block.pneumaticcraft.plastic_brick";
        }
    }
}
