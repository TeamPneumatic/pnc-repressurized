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

import me.desht.pneumaticcraft.api.lib.NBTKeys;
import me.desht.pneumaticcraft.common.block.entity.SentryTurretBlockEntity;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class SentryTurretBlock extends AbstractPneumaticCraftBlock implements PneumaticCraftEntityBlock {
    private final VoxelShape BOUNDS = Stream.of(
            Block.box(4, 8, 4, 12, 14, 12),
            Block.box(3, 1, 3, 13, 5, 13),
            Block.box(7, 6, 7, 9, 8, 9),
            Block.box(6, 5, 6, 10, 6, 10),
            Block.box(0, 3.5, 14, 2, 4.5, 16),
            Block.box(0.5, 3, 14.5, 1.5, 4, 15.5),
            Block.box(0, 0, 14, 2, 3, 16),
            Block.box(14, 0, 14, 16, 3, 16),
            Block.box(14.5, 3, 14.5, 15.5, 4, 15.5),
            Block.box(14, 3.5, 14, 16, 4.5, 16),
            Block.box(0.5, 3, 0.5, 1.5, 4, 1.5),
            Block.box(0, 3.5, 0, 2, 4.5, 2),
            Block.box(0, 0, 0, 2, 3, 2),
            Block.box(14, 0, 0, 16, 3, 2),
            Block.box(14.5, 3, 0.5, 15.5, 4, 1.5),
            Block.box(14, 3.5, 0, 16, 4.5, 2)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

    public SentryTurretBlock() {
        super(ModBlocks.defaultProps());
    }

    @Override
    protected boolean isWaterloggable() {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState p_220053_1_, BlockGetter p_220053_2_, BlockPos p_220053_3_, CollisionContext p_220053_4_) {
        return BOUNDS;
    }

    @Override
    public void addExtraInformation(ItemStack stack, BlockGetter world, List<Component> curInfo, TooltipFlag flag) {
        CompoundTag tag = stack.getTagElement(NBTKeys.BLOCK_ENTITY_TAG);
        if (tag != null && tag.contains(SentryTurretBlockEntity.NBT_ENTITY_FILTER, Tag.TAG_STRING)) {
            curInfo.add(Component.translatable("pneumaticcraft.gui.entityFilter")
                    .append(": " + tag.getString(SentryTurretBlockEntity.NBT_ENTITY_FILTER)).withStyle(ChatFormatting.YELLOW));
        }
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(world, pos, state, entity, stack);

        world.getBlockEntity(pos, ModBlockEntityTypes.SENTRY_TURRET.get())
                .ifPresent(te -> te.setIdleYaw(entity.getViewYRot(0f)));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new SentryTurretBlockEntity(pPos, pState);
    }
}
