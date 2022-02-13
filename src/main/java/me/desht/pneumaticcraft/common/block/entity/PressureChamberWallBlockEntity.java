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

package me.desht.pneumaticcraft.common.block.entity;

import me.desht.pneumaticcraft.api.tileentity.IManoMeasurable;
import me.desht.pneumaticcraft.common.core.ModBlockEntities;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.thirdparty.waila.IInfoForwarder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

public class PressureChamberWallBlockEntity extends AbstractTickingBlockEntity
        implements IManoMeasurable, IInfoForwarder {

    private PressureChamberValveBlockEntity teValve;
    private int valveX;
    private int valveY;
    private int valveZ;

    public PressureChamberWallBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.PRESSURE_CHAMBER_WALL.get(), pos, state, 0);
    }

    PressureChamberWallBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int upgradeSize) {
        super(type, pos, state, upgradeSize);
    }

    public PressureChamberValveBlockEntity getCore() {
        if (teValve == null && (valveX != 0 || valveY != 0 || valveZ != 0)) {
            // when the saved BE equals null, check if we can retrieve it from the NBT saved coords.
            BlockEntity te = nonNullLevel().getBlockEntity(new BlockPos(valveX, valveY, valveZ));
            setCore(te instanceof PressureChamberValveBlockEntity ? (PressureChamberValveBlockEntity) te : null);
        }
        return teValve;
    }

    public void onBlockBreak() {
        teValve = getCore();
        if (teValve != null) {
            teValve.onMultiBlockBreak();
        }
    }

    void setCore(PressureChamberValveBlockEntity te) {
        if (!nonNullLevel().isClientSide) {
            if (te != null) {
                valveX = te.getBlockPos().getX();
                valveY = te.getBlockPos().getY();
                valveZ = te.getBlockPos().getZ();
            } else {
                valveX = 0;
                valveY = 0;
                valveZ = 0;
            }
        }
        boolean hasChanged = teValve != te;
        teValve = te;
        if (hasChanged && !nonNullLevel().isClientSide) {
            BlockState curState = nonNullLevel().getBlockState(getBlockPos());
            if (curState.getBlock() == ModBlocks.PRESSURE_CHAMBER_WALL.get()) {
                BlockState newState = ModBlocks.PRESSURE_CHAMBER_WALL.get().updateState(curState, getLevel(), getBlockPos());
                nonNullLevel().setBlock(getBlockPos(), newState, Block.UPDATE_CLIENTS);
            }
        }
    }

    @Override
    public void onDescUpdate() {
        super.onDescUpdate();
        teValve = null;
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return null;
    }

    /**
     * Reads a block entity from NBT.
     */
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        valveX = tag.getInt("valveX");
        valveY = tag.getInt("valveY");
        valveZ = tag.getInt("valveZ");
        teValve = null;
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("valveX", valveX);
        tag.putInt("valveY", valveY);
        tag.putInt("valveZ", valveZ);
    }

    @Override
    public void printManometerMessage(Player player, List<Component> curInfo) {
        if (getCore() != null) {
            teValve.airHandler.printManometerMessage(player, curInfo);
        }
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
    }

    @Override
    public BlockEntity getInfoBlockEntity(){
        return getCore();
    }

}
