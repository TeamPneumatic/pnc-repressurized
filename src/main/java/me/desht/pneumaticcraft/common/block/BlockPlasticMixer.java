package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.GuiHandler.EnumGuiId;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPlasticMixer;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import me.desht.pneumaticcraft.lib.BBConstants;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockPlasticMixer extends BlockPneumaticCraftModeled {

    private static final AxisAlignedBB BLOCK_BOUNDS = new AxisAlignedBB(
            BBConstants.PLASTIC_MIXER_MIN_POS, 0F, BBConstants.PLASTIC_MIXER_MIN_POS,
            BBConstants.PLASTIC_MIXER_MAX_POS, 1, BBConstants.PLASTIC_MIXER_MAX_POS
    );
    private static final AxisAlignedBB COLLISION_BOUNDS = new AxisAlignedBB(
            BBConstants.PLASTIC_MIXER_MIN_POS, 0F, BBConstants.PLASTIC_MIXER_MIN_POS,
            BBConstants.PLASTIC_MIXER_MAX_POS, 1, BBConstants.PLASTIC_MIXER_MAX_POS
    );

    BlockPlasticMixer() {
        super(Material.IRON, "plastic_mixer");
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityPlasticMixer.class;
    }

    @Override
    public EnumGuiId getGuiID() {
        return EnumGuiId.PLASTIC_MIXER;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return COLLISION_BOUNDS;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BLOCK_BOUNDS;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        super.getDrops(drops, world, pos, state, fortune);

        if (drops.isEmpty()) return;

        ItemStack teStack = drops.get(0);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityPlasticMixer) {
            int[] buffers = ((TileEntityPlasticMixer) te).dyeBuffers;
            for (int i = 0; i < 3; i++) {
                NBTUtil.setInteger(teStack, "dyeBuffer" + i, buffers[i]);
            }
        }
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, entity, stack);

        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityPlasticMixer && stack.hasTagCompound()) {
            int[] buffers = ((TileEntityPlasticMixer) te).dyeBuffers;
            for (int i = 0; i < 3; i++) {
                buffers[i] = NBTUtil.getInteger(stack, "dyeBuffer" + i);
            }
        }
    }
}
