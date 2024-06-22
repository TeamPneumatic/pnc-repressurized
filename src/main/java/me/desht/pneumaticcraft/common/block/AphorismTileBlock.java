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
import me.desht.pneumaticcraft.client.gui.AphorismTileScreen;
import me.desht.pneumaticcraft.common.block.entity.utility.AphorismTileBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.utility.AphorismTileBlockEntity.SavedData;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.Tags;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class AphorismTileBlock extends AbstractPneumaticCraftBlock implements ColorHandlers.ITintableBlock, PneumaticCraftEntityBlock {
    public static final float APHORISM_TILE_THICKNESS = 1 / 16F;
    public static final BooleanProperty INVISIBLE = BooleanProperty.create("invisible");

    private static final VoxelShape[] SHAPES = new VoxelShape[]{
            Block.box(0, 0, 0, 16, 1, 16),
            Block.box(0, 15, 0, 16, 16, 16),
            Block.box(0, 0, 0, 16, 16, 1),
            Block.box(0, 0, 15, 16, 16, 16),
            Block.box(0, 0, 0, 1, 16, 16),
            Block.box(15, 0, 0, 16, 16, 16),
    };

    public AphorismTileBlock() {
        super(Block.Properties.of().mapColor(MapColor.QUARTZ).strength(1.5f, 4.0f).noCollission());
        registerDefaultState(defaultBlockState().setValue(INVISIBLE, false));
    }

    @Override
    protected boolean isWaterloggable() {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(INVISIBLE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext selectionContext) {
        if (state.getBlock() == ModBlocks.APHORISM_TILE.get() && state.getValue(AphorismTileBlock.INVISIBLE)) {
            // bad mapping: should be isSneaking()
            return selectionContext.isDescending() ? SHAPES[getRotation(state).get3DDataValue()] : Shapes.empty();
        } else {
            return SHAPES[getRotation(state).get3DDataValue()];
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(directionProperty(), ctx.getClickedFace().getOpposite());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return state.getBlock() == ModBlocks.APHORISM_TILE.get() && state.getValue(AphorismTileBlock.INVISIBLE) ?
                RenderShape.INVISIBLE : RenderShape.MODEL;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> curInfo, TooltipFlag flag) {
        super.appendHoverText(stack, context, curInfo, flag);

        SavedData savedData = stack.get(ModDataComponents.APHORISM_TILE_DATA);
        if (savedData != null) {
            if (!savedData.lines().isEmpty()) {
                curInfo.add(xlate("gui.tooltip.block.pneumaticcraft.aphorism_tile.text").withStyle(ChatFormatting.YELLOW));
                savedData.lines().forEach(line -> curInfo.add(Component.literal("  " + line).withStyle(ChatFormatting.ITALIC)));
            }
            curInfo.add(xlate("gui.tooltip.block.pneumaticcraft.aphorism_tile.reset").withStyle(ChatFormatting.DARK_GREEN));
        }
    }

    /**
     * Called when the block is placed in the world.
     */
    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity entityLiving, ItemStack stack) {
        super.setPlacedBy(world, pos, state, entityLiving, stack);

        if (world.isClientSide) {
            PneumaticCraftUtils.getBlockEntityAt(world, pos, AphorismTileBlockEntity.class).ifPresent(teAT -> {
                SavedData savedData = stack.get(ModDataComponents.APHORISM_TILE_DATA);
                if (savedData != null) {
                    teAT.loadSavedData(savedData);
                }
                AphorismTileScreen.openGui(teAT, true);
                if (entityLiving instanceof Player p) {
                    sendEditorMessage(p);
                }
            });
        }
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult brtr) {
        BlockEntity te = world.getBlockEntity(pos);
        if (!(te instanceof AphorismTileBlockEntity teAT)) {
            return ItemInteractionResult.FAIL;
        }

        if (!world.isClientSide && player.getItemInHand(hand).is(Tags.Items.DYES) && !teAT.isInvisible()) {
            return tryDyeTile(state, player, hand, brtr, teAT);
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (world.isClientSide) {
            return world.getBlockEntity(pos) instanceof AphorismTileBlockEntity teAT ?
                    openEditorGui(player, teAT) :
                    InteractionResult.FAIL;
        }
        return InteractionResult.CONSUME;
    }

    private ItemInteractionResult tryDyeTile(BlockState state, Player player, InteractionHand hand, BlockHitResult brtr, AphorismTileBlockEntity teAT) {
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
            return ItemInteractionResult.CONSUME;
        }
        return ItemInteractionResult.FAIL;
    }

    private InteractionResult openEditorGui(Player player, AphorismTileBlockEntity teAT) {
        AphorismTileScreen.openGui(teAT, false);
        sendEditorMessage(player);
        return InteractionResult.SUCCESS;
    }

    private boolean clickedBorder(BlockState state, Vec3 hitVec) {
        double x = Math.abs(hitVec.x - (int) hitVec.x);
        double y = Math.abs(hitVec.y - (int) hitVec.y);
        double z = Math.abs(hitVec.z - (int) hitVec.z);
        return switch (getRotation(state)) {
            case EAST, WEST -> y < 0.1 || y > 0.9 || z < 0.1 || z > 0.9;
            case NORTH, SOUTH -> y < 0.1 || y > 0.9 || x < 0.1 || x > 0.9;
            case UP, DOWN -> x < 0.1 || x > 0.9 || z < 0.1 || z > 0.9;
        };
    }

    private void sendEditorMessage(Player player) {
        Component msg = Component.literal(ChatFormatting.WHITE.toString())
                .append(Component.translatable("pneumaticcraft.gui.aphorismTileEditor"))
                .append(Component.literal(": "))
                .append(Component.translatable("pneumaticcraft.gui.holdF1forHelp"));
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
    public boolean onWrenched(Level world, Player player, BlockPos pos, Direction face, InteractionHand hand) {
        if (player != null && player.isShiftKeyDown()) {
            return PneumaticCraftUtils.getBlockEntityAt(world, pos, AphorismTileBlockEntity.class).map(teAt -> {
                teAt.setTextRotation((teAt.getTextRotation() + 1) % 4);
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
    public int getTintColor(BlockState state, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos, int tintIndex) {
        if (world != null && pos != null) {
            return PneumaticCraftUtils.getBlockEntityAt(world, pos, AphorismTileBlockEntity.class).map(teAt -> switch (tintIndex) {
                case 0 -> // border
                        DyeColor.byId(teAt.getBorderColor()).getTextureDiffuseColor();
                case 1 -> // background
                        ColorHandlers.desaturate(DyeColor.byId(teAt.getBackgroundColor()).getTextureDiffuseColor());
                default -> 0xFFFFFFFF;
            }).orElse(0xFFFFFFFF);
        }
        return 0xFFFFFFFF;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new AphorismTileBlockEntity(pPos, pState);
    }

    @Override
    public void addSerializableComponents(List<DataComponentType<?>> list) {
        super.addSerializableComponents(list);
        list.add(ModDataComponents.APHORISM_TILE_DATA.get());
    }

    public static class ItemBlockAphorismTile extends BlockItem implements ColorHandlers.ITintableItem {
        public ItemBlockAphorismTile(AphorismTileBlock blockAphorismTile) {
            super(blockAphorismTile, ModItems.defaultProps());
        }

        private static int getColor(ItemStack stack, Function<SavedData, Integer> getter, DyeColor fallback) {
            SavedData savedData = stack.get(ModDataComponents.APHORISM_TILE_DATA);
            return savedData == null ? fallback.getId() : getter.apply(savedData);
        }

        @Override
        public int getTintColor(ItemStack stack, int tintIndex) {
            return switch (tintIndex) {
                case 0 -> // border
                        DyeColor.byId(getColor(stack, SavedData::borderColor, DyeColor.BLUE)).getTextureDiffuseColor();
                case 1 -> // background
                        ColorHandlers.desaturate(DyeColor.byId(getColor(stack, SavedData::bgColor, DyeColor.WHITE)).getTextureDiffuseColor());
                default -> 0xFFFFFF;
            };
        }
    }
}
