package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.client.gui.GuiAphorismTile;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAphorismTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
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
import net.minecraft.world.ILightReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.List;

import static me.desht.pneumaticcraft.common.tileentity.TileEntityAphorismTile.NBT_BACKGROUND_COLOR;
import static me.desht.pneumaticcraft.common.tileentity.TileEntityAphorismTile.NBT_BORDER_COLOR;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;
import static me.desht.pneumaticcraft.lib.NBTKeys.BLOCK_ENTITY_TAG;
import static me.desht.pneumaticcraft.lib.NBTKeys.NBT_EXTRA;

public class BlockAphorismTile extends BlockPneumaticCraft implements ColorHandlers.ITintableBlock {

    private static final VoxelShape[] SHAPES = new VoxelShape[] {
            Block.makeCuboidShape(0, 0, 0, 16,  1, 16),
            Block.makeCuboidShape(0, 15, 0, 16, 16, 16),
            Block.makeCuboidShape(0, 0, 0, 16, 16,  1),
            Block.makeCuboidShape(0, 0, 15, 16, 16, 16),
            Block.makeCuboidShape(0, 0, 0,  1, 16, 16),
            Block.makeCuboidShape(15, 0, 0, 16, 16, 16),
    };

    public BlockAphorismTile() {
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(1.5f, 4.0f).doesNotBlockMovement());
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext selectionContext) {
        return SHAPES[getRotation(state).getIndex()];
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext ctx) {
        return this.getDefaultState().with(directionProperty(), ctx.getFace().getOpposite());
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityAphorismTile.class;
    }

    @Override
    public void addInformation(ItemStack stack, IBlockReader world, List<ITextComponent> curInfo, ITooltipFlag flag) {
        super.addInformation(stack, world, curInfo, flag);

        CompoundNBT tag = stack.getChildTag(BLOCK_ENTITY_TAG);
        if (tag != null && tag.contains(NBT_EXTRA)) {
            CompoundNBT subTag = tag.getCompound(NBT_EXTRA);
            if (subTag != null && (subTag.contains(NBT_BORDER_COLOR) || subTag.contains(NBT_BACKGROUND_COLOR))) {
                ListNBT l = subTag.getList(TileEntityAphorismTile.NBT_TEXT_LINES, Constants.NBT.TAG_STRING);
                if (!l.isEmpty()) {
                    curInfo.add(xlate("gui.tooltip.block.pneumaticcraft.aphorism_tile.text").applyTextStyle(TextFormatting.YELLOW));
                    l.forEach(el -> curInfo.add(new StringTextComponent("  " + el.getString()).applyTextStyle(TextFormatting.ITALIC)));
                }
                curInfo.add(xlate("gui.tooltip.block.pneumaticcraft.aphorism_tile.reset").applyTextStyle(TextFormatting.DARK_GREEN));
            }
        }
    }

    /**
     * Called when the block is placed in the world.
     */
    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity entityLiving, ItemStack iStack) {
        super.onBlockPlacedBy(world, pos, state, entityLiving, iStack);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityAphorismTile && world.isRemote) {
            TileEntityAphorismTile teAT = (TileEntityAphorismTile) te;
            CompoundNBT tag = iStack.getChildTag(BLOCK_ENTITY_TAG);
            if (tag != null) teAT.readFromPacket(tag);
            GuiAphorismTile.openGui(teAT);
            if (entityLiving instanceof PlayerEntity) sendEditorMessage((PlayerEntity) entityLiving);
        }
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult brtr) {
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
                            if (PNCConfig.Common.General.useUpDyesWhenColoring) player.getHeldItem(hand).shrink(1);
                        }
                    } else {
                        if (teAT.getBackgroundColor() != color.getId()) {
                            teAT.setBackgroundColor(color.getId());
                            if (PNCConfig.Common.General.useUpDyesWhenColoring) player.getHeldItem(hand).shrink(1);
                        }
                    }
                }
            }
        }
        return ActionResultType.SUCCESS;
    }

    private boolean clickedBorder(BlockState state, Vec3d hitVec) {
        double x = Math.abs(hitVec.x - (int) hitVec.x);
        double y = Math.abs(hitVec.y - (int) hitVec.y);
        double z = Math.abs(hitVec.z - (int) hitVec.z);
        switch (getRotation(state)) {
            case EAST: case WEST: return y < 0.1 || y > 0.9 || z < 0.1 || z > 0.9;
            case NORTH: case SOUTH: return y < 0.1 || y > 0.9 || x < 0.1 || x > 0.9;
            case UP: case DOWN: return x < 0.1 || x > 0.9 || z < 0.1 || z > 0.9;
        }
        return false;
    }

    private void sendEditorMessage(PlayerEntity player) {
        ITextComponent msg = new StringTextComponent(TextFormatting.WHITE.toString())
                .appendSibling(new TranslationTextComponent("pneumaticcraft.gui.aphorismTileEditor"))
                .appendSibling(new StringTextComponent(": "))
                .appendSibling(new TranslationTextComponent("pneumaticcraft.gui.holdF1forHelp"));
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

    @Override
    public int getTintColor(BlockState state, @Nullable ILightReader world, @Nullable BlockPos pos, int tintIndex) {
        if (world != null && pos != null) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityAphorismTile) {
                TileEntityAphorismTile teAt = (TileEntityAphorismTile) te;
                switch (tintIndex) {
                    case 0: // border
                        return DyeColor.byId(teAt.getBorderColor()).getColorValue();
                    case 1: // background
                        return ColorHandlers.desaturate(DyeColor.byId(teAt.getBackgroundColor()).getColorValue());
                }
            }
        }
        return 0xFFFFFFFF;
    }

    public static class ItemBlockAphorismTile extends BlockItem implements ColorHandlers.ITintableItem {
        public ItemBlockAphorismTile(BlockAphorismTile blockAphorismTile) {
            super(blockAphorismTile, ModItems.defaultProps());
        }

        private static int getColor(ItemStack stack, String key, DyeColor fallback) {
            CompoundNBT tag = stack.getChildTag(BLOCK_ENTITY_TAG);
            if (tag != null && tag.contains(NBT_EXTRA)) {
                return tag.getCompound(NBT_EXTRA).getInt(key);
            }
            return fallback.getId();
        }

        @Override
        public int getTintColor(ItemStack stack, int tintIndex) {
            switch (tintIndex) {
                case 0: // border
                    return DyeColor.byId(getColor(stack, NBT_BORDER_COLOR, DyeColor.BLUE)).getColorValue();
                case 1: // background
                    return ColorHandlers.desaturate(DyeColor.byId(getColor(stack, NBT_BACKGROUND_COLOR, DyeColor.WHITE)).getColorValue());
                default:
                    return 0xFFFFFF;
            }
        }
    }
}
