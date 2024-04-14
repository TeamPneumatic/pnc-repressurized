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

package me.desht.pneumaticcraft.common.block.entity.processing;

import me.desht.pneumaticcraft.api.block.PressureChamberWallState;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.tileentity.IManoMeasurable;
import me.desht.pneumaticcraft.common.block.entity.AbstractTickingBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.IInfoForwarder;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static me.desht.pneumaticcraft.api.block.PNCBlockStateProperties.FORMED;
import static me.desht.pneumaticcraft.api.block.PNCBlockStateProperties.WALL_STATE;

public class PressureChamberWallBlockEntity extends AbstractTickingBlockEntity implements IManoMeasurable, IInfoForwarder {
    private PressureChamberValveBlockEntity teValve;  // lazily inited in getPrimaryValve()
    private BlockPos valvePos;  // only used for serialization to/from NBT

    public PressureChamberWallBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntityTypes.PRESSURE_CHAMBER_WALL.get(), pos, state, 0);
    }

    PressureChamberWallBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int upgradeSize) {
        super(type, pos, state, upgradeSize);
    }

    @Override
    public boolean hasItemCapability() {
        return false;
    }

    public PressureChamberValveBlockEntity getPrimaryValve() {
        if (teValve == null && valvePos != null) {
            setPrimaryValve(nonNullLevel().getBlockEntity(valvePos) instanceof PressureChamberValveBlockEntity v ? v : null);
        }
        return teValve;
    }

    void setPrimaryValve(PressureChamberValveBlockEntity newValve) {
        boolean valveChanging = teValve != newValve || newValve == null && valvePos != null || newValve != null && valvePos == null;
        valvePos = newValve == null ? null : newValve.getBlockPos();
        if (valveChanging && !nonNullLevel().isClientSide) {
            teValve = newValve;
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
        if (!this.isRemoved() && (valve == null || !valve.isRemoved())) {
            if (getBlockState().hasProperty(WALL_STATE)) {
                nonNullLevel().setBlock(getBlockPos(), calcNewWallState(valve), Block.UPDATE_CLIENTS);
            } else if (getBlockState().hasProperty(FORMED)) {
                nonNullLevel().setBlock(getBlockPos(), calcNewGlassState(valve), Block.UPDATE_CLIENTS);
            }
        }
    }

    private BlockState calcNewGlassState(PressureChamberValveBlockEntity valve) {
        return getBlockState().setValue(FORMED, valve != null && !valve.isRemoved());
    }

    private BlockState calcNewWallState(PressureChamberValveBlockEntity valve) {
        PressureChamberWallState wallState = PressureChamberWallState.NONE;
        if (valve != null && !valve.isRemoved()) {
            boolean xMin = getBlockPos().getX() == valve.multiBlockX;
            boolean yMin = getBlockPos().getY() == valve.multiBlockY;
            boolean zMin = getBlockPos().getZ() == valve.multiBlockZ;
            boolean xMax = getBlockPos().getX() == valve.multiBlockX + valve.multiBlockSize - 1;
            boolean yMax = getBlockPos().getY() == valve.multiBlockY + valve.multiBlockSize - 1;
            boolean zMax = getBlockPos().getZ() == valve.multiBlockZ + valve.multiBlockSize - 1;

            // Corners
            if (xMin && yMin && zMin || xMax && yMax && zMax) {
                wallState = PressureChamberWallState.XMIN_YMIN_ZMIN;
            } else if (xMin && yMin && zMax || xMax && yMax && zMin) {
                wallState = PressureChamberWallState.XMIN_YMIN_ZMAX;
            } else if (xMin && yMax && zMax || xMax && yMin && zMin) {
                wallState = PressureChamberWallState.XMIN_YMAX_ZMAX;
            } else if (xMin && yMax && zMin || xMax && yMin && zMax) {
                wallState = PressureChamberWallState.XMIN_YMAX_ZMIN;
            }
            // Edges
            else if (yMin && xMin || yMax && xMax || yMin && xMax || yMax && xMin) {
                wallState = PressureChamberWallState.XEDGE;
            } else if (yMin && zMin || yMax && zMax || yMin && zMax || yMax && zMin) {
                wallState = PressureChamberWallState.ZEDGE;
            } else if (!yMin && !yMax) {
                if (xMin && zMin || xMax && zMax || xMin && zMax || xMax && zMin) {
                    wallState = PressureChamberWallState.YEDGE;
                } else {
                    wallState = PressureChamberWallState.CENTER;
                }
            } else {
                wallState = PressureChamberWallState.CENTER;
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
    public IItemHandler getItemHandler(@Nullable Direction dir) {
        return null;
    }

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
            teValve.getAirHandler(null).printManometerMessage(player, curInfo);
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
