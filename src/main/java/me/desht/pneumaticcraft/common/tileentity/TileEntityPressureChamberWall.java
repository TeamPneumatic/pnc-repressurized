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

package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.tileentity.IManoMeasurable;
import me.desht.pneumaticcraft.common.block.BlockPressureChamberWall.WallState;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.thirdparty.waila.IInfoForwarder;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static me.desht.pneumaticcraft.common.block.BlockPressureChamberWallBase.WALL_STATE;

public class TileEntityPressureChamberWall extends TileEntityBase implements IManoMeasurable, IInfoForwarder {
    private TileEntityPressureChamberValve teValve;  // lazily inited in getPrimaryValve()
    private BlockPos valvePos;  // only used for serialization to/from NBT

    public TileEntityPressureChamberWall() {
        this(ModTileEntities.PRESSURE_CHAMBER_WALL.get(), 0);
    }

    TileEntityPressureChamberWall(TileEntityType type, int upgradeSize) {
        super(type, upgradeSize);
    }

    public TileEntityPressureChamberValve getPrimaryValve() {
        if (teValve == null && valvePos != null) {
            TileEntity te = getLevel().getBlockEntity(valvePos);
            setPrimaryValve(te instanceof TileEntityPressureChamberValve ? (TileEntityPressureChamberValve) te : null);
        }
        return teValve;
    }

    void setPrimaryValve(TileEntityPressureChamberValve newCore) {
        if (teValve != newCore && !getLevel().isClientSide) {
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

    private void updateBlockState(TileEntityPressureChamberValve valve) {
        if (!this.isRemoved() && (valve == null || !valve.isRemoved()) && getBlockState().hasProperty(WALL_STATE)) {
            getLevel().setBlock(getBlockPos(), calcNewBlockState(valve), 2);
        }
    }

    private BlockState calcNewBlockState(TileEntityPressureChamberValve valve) {
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

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);

        if (tag.getBoolean("noValve")) {
            valvePos = null;
        } else if (tag.contains("valvePos")) {
            valvePos = NBTUtil.readBlockPos(tag.getCompound("valvePos"));
        } else {
            // legacy
            valvePos = new BlockPos(tag.getInt("valveX"), tag.getInt("valveY"), tag.getInt("valveZ"));
        }
        teValve = null;
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        super.save(tag);
        if (valvePos == null) {
            tag.putBoolean("noValve", true);
        } else {
            tag.put("valvePos", NBTUtil.writeBlockPos(valvePos));
        }
        return tag;
    }

    @Override
    public void printManometerMessage(PlayerEntity player, List<ITextComponent> curInfo) {
        if (getPrimaryValve() != null) {
            teValve.airHandler.printManometerMessage(player, curInfo);
        }
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
    }

    @Override
    public TileEntity getInfoTileEntity() {
        return getPrimaryValve();
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID)
    public static class PressureWallStateManager
    {
        private static final Deque<Pair<TileEntityPressureChamberWall,TileEntityPressureChamberValve>> todo = new ArrayDeque<>();

        @SubscribeEvent
        public static void onServerTick(TickEvent.ServerTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                while (!todo.isEmpty()) {
                    Pair<TileEntityPressureChamberWall,TileEntityPressureChamberValve> pair = todo.poll();
                    pair.getLeft().updateBlockState(pair.getRight());
                }
            }
        }

        private static void addDeferredUpdate(TileEntityPressureChamberWall wall, TileEntityPressureChamberValve valve) {
            todo.offer(Pair.of(wall, valve));
        }
    }
}
