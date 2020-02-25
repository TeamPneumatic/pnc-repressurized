package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.DamageSourcePneumaticCraft;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ICustomTooltipName;
import me.desht.pneumaticcraft.common.item.ITintableItem;
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
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class BlockPlasticBrick extends Block {
    private static final VoxelShape COLLISION_SHAPE = makeCuboidShape(0, 0, 0, 16, 15, 16);

    private static final EnumProperty<PartType> X_PART = EnumProperty.create("x_part", PartType.class);
    private static final EnumProperty<PartType> Z_PART = EnumProperty.create("z_part", PartType.class);
    private final DyeColor color;

    public BlockPlasticBrick(DyeColor color) {
        super(ModBlocks.defaultProps().sound(SoundType.WOOD).hardnessAndResistance(2f));
        this.color = color;

        setDefaultState(getStateContainer().getBaseState().with(X_PART, PartType.NONE).with(Z_PART, PartType.NONE));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    public DyeColor getColor() {
        return color;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);

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

        return calcParts(ctx.getWorld(), ctx.getPos(), state);
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        return calcParts(worldIn, currentPos, stateIn);
    }

    private BlockState calcParts(IWorld world, BlockPos pos, BlockState stateIn) {
        Block w = world.getBlockState(pos.west()).getBlock();
        Block e = world.getBlockState(pos.east()).getBlock();
        PartType xType = PartType.NONE;
        if (w == this || e == this) {
            boolean bx = ((pos.getX() + pos.getY()) & 0x1) == 0;
            xType = bx ? PartType.RIGHT : PartType.LEFT;
        }

        Block n = world.getBlockState(pos.north()).getBlock();
        Block s = world.getBlockState(pos.south()).getBlock();
        PartType zType = PartType.NONE;
        if (n == this || s == this) {
            boolean bz = ((pos.getZ() + pos.getY()) & 0x1) == 0;
            zType = bz ? PartType.LEFT : PartType.RIGHT;
        }

        return stateIn.with(X_PART, xType).with(Z_PART, zType);
    }

    @Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        if (entityIn instanceof LivingEntity) {
            ItemStack stack = ((LivingEntity) entityIn).getItemStackFromSlot(EquipmentSlotType.FEET);
            if (stack.isEmpty()) {
                entityIn.attackEntityFrom(DamageSourcePneumaticCraft.PLASTIC_BLOCK, 3);
                ((LivingEntity) entityIn).addPotionEffect(new EffectInstance(Effects.SLOWNESS, 40, 1));
            }
        }
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
        public String getName() {
            return name;
        }
    }

    public static class ItemPlasticBrick extends BlockItem implements ITintableItem, ICustomTooltipName {
        public ItemPlasticBrick(BlockPlasticBrick blockPlasticBrick) {
            super(blockPlasticBrick, ModItems.defaultProps());
        }

        @Override
        public int getTintColor(ItemStack stack, int tintIndex) {
            Block b = ((BlockItem) stack.getItem()).getBlock();
            if (b instanceof BlockPlasticBrick) {
                return ((BlockPlasticBrick) b).getColor().getColorValue();
            }
            return 0xFFFFFFF;
        }

        @Override
        public String getCustomTooltipTranslationKey() {
            return "block.pneumaticcraft.plastic_brick";
        }
    }
}
