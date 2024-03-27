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

package me.desht.pneumaticcraft.common.thirdparty.computer_common;

import me.desht.pneumaticcraft.common.block.AbstractPneumaticCraftBlock;
import me.desht.pneumaticcraft.common.block.PneumaticCraftEntityBlock;
import me.desht.pneumaticcraft.common.item.CreativeTabStackProvider;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class DroneInterfaceBlock extends AbstractPneumaticCraftBlock implements PneumaticCraftEntityBlock, CreativeTabStackProvider {
    static final BooleanProperty CONNECTED = BooleanProperty.create("connected");

    public DroneInterfaceBlock() {
        super(ModBlocks.defaultProps());

        registerDefaultState(defaultBlockState().setValue(CONNECTED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        builder.add(CONNECTED);
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    protected boolean reversePlacementRotation() {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new DroneInterfaceBlockEntity(pPos, pState);
    }

    @Override
    public Stream<ItemStack> getStacksForItem() {
        return ThirdPartyManager.instance().isModTypeLoaded(ThirdPartyManager.ModType.COMPUTER) ?
                Stream.of(new ItemStack(this)) : Stream.empty();
    }
}
