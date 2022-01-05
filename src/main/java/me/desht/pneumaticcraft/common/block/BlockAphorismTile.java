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
import me.desht.pneumaticcraft.client.gui.GuiAphorismTile;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAphorismTile;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
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
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.List;

import static me.desht.pneumaticcraft.api.lib.NBTKeys.BLOCK_ENTITY_TAG;
import static me.desht.pneumaticcraft.api.lib.NBTKeys.NBT_EXTRA;
import static me.desht.pneumaticcraft.common.tileentity.TileEntityAphorismTile.NBT_BACKGROUND_COLOR;
import static me.desht.pneumaticcraft.common.tileentity.TileEntityAphorismTile.NBT_BORDER_COLOR;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class BlockAphorismTile extends BlockPneumaticCraft implements ColorHandlers.ITintableBlock {
    public static final float APHORISM_TILE_THICKNESS = 1 / 16F;
    public static final BooleanProperty INVISIBLE = BooleanProperty.create("invisible");

    private static final VoxelShape[] SHAPES = new VoxelShape[] {
            Block.box(0, 0, 0, 16,  1, 16),
            Block.box(0, 15, 0, 16, 16, 16),
            Block.box(0, 0, 0, 16, 16,  1),
            Block.box(0, 0, 15, 16, 16, 16),
            Block.box(0, 0, 0,  1, 16, 16),
            Block.box(15, 0, 0, 16, 16, 16),
    };

    public BlockAphorismTile() {
        super(Block.Properties.of(Material.STONE).strength(1.5f, 4.0f).noCollission());
        registerDefaultState(getStateDefinition().any().setValue(INVISIBLE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(INVISIBLE);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext selectionContext) {
        if (state.getBlock() == ModBlocks.APHORISM_TILE.get() && state.getValue(BlockAphorismTile.INVISIBLE)) {
            // bad mapping: should be isSneaking()
            return selectionContext.isDescending() ? SHAPES[getRotation(state).get3DDataValue()] : VoxelShapes.empty();
        } else {
            return SHAPES[getRotation(state).get3DDataValue()];
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext ctx) {
        return this.defaultBlockState().setValue(directionProperty(), ctx.getClickedFace().getOpposite());
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityAphorismTile.class;
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) {
        return state.getBlock() == ModBlocks.APHORISM_TILE.get() && state.getValue(BlockAphorismTile.INVISIBLE) ?
                BlockRenderType.INVISIBLE : BlockRenderType.MODEL;
    }

    @Override
    public void appendHoverText(ItemStack stack, IBlockReader world, List<ITextComponent> curInfo, ITooltipFlag flag) {
        super.appendHoverText(stack, world, curInfo, flag);

        CompoundNBT tag = stack.getTagElement(BLOCK_ENTITY_TAG);
        if (tag != null && tag.contains(NBT_EXTRA)) {
            CompoundNBT subTag = tag.getCompound(NBT_EXTRA);
            if (subTag.contains(NBT_BORDER_COLOR) || subTag.contains(NBT_BACKGROUND_COLOR)) {
                ListNBT l = subTag.getList(TileEntityAphorismTile.NBT_TEXT_LINES, Constants.NBT.TAG_STRING);
                if (!l.isEmpty()) {
                    curInfo.add(xlate("gui.tooltip.block.pneumaticcraft.aphorism_tile.text").withStyle(TextFormatting.YELLOW));
                    l.forEach(el -> curInfo.add(new StringTextComponent("  " + el.getAsString()).withStyle(TextFormatting.ITALIC)));
                }
                curInfo.add(xlate("gui.tooltip.block.pneumaticcraft.aphorism_tile.reset").withStyle(TextFormatting.DARK_GREEN));
            }
        }
    }

    /**
     * Called when the block is placed in the world.
     */
    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity entityLiving, ItemStack iStack) {
        super.setPlacedBy(world, pos, state, entityLiving, iStack);

        if (world.isClientSide) {
            PneumaticCraftUtils.getTileEntityAt(world, pos, TileEntityAphorismTile.class).ifPresent(teAT -> {
                CompoundNBT tag = iStack.getTagElement(BLOCK_ENTITY_TAG);
                if (tag != null) teAT.readFromPacket(tag);
                GuiAphorismTile.openGui(teAT, true);
                if (entityLiving instanceof PlayerEntity) sendEditorMessage((PlayerEntity) entityLiving);
            });
        }
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult brtr) {
        TileEntity te = world.getBlockEntity(pos);
        if (!(te instanceof TileEntityAphorismTile)) return ActionResultType.FAIL;
        TileEntityAphorismTile teAT = (TileEntityAphorismTile) te;

        if (!world.isClientSide && player.getItemInHand(hand).getItem().is(Tags.Items.DYES) && !teAT.isInvisible()) {
            return tryDyeTile(state, player, hand, brtr, teAT);
        } else if (world.isClientSide && hand == Hand.MAIN_HAND && player.getItemInHand(hand).isEmpty()) {
            return openEditorGui(player, teAT);
        }
        return ActionResultType.PASS;
    }

    private ActionResultType tryDyeTile(BlockState state, PlayerEntity player, Hand hand, BlockRayTraceResult brtr, TileEntityAphorismTile teAT) {
        DyeColor color = DyeColor.getColor(player.getItemInHand(hand));
        if (color != null) {
            if (clickedBorder(state, brtr.getLocation())) {
                if (teAT.getBorderColor() != color.getId()) {
                    teAT.setBorderColor(color.getId());
                    if (ConfigHelper.common().general.useUpDyesWhenColoring.get()) player.getItemInHand(hand).shrink(1);
                }
            } else {
                if (teAT.getBackgroundColor() != color.getId()) {
                    teAT.setBackgroundColor(color.getId());
                    if (ConfigHelper.common().general.useUpDyesWhenColoring.get()) player.getItemInHand(hand).shrink(1);
                }
            }
            return ActionResultType.CONSUME;
        }
        return ActionResultType.FAIL;
    }

    private ActionResultType openEditorGui(PlayerEntity player, TileEntityAphorismTile teAT) {
        GuiAphorismTile.openGui(teAT, false);
        sendEditorMessage(player);
        return ActionResultType.SUCCESS;
    }

    private boolean clickedBorder(BlockState state, Vector3d hitVec) {
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
                .append(new TranslationTextComponent("pneumaticcraft.gui.aphorismTileEditor"))
                .append(new StringTextComponent(": "))
                .append(new TranslationTextComponent("pneumaticcraft.gui.holdF1forHelp"));
        player.displayClientMessage(msg, true);
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
        if (player != null && player.isShiftKeyDown()) {
            return PneumaticCraftUtils.getTileEntityAt(world, pos, TileEntityAphorismTile.class).map(teAt -> {
                if (++teAt.textRotation > 3) teAt.textRotation = 0;
                teAt.sendDescriptionPacket();
                return true;
            }).orElse(false);
        } else {
            return super.onWrenched(world, player, pos, face, hand);
        }
    }

    @Override
    protected boolean rotateForgeWay() {
        return false;
    }

    @Override
    public int getTintColor(BlockState state, @Nullable IBlockDisplayReader world, @Nullable BlockPos pos, int tintIndex) {
        if (world != null && pos != null) {
            return PneumaticCraftUtils.getTileEntityAt(world, pos, TileEntityAphorismTile.class).map(teAt -> {
                switch (tintIndex) {
                    case 0: // border
                        return DyeColor.byId(teAt.getBorderColor()).getColorValue();
                    case 1: // background
                        return ColorHandlers.desaturate(DyeColor.byId(teAt.getBackgroundColor()).getColorValue());
                    default:
                        return 0xFFFFFFFF;
                }
            }).orElse(0xFFFFFFFF);
        }
        return 0xFFFFFFFF;
    }

    public static class ItemBlockAphorismTile extends BlockItem implements ColorHandlers.ITintableItem {
        public ItemBlockAphorismTile(BlockAphorismTile blockAphorismTile) {
            super(blockAphorismTile, ModItems.defaultProps());
        }

        private static int getColor(ItemStack stack, String key, DyeColor fallback) {
            CompoundNBT tag = stack.getTagElement(BLOCK_ENTITY_TAG);
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
