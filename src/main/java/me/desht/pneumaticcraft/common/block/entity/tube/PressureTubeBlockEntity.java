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

package me.desht.pneumaticcraft.common.block.entity.tube;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.api.tileentity.IAirListener;
import me.desht.pneumaticcraft.api.tileentity.IManoMeasurable;
import me.desht.pneumaticcraft.common.block.PressureTubeBlock;
import me.desht.pneumaticcraft.common.block.entity.AbstractAirHandlingBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.CamouflageableBlockEntity;
import me.desht.pneumaticcraft.common.item.TubeModuleItem;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.tubemodules.AbstractTubeModule;
import me.desht.pneumaticcraft.common.tubemodules.IInfluenceDispersing;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.RayTraceUtils;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

public class PressureTubeBlockEntity extends AbstractAirHandlingBlockEntity implements IAirListener, IManoMeasurable, CamouflageableBlockEntity {
    @DescSynced
    private final boolean[] sidesClosed = new boolean[6];
    private final EnumMap<Direction, AbstractTubeModule> modules = new EnumMap<>(Direction.class);
    private BlockState camoState;
    private AABB renderBoundingBox = null;
    private Direction inLineModuleDir = null;  // only one inline module allowed
    private final List<Direction> neighbourDirections = new ArrayList<>();
    private VoxelShape cachedTubeShape = null; // important for performance
    private int pendingCacheShapeClear = 0;

    public PressureTubeBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntityTypes.PRESSURE_TUBE.get(), pos, state, PressureTier.TIER_ONE, PneumaticValues.VOLUME_PRESSURE_TUBE);
    }

    PressureTubeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, PressureTier tier, int volumePressureTube) {
        super(type, pos, state, tier, volumePressureTube, 0);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        byte closed = tag.getByte("sidesClosed");
        for (int i = 0; i < 6; i++) {
            sidesClosed[i] = ((closed & 1 << i) != 0);
        }
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);

        byte closed = 0;
        for (int i = 0; i < 6; i++) {
            if (sidesClosed[i]) closed |= 1 << i;
        }
        if (closed != 0) {
            nbt.putByte("sidesClosed", closed);
        }
    }

    @Override
    public void writeToPacket(CompoundTag tag) {
        super.writeToPacket(tag);

        writeModulesToNBT(tag);
        CamouflageableBlockEntity.writeCamo(tag, camoState);
    }

    public void writeModulesToNBT(CompoundTag tag) {
        ListTag moduleList = new ListTag();
        for (Direction d : DirectionUtil.VALUES) {
            AbstractTubeModule tm = getModule(d);
            if (tm != null) {
                CompoundTag moduleTag = new CompoundTag();
                moduleTag.putString("type", tm.getType().toString());
                tm.writeToNBT(moduleTag);
                moduleList.add(moduleTag);
            }
        }
        if (!moduleList.isEmpty()) {
            tag.put("modules", moduleList);
        }
    }

    @Override
    public void readFromPacket(CompoundTag tag) {
        super.readFromPacket(tag);

        clearCachedShape();

        EnumSet<Direction> dirs = EnumSet.allOf(Direction.class);
        ListTag moduleList = tag.getList("modules", Tag.TAG_COMPOUND);
        for (int i = 0; i < moduleList.size(); i++) {
            CompoundTag moduleTag = moduleList.getCompound(i);
            Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(moduleTag.getString("type")));
            Direction dir = Direction.from3DDataValue(moduleTag.getInt("dir"));
            if (item instanceof TubeModuleItem) {
                AbstractTubeModule module = ((TubeModuleItem) item).createModule(dir, this);
                module.readFromNBT(moduleTag);
                AbstractTubeModule oldModule = getModule(module.getDirection());
                if (oldModule != null && !oldModule.getType().equals(module.getType())) {
                    oldModule.onRemoved();
                }
                setModule(module.getDirection(), module);
                dirs.remove(module.getDirection());
            } else {
                Log.error("unknown tube module type: " + moduleTag.getString("type"));
            }
        }

        // any sides *not* listed in the packet data are no longer present and must be removed
        for (Direction d : dirs) {
            AbstractTubeModule module = getModule(d);
            if (module != null) {
                setModule(d, null);
            }
        }

        updateRenderBoundingBox();
        if (hasLevel() && nonNullLevel().isClientSide) {
            forceBlockEntityRerender();
        }
        camoState = CamouflageableBlockEntity.readCamo(tag);
    }

    public void updateRenderBoundingBox() {
        renderBoundingBox = new AABB(getBlockPos());

        for (Direction dir : DirectionUtil.VALUES) {
            if (modules.containsKey(dir) && modules.get(dir).getRenderBoundingBox() != null) {
                renderBoundingBox = renderBoundingBox.minmax(modules.get(dir).getRenderBoundingBox());
            }
        }
    }

    @Override
    public void tickCommonPre() {
        super.tickCommonPre();

        if (pendingCacheShapeClear > 0 && --pendingCacheShapeClear == 0) {
            cachedTubeShape = null;
        }
    }

    @Override
    public void tickClient() {
        super.tickClient();
        for (Direction dir : DirectionUtil.VALUES) {
            AbstractTubeModule tm = getModule(dir);
            if (tm != null) tm.tickClient();
        }
    }

    @Override
    public void tickServer() {
        super.tickServer();

        boolean couldLeak = true;

        for (Direction dir : DirectionUtil.VALUES) {
            AbstractTubeModule tm = getModule(dir);
            if (tm != null) {
                couldLeak = false;
                tm.tickServer();
            }
            if (isSideClosed(dir)) {
                couldLeak = false;
            }
        }

        // check for possibility of air leak due to unconnected tube
        if (couldLeak && neighbourDirections.size() == 1) {
            Direction d = neighbourDirections.get(0).getOpposite();
            airHandler.setSideLeaking(canConnectPneumatic(d) ? d : null);
        } else {
            airHandler.setSideLeaking(null);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (!nonNullLevel().isClientSide) {
            discoverConnectedNeighbors();
        }
    }

    @Override
    public void onAirDispersion(IAirHandlerMachine handler, @Nullable Direction side, int airDispersed) {
        if (side != null && getModule(side) instanceof IInfluenceDispersing dispersing) {
            dispersing.onAirDispersion(airDispersed);
        }
    }

    @Override
    public int getMaxDispersion(IAirHandlerMachine handler, @Nullable Direction side) {
        return side != null && getModule(side) instanceof IInfluenceDispersing dispersing ?
                dispersing.getMaxDispersion() :
                Integer.MAX_VALUE;
    }

    public AbstractTubeModule getModule(Direction side) {
        return modules.get(side);
    }

    public boolean isSideClosed(Direction side) {
        return sidesClosed[side.get3DDataValue()];
    }

    public void setSideClosed(Direction side, boolean closed) {
        if (sidesClosed[side.get3DDataValue()] != closed) {
            sidesClosed[side.get3DDataValue()] = closed;
            initializeHullAirHandlers();
            discoverConnectedNeighbors();
        }
    }

    public Stream<AbstractTubeModule> tubeModules() {
        return modules.values().stream().filter(Objects::nonNull);
    }

    public boolean mayPlaceModule(AbstractTubeModule module) {
        Direction side = module.getDirection();
        return inLineModuleDir == null
                && getModule(side) == null
                && !isSideClosed(side)
                && (!module.isInline() || getConnectedNeighbor(side) == null);
    }

    public void setModule(Direction side, AbstractTubeModule module) {
        if (module != null) {
            if (module.isInline()) {
                inLineModuleDir = side;
            }
        } else {
            if (inLineModuleDir == side) {
                inLineModuleDir = null;
            }
            if (modules.containsKey(side)) {
                modules.get(side).onRemoved();
            }
        }
        clearCachedShape();
        if (module != null) {
            modules.put(side, module);
        } else {
            modules.remove(side);
        }
        if (getLevel() != null && !getLevel().isClientSide) {
            getLevel().setBlock(getBlockPos(), PressureTubeBlock.recalculateState(level, worldPosition, getBlockState()), Block.UPDATE_ALL);
            sendDescriptionPacket();
            setChanged();
        }
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return side != null
                && (inLineModuleDir == null || inLineModuleDir.getAxis() == side.getAxis())
                && !isSideClosed(side)
                && (getModule(side) == null || getModule(side).isInline());
    }

    @Override
    public void onNeighborTileUpdate(BlockPos tilePos) {
        super.onNeighborTileUpdate(tilePos);

        tubeModules().filter(module -> getBlockPos().relative(module.getDirection()).equals(tilePos))
                .forEach(AbstractTubeModule::onNeighborTileUpdate);
    }

    @Override
    public void onNeighborBlockUpdate(BlockPos fromPos) {
        super.onNeighborBlockUpdate(fromPos);

        tubeModules().forEach(AbstractTubeModule::onNeighborBlockUpdate);

        discoverConnectedNeighbors();
    }

    private void discoverConnectedNeighbors() {
        neighbourDirections.clear();
        airHandler.getConnectedAirHandlers(this).forEach(connection -> neighbourDirections.add(connection.getDirection()));
    }

    @Override
    public IItemHandler getItemHandler(@org.jetbrains.annotations.Nullable Direction dir) {
        return null;
    }

    public BlockEntity getConnectedNeighbor(Direction dir) {
        AbstractTubeModule tm = getModule(dir);
        if (!isSideClosed(dir) && (tm == null || tm.isInline() && dir.getAxis() == tm.getDirection().getAxis())) {
            BlockEntity te = getCachedNeighbor(dir);
            if (te != null && PNCCapabilities.getAirHandler(te, dir.getOpposite()).isPresent()) {
                return te;
            }
        }
        return null;
    }

    public AABB getRenderBoundingBox() {
        return renderBoundingBox != null ? renderBoundingBox : new AABB(getBlockPos());
    }

    @Override
    public void printManometerMessage(Player player, List<Component> text) {
        HitResult hitResult = RayTraceUtils.getEntityLookedObject(player, PneumaticCraftUtils.getPlayerReachDistance(player));
        if (hitResult instanceof BlockHitResult blockHitResult) {
            AbstractTubeModule tm = getModule(blockHitResult.getDirection());
            if (tm != null) tm.addInfo(text);
        }
    }

    @Override
    public BlockState getCamouflage() {
        return camoState;
    }

    @Override
    public void setCamouflage(BlockState state) {
        camoState = state;
        CamouflageableBlockEntity.syncToClient(this);
    }

    public VoxelShape getCachedTubeShape(VoxelShape blockShape) {
        if (cachedTubeShape == null) {
            cachedTubeShape = blockShape;
            tubeModules().forEach(module -> cachedTubeShape = Shapes.or(cachedTubeShape, module.getShape()));
        }
        return cachedTubeShape;
    }

    public void clearCachedShape() {
        // needs to be deferred by a couple of ticks, it seems
        // otherwise the old shape (now wrong) can get recached on the client
        pendingCacheShapeClear = 2;
    }
}
