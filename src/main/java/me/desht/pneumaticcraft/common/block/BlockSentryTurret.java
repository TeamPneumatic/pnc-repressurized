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
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySentryTurret;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.List;
import java.util.stream.Stream;

public class BlockSentryTurret extends BlockPneumaticCraft {
    private final VoxelShape BOUNDS = Stream.of(
            Block.box(3, 8, 3, 13, 16, 13),
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
    ).reduce((v1, v2) -> VoxelShapes.join(v1, v2, IBooleanFunction.OR)).get();

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
        CompoundNBT tag = stack.getTagElement(NBTKeys.BLOCK_ENTITY_TAG);
        if (tag != null && tag.contains(TileEntitySentryTurret.NBT_ENTITY_FILTER, Constants.NBT.TAG_STRING)) {
            curInfo.add(new TranslationTextComponent("pneumaticcraft.gui.entityFilter")
                    .append(": " + tag.getString(TileEntitySentryTurret.NBT_ENTITY_FILTER)).withStyle(TextFormatting.YELLOW));
        }
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(world, pos, state, entity, stack);

        PneumaticCraftUtils.getTileEntityAt(world, pos, TileEntitySentryTurret.class)
                .ifPresent(te -> te.setIdleYaw(entity.getViewYRot(0f)));
    }
}
