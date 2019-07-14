package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.client.gui.GuiAphorismTile;
import me.desht.pneumaticcraft.common.config.Config;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAphorismTile;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class BlockAphorismTile extends BlockPneumaticCraft {

    private static final String NBT_BORDER_COLOR = "borderColor";
    private static final String NBT_BACKGROUND_COLOR = "backgroundColor";

    private static final VoxelShape[] SHAPES = new VoxelShape[] {
            Block.makeCuboidShape(0, 0, 0, 16,  1, 16),
            Block.makeCuboidShape(0, 1, 0, 16, 16, 16),
            Block.makeCuboidShape(0, 0, 0, 16, 16,  1),
            Block.makeCuboidShape(0, 0, 1, 16, 16, 16),
            Block.makeCuboidShape(0, 0, 0,  1, 16, 16),
            Block.makeCuboidShape(1, 0, 0, 16, 16, 16),
    };

    public BlockAphorismTile() {
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(1.5f, 4.0f).doesNotBlockMovement(), "aphorism_tile");
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext selectionContext) {
        return SHAPES[state.get(ROTATION).getIndex()];
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityAphorismTile.class;
    }

    @Override
    public void addInformation(ItemStack stack, IBlockReader world, List<ITextComponent> curInfo, ITooltipFlag flag) {
        super.addInformation(stack, world, curInfo, flag);
        if (NBTUtil.hasTag(stack, NBT_BORDER_COLOR) || NBTUtil.hasTag(stack, NBT_BACKGROUND_COLOR)) {
            curInfo.add(xlate("gui.tab.info.tile.aphorism_tile.color").applyTextStyles(TextFormatting.DARK_GREEN, TextFormatting.ITALIC));
        }
    }

    /**
     * Called when the block is placed in the world.
     */
    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity entityLiving, ItemStack iStack) {
        super.onBlockPlacedBy(world, pos, state, entityLiving, iStack);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityAphorismTile) {
            TileEntityAphorismTile teAT = (TileEntityAphorismTile) te;

            teAT.setBackgroundColor(getBackgroundColor(iStack));
            teAT.setBorderColor(getBorderColor(iStack));

            Direction rotation = getRotation(world, pos);
            if (rotation.getAxis() == Axis.Y) {
                float yaw = entityLiving.rotationYaw;
                if (yaw < 0) yaw += 360;
                teAT.textRotation = (((int) yaw + 45) / 90 + 2) % 4;
                if (rotation.getYOffset() > 0 && (teAT.textRotation == 1 || teAT.textRotation == 3)) {
                    // fudge - reverse rotation if placing above, and player is facing on east/west axis
                    teAT.textRotation = 4 - teAT.textRotation;
                }
            }

            if (world.isRemote && entityLiving instanceof PlayerEntity) {
                GuiAphorismTile.openGui(teAT);
                sendEditorMessage((PlayerEntity) entityLiving);
            }
        }
    }

    // todo 1.14 loot table copy colors to itemstack
//    @Override
//    public void getDrops(NonNullList<ItemStack> drops, IBlockReader world, BlockPos pos, BlockState state, int fortune) {
//        super.getDrops(drops, world, pos, state, fortune);
//        TileEntity te = world.getTileEntity(pos);
//        if (te instanceof TileEntityAphorismTile && drops.size() > 0) {
//            TileEntityAphorismTile teAT = (TileEntityAphorismTile) te;
//            ItemStack teStack = drops.get(0);
//            int bgColor = teAT.getBackgroundColor();
//            int borderColor = teAT.getBorderColor();
//            if (bgColor != DyeColor.WHITE.getDyeDamage() || borderColor != DyeColor.BLUE.getDyeDamage()) {
//                NBTUtil.setInteger(teStack, NBT_BACKGROUND_COLOR, bgColor);
//                NBTUtil.setInteger(teStack, NBT_BORDER_COLOR, borderColor);
//            }
//        }
//    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult brtr) {
        if (world.isRemote && hand != Hand.OFF_HAND && player.getHeldItem(hand).isEmpty() && !player.isSneaking()) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityAphorismTile) {
                GuiAphorismTile.openGui((TileEntityAphorismTile) te);
                sendEditorMessage(player);
            }
        } else if (!world.isRemote) {
            DyeColor color =  DyeColor.getColor(player.getHeldItem(hand));
            if (color != null) {
                TileEntity te = world.getTileEntity(pos);
                if (te instanceof TileEntityAphorismTile) {
                    TileEntityAphorismTile teAT = (TileEntityAphorismTile) te;
                    if (clickedBorder(state, brtr.getHitVec())) {
                        if (teAT.getBorderColor() != color.getId()) {
                            teAT.setBorderColor(color.getId());
                            if (Config.Common.General.useUpDyesWhenColoring) player.getHeldItem(hand).shrink(1);
                        }
                    } else {
                        if (teAT.getBackgroundColor() != color.getId()) {
                            teAT.setBackgroundColor(color.getId());
                            if (Config.Common.General.useUpDyesWhenColoring) player.getHeldItem(hand).shrink(1);
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean clickedBorder(BlockState state, Vec3d hitVec) {
        switch (getRotation(state)) {
            case EAST: case WEST: return hitVec.y < 0.1 || hitVec.y > 0.9 || hitVec.z < 0.1 || hitVec.z > 0.9;
            case NORTH: case SOUTH: return hitVec.y < 0.1 || hitVec.y > 0.9 || hitVec.x < 0.1 || hitVec.x > 0.9;
            case UP: case DOWN: return hitVec.x < 0.1 || hitVec.x > 0.9 || hitVec.z < 0.1 || hitVec.z > 0.9;
        }
        return false;
    }

    private void sendEditorMessage(PlayerEntity player) {
        ITextComponent msg = new StringTextComponent(TextFormatting.WHITE.toString())
                .appendSibling(new TranslationTextComponent("gui.aphorismTileEditor"))
                .appendSibling(new StringTextComponent(": "))
                .appendSibling(new TranslationTextComponent("gui.holdF1forHelp"));
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
    public boolean onWrenched(World world, PlayerEntity player, BlockPos pos, Direction face, Hand hand) {
        if (player != null && player.isSneaking()) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileEntityAphorismTile) {
                TileEntityAphorismTile teAt = (TileEntityAphorismTile) tile;
                if (++teAt.textRotation > 3) teAt.textRotation = 0;
                teAt.sendDescriptionPacket();
                return true;
            } else {
                return false;
            }
        } else {
            return super.onWrenched(world, player, pos, face, hand);
        }
    }

    @Override
    protected boolean rotateForgeWay() {
        return false;
    }

    public static int getBackgroundColor(ItemStack stack) {
        return NBTUtil.hasTag(stack, NBT_BACKGROUND_COLOR) ? NBTUtil.getInteger(stack, NBT_BACKGROUND_COLOR) : DyeColor.WHITE.getId();
    }

    public static int getBorderColor(ItemStack stack) {
        return NBTUtil.hasTag(stack, NBT_BORDER_COLOR) ? NBTUtil.getInteger(stack, NBT_BORDER_COLOR) : DyeColor.BLUE.getId();
    }
}
