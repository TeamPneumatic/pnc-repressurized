package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySentryTurret;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.List;

public class BlockSentryTurret extends BlockPneumaticCraft {
    private final VoxelShape BOUNDS = Block.makeCuboidShape(3, 0, 3, 13, 14, 13);

    private static final String NBT_ENTITY_FILTER = "EntityFilter";

    public BlockSentryTurret() {
        super(ModBlocks.defaultProps());
    }

    @Override
    public VoxelShape getShape(BlockState p_220053_1_, IBlockReader p_220053_2_, BlockPos p_220053_3_, ISelectionContext p_220053_4_) {
        return BOUNDS;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntitySentryTurret.class;
    }

    @Override
    public void addExtraInformation(ItemStack stack, IBlockReader world, List<ITextComponent> curInfo, ITooltipFlag flag) {
        if (NBTUtil.hasTag(stack, NBT_ENTITY_FILTER)) {
            curInfo.add(new StringTextComponent("Entity Filter: " + NBTUtil.getString(stack, NBT_ENTITY_FILTER)).applyTextStyle(TextFormatting.GOLD));
        }
    }

    // todo 1.14 copy entity filter NBT with loot table
//    @Override
//    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, BlockState state, int fortune) {
//        super.getDrops(drops, world, pos, state, fortune);
//
//        TileEntity te = world.getTileEntity(pos);
//        if (te instanceof TileEntitySentryTurret && ((TileEntitySentryTurret) te).shouldPreserveStateOnBreak()) {
//            String filter = ((TileEntitySentryTurret) te).getText(0);
//            if (filter != null && !filter.isEmpty()) {
//                ItemStack teStack = drops.get(0);
//                NBTUtil.setString(teStack, NBT_ENTITY_FILTER, filter);
//            }
//        }
//    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, entity, stack);

        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntitySentryTurret && NBTUtil.hasTag(stack, NBT_ENTITY_FILTER)) {
            ((TileEntitySentryTurret) te).setText(0, NBTUtil.getString(stack, NBT_ENTITY_FILTER));
        }
    }
}
