package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySentryTurret;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.NBTKeys;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.List;

public class BlockSentryTurret extends BlockPneumaticCraft {
    private final VoxelShape BOUNDS = Block.makeCuboidShape(3, 0, 3, 13, 14, 13);

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
        CompoundNBT tag = stack.getChildTag(NBTKeys.BLOCK_ENTITY_TAG);
        if (tag != null && tag.contains(TileEntitySentryTurret.NBT_ENTITY_FILTER, Constants.NBT.TAG_STRING)) {
            curInfo.add(new TranslationTextComponent("pneumaticcraft.gui.entityFilter")
                    .appendString(": " + tag.getString(TileEntitySentryTurret.NBT_ENTITY_FILTER)).mergeStyle(TextFormatting.YELLOW));
        }
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, entity, stack);

        PneumaticCraftUtils.getTileEntityAt(world, pos, TileEntitySentryTurret.class)
                .ifPresent(te -> te.setIdleYaw(entity.getYaw(0f)));
    }
}
