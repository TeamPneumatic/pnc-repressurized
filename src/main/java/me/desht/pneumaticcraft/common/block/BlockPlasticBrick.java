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
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ICustomTooltipName;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class BlockPlasticBrick extends Block implements ColorHandlers.ITintableBlock {
    private static final VoxelShape COLLISION_SHAPE = box(0, 0, 0, 16, 15, 16);

    private static final EnumProperty<PartType> X_PART = EnumProperty.create("x_part", PartType.class);
    private static final EnumProperty<PartType> Z_PART = EnumProperty.create("z_part", PartType.class);
    private final DyeColor color;

    public BlockPlasticBrick(DyeColor color) {
        super(ModBlocks.defaultProps().sound(SoundType.WOOD).strength(2f));
        this.color = color;

        registerDefaultState(getStateDefinition().any().setValue(X_PART, PartType.NONE).setValue(Z_PART, PartType.NONE));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    public DyeColor getColor() {
        return color;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        builder.add(X_PART, Z_PART);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return COLLISION_SHAPE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext ctx) {
        BlockState state = super.getStateForPlacement(ctx);

        return calcParts(ctx.getLevel(), ctx.getClickedPos(), state);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        return calcParts(worldIn, currentPos, stateIn);
    }

    private BlockState calcParts(IWorld world, BlockPos pos, BlockState stateIn) {
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
    public void entityInside(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        if (entityIn instanceof LivingEntity) {
            ItemStack stack = ((LivingEntity) entityIn).getItemBySlot(EquipmentSlotType.FEET);
            if (stack.isEmpty()) {
                entityIn.hurt(DamageSourcePneumaticCraft.PLASTIC_BLOCK, 3);
                ((LivingEntity) entityIn).addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 40, 1));
            }
        }
    }

    @Override
    public int getTintColor(BlockState state, @Nullable IBlockDisplayReader world, @Nullable BlockPos pos, int tintIndex) {
        return getColor().getColorValue();
    }

    public boolean isPathfindable(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }

    enum PartType implements IStringSerializable {
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
        public ItemPlasticBrick(BlockPlasticBrick blockPlasticBrick) {
            super(blockPlasticBrick, ModItems.defaultProps());
        }

        @Override
        public String getCustomTooltipTranslationKey() {
            return "block.pneumaticcraft.plastic_brick";
        }
    }
}
