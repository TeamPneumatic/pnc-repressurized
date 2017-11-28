package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAphorismTile;
import me.desht.pneumaticcraft.lib.BBConstants;
import me.desht.pneumaticcraft.proxy.CommonProxy.EnumGuiId;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockAphorismTile extends BlockPneumaticCraft {
    BlockAphorismTile() {
        super(Material.ROCK, "aphorism_tile");
        setHardness(1.5f);
        setResistance(4.0f);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        EnumFacing dir = state.getValue(ROTATION);
        return new AxisAlignedBB(
                dir.getFrontOffsetX() <= 0 ? 0 : 1F - BBConstants.APHORISM_TILE_THICKNESS,
                dir.getFrontOffsetY() <= 0 ? 0 : 1F - BBConstants.APHORISM_TILE_THICKNESS,
                dir.getFrontOffsetZ() <= 0 ? 0 : 1F - BBConstants.APHORISM_TILE_THICKNESS,
                dir.getFrontOffsetX() >= 0 ? 1 : BBConstants.APHORISM_TILE_THICKNESS,
                dir.getFrontOffsetY() >= 0 ? 1 : BBConstants.APHORISM_TILE_THICKNESS,
                dir.getFrontOffsetZ() >= 0 ? 1 : BBConstants.APHORISM_TILE_THICKNESS);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return getBoundingBox(blockState, worldIn, pos);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityAphorismTile.class;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    /**
     * Called when the block is placed in the world.
     */
    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entityLiving, ItemStack iStack) {
        super.onBlockPlacedBy(world, pos, state, entityLiving, iStack);
        EnumFacing rotation = getRotation(world, pos);
        if (rotation.getAxis() == Axis.Y) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityAphorismTile) {
                TileEntityAphorismTile teAT = (TileEntityAphorismTile) te;
                float yaw = entityLiving.rotationYaw; if (yaw < 0) yaw += 360;
                teAT.textRotation = (((int) yaw + 45) / 90 + 2) % 4;
                if (rotation.getFrontOffsetY() > 0 && (teAT.textRotation == 1 || teAT.textRotation == 3)) {
                    // fudge - reverse rotation if placing above, and player is facing on east/west axis
                    teAT.textRotation = 4 - teAT.textRotation;
                }
            }
        }
        if (world.isRemote && entityLiving instanceof EntityPlayer) {
            ((EntityPlayer) entityLiving).openGui(PneumaticCraftRepressurized.instance, EnumGuiId.APHORISM_TILE.ordinal(), world, pos.getX(), pos.getY(), pos.getZ());
            sendEditorMessage((EntityPlayer) entityLiving);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote && player.getHeldItem(hand).isEmpty() && !player.isSneaking()) {
            player.openGui(PneumaticCraftRepressurized.instance, EnumGuiId.APHORISM_TILE.ordinal(), world, pos.getX(), pos.getY(), pos.getZ());
            sendEditorMessage(player);
        }
        return true;
    }

    private void sendEditorMessage(EntityPlayer player) {
        ITextComponent msg = new TextComponentString(TextFormatting.WHITE.toString())
                .appendSibling(new TextComponentTranslation("gui.aphorismTileEditor"))
                .appendSibling(new TextComponentString(": "))
                .appendSibling(new TextComponentTranslation("gui.holdF1forHelp"));
        player.sendStatusMessage(msg, true);
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
    public boolean rotateBlock(World world, EntityPlayer player, BlockPos pos, EnumFacing face) {
        if (player.isSneaking()) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileEntityAphorismTile) {
                TileEntityAphorismTile teAt = (TileEntityAphorismTile) tile;
                if (++teAt.textRotation > 3) teAt.textRotation = 0;
                return true;
            } else {
                return false;
            }
        } else {
            return super.rotateBlock(world, player, pos, face);
        }
    }

    @Override
    protected boolean rotateForgeWay() {
        return false;
    }
}
