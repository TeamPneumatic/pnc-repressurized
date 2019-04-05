package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.GuiHandler.EnumGuiId;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySentryTurret;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

public class BlockSentryTurret extends BlockPneumaticCraftModeled {
    private static final AxisAlignedBB BLOCK_BOUNDS = new AxisAlignedBB(3 / 16F, 0, 3 / 16F, 13 / 16F, 14 / 16F, 13 / 16F);
    private static final String NBT_ENTITY_FILTER = "EntityFilter";

    BlockSentryTurret() {
        super(Material.IRON, "sentry_turret");
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BLOCK_BOUNDS;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntitySentryTurret.class;
    }

    @Override
    public EnumGuiId getGuiID() {
        return EnumGuiId.SENTRY_TURRET;
    }

    @Override
    public void addExtraInformation(ItemStack stack, World world, List<String> curInfo, ITooltipFlag flag) {
        if (NBTUtil.hasTag(stack, NBT_ENTITY_FILTER)) {
            curInfo.add(TextFormatting.GOLD + "Entity Filter: " + NBTUtil.getString(stack, NBT_ENTITY_FILTER));
        }
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        super.getDrops(drops, world, pos, state, fortune);

        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntitySentryTurret && ((TileEntitySentryTurret) te).shouldPreserveStateOnBreak()) {
            String filter = ((TileEntitySentryTurret) te).getText(0);
            if (filter != null && !filter.isEmpty()) {
                ItemStack teStack = drops.get(0);
                NBTUtil.setString(teStack, NBT_ENTITY_FILTER, filter);
            }
        }
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, entity, stack);

        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntitySentryTurret && NBTUtil.hasTag(stack, NBT_ENTITY_FILTER)) {
            ((TileEntitySentryTurret) te).setText(0, NBTUtil.getString(stack, NBT_ENTITY_FILTER));
        }
    }
}
