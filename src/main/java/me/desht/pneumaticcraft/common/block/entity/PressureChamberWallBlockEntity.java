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

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.tileentity.IManoMeasurable;
import me.desht.pneumaticcraft.common.block.PressureChamberWallBlock.WallState;
import me.desht.pneumaticcraft.common.core.ModBlockEntities;
import me.desht.pneumaticcraft.common.thirdparty.waila.IInfoForwarder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static me.desht.pneumaticcraft.common.block.AbstractPressureWallBlock.WALL_STATE;

public class PressureChamberWallBlockEntity extends AbstractTickingBlockEntity implements IManoMeasurable, IInfoForwarder {
    private PressureChamberValveBlockEntity teValve;  // lazily inited in getPrimaryValve()
    private BlockPos valvePos;  // only used for serialization to/from NBT

    public PressureChamberWallBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.PRESSURE_CHAMBER_WALL.get(), pos, state, 0);
    }

    PressureChamberWallBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int upgradeSize) {
        super(type, pos, state, upgradeSize);
    }

    public PressureChamberValveBlockEntity getPrimaryValve() {
        if (teValve == null && valvePos != null) {
            setPrimaryValve(nonNullLevel().getBlockEntity(valvePos) instanceof PressureChamberValveBlockEntity v ? v : null);
        }
        return teValve;
    }

    void setPrimaryValve(PressureChamberValveBlockEntity newCore) {
        if (teValve != newCore && !nonNullLevel().isClientSide) {
            teValve = newCore;
            valvePos = teValve == null ? null : teValve.getBlockPos();
            // defer updating the blockstate since this can get called during placement, and updating the blockstate
            // then can cause the TE to change, losing the reference to the valve TE
            // https://github.com/TeamPneumatic/pnc-repressurized/issues/1049
            PressureWallStateManager.addDeferredUpdate(this, teValve);
            setChanged();
        }
    }

    public void onBlockBreak() {
        teValve = getPrimaryValve();
        if (teValve != null) {
            teValve.onMultiBlockBreak();
        }
    }

    private void updateBlockState(PressureChamberValveBlockEntity valve) {
        if (!this.isRemoved() && (valve == null || !valve.isRemoved()) && getBlockState().hasProperty(WALL_STATE)) {
            nonNullLevel().setBlock(getBlockPos(), calcNewBlockState(valve), 2);
        }
    }

    private BlockState calcNewBlockState(PressureChamberValveBlockEntity valve) {
        WallState wallState = WallState.NONE;
        if (valve != null && !valve.isRemoved()) {
            boolean xMin = getBlockPos().getX() == valve.multiBlockX;
            boolean yMin = getBlockPos().getY() == valve.multiBlockY;
            boolean zMin = getBlockPos().getZ() == valve.multiBlockZ;
            boolean xMax = getBlockPos().getX() == valve.multiBlockX + valve.multiBlockSize - 1;
            boolean yMax = getBlockPos().getY() == valve.multiBlockY + valve.multiBlockSize - 1;
            boolean zMax = getBlockPos().getZ() == valve.multiBlockZ + valve.multiBlockSize - 1;

            // Corners
            if (xMin && yMin && zMin || xMax && yMax && zMax) {
                wallState = WallState.XMIN_YMIN_ZMIN;
            } else if (xMin && yMin && zMax || xMax && yMax && zMin) {
                wallState = WallState.XMIN_YMIN_ZMAX;
            } else if (xMin && yMax && zMax || xMax && yMin && zMin) {
                wallState = WallState.XMIN_YMAX_ZMAX;
            } else if (xMin && yMax && zMin || xMax && yMin && zMax) {
                wallState = WallState.XMIN_YMAX_ZMIN;
            }
            // Edges
            else if (yMin && xMin || yMax && xMax || yMin && xMax || yMax && xMin) {
                wallState = WallState.XEDGE;
            } else if (yMin && zMin || yMax && zMax || yMin && zMax || yMax && zMin) {
                wallState = WallState.ZEDGE;
            } else if (!yMin && !yMax) {
                if (xMin && zMin || xMax && zMax || xMin && zMax || xMax && zMin) {
                    wallState = WallState.YEDGE;
                } else {
                    wallState = WallState.CENTER;
                }
            } else {
                wallState = WallState.CENTER;
            }
        }
        return getBlockState().setValue(WALL_STATE, wallState);
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

        if (tag.getBoolean("noValve")) {
            valvePos = null;
        } else if (tag.contains("valvePos")) {
            valvePos = NbtUtils.readBlockPos(tag.getCompound("valvePos"));
        } else {
            // legacy
            valvePos = new BlockPos(tag.getInt("valveX"), tag.getInt("valveY"), tag.getInt("valveZ"));
        }
        teValve = null;
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (valvePos == null) {
            tag.putBoolean("noValve", true);
        } else {
            tag.put("valvePos", NbtUtils.writeBlockPos(valvePos));
        }
    }

    @Override
    public void printManometerMessage(Player player, List<Component> curInfo) {
        if (getPrimaryValve() != null) {
            teValve.airHandler.printManometerMessage(player, curInfo);
        }
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
    }

    @Override
    public BlockEntity getInfoBlockEntity(){
        return getPrimaryValve();
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID)
    public static class PressureWallStateManager
    {
        private static final Deque<WallAndValve> todo = new ArrayDeque<>();

        @SubscribeEvent
        public static void onServerTick(TickEvent.ServerTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                while (!todo.isEmpty()) {
                    WallAndValve element = todo.poll();
                    element.wall().updateBlockState(element.valve());
                }
            }
        }

        private static void addDeferredUpdate(PressureChamberWallBlockEntity wall, PressureChamberValveBlockEntity valve) {
            todo.offer(new WallAndValve(wall, valve));
        }

        private record WallAndValve(PressureChamberWallBlockEntity wall, PressureChamberValveBlockEntity valve) {
        }
    }
}
