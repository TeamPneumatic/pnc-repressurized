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
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;


public class PressureTubeBlockEntity extends AbstractAirHandlingBlockEntity implements IAirListener, IManoMeasurable, CamouflageableBlockEntity {
    public static final ModelProperty<Short> CONNECTION_PROPERTY = new ModelProperty<>();

    @DescSynced
    private int sidesClosed;
    @DescSynced
    private int sidesVisuallyConnected;  // tubes with only one real connection also show the opposite side visually connected
    @DescSynced
    private Direction inLineModuleDir = null;  // only one inline module allowed

    private int sidesActuallyConnected;  // sides actually connected for air-handling purposes
    private final EnumMap<Direction, AbstractTubeModule> modules = new EnumMap<>(Direction.class);
    private BlockState camoState;
    private AABB renderBoundingBox = null;
    private Integer shapeCacheKey = null;  // key into PressureTubeBlock#SHAPE_CACHE; very important for performance

    public PressureTubeBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntityTypes.PRESSURE_TUBE.get(), pos, state, PressureTier.TIER_ONE, PneumaticValues.VOLUME_PRESSURE_TUBE);
    }

    PressureTubeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, PressureTier tier, int volumePressureTube) {
        super(type, pos, state, tier, volumePressureTube, 0);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        sidesClosed = tag.getInt("sidesClosed");
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);

        if (sidesClosed != 0) {
            tag.putInt("sidesClosed", sidesClosed);
        }
    }

    @Override
    public void writeToPacket(CompoundTag tag, HolderLookup.Provider provider) {
        super.writeToPacket(tag, provider);

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
    public void readFromPacket(CompoundTag tag, HolderLookup.Provider provider) {
        super.readFromPacket(tag, provider);

        EnumSet<Direction> dirs = EnumSet.allOf(Direction.class);
        ListTag moduleList = tag.getList("modules", Tag.TAG_COMPOUND);
        for (int i = 0; i < moduleList.size(); i++) {
            CompoundTag moduleTag = moduleList.getCompound(i);
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(moduleTag.getString("type")));
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

    @Override
    public void onDescUpdate() {
        super.onDescUpdate();

        requestModelDataUpdate();
        purgeShapeCacheKey();
    }

    private void updateRenderBoundingBox() {
        renderBoundingBox = new AABB(getBlockPos());

        for (Direction dir : DirectionUtil.VALUES) {
            if (modules.containsKey(dir) && modules.get(dir).getRenderBoundingBox() != null) {
                renderBoundingBox = renderBoundingBox.minmax(modules.get(dir).getRenderBoundingBox());
            }
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
        Direction leakDir = couldLeak ? getLeakDir() : null;
        airHandler.setSideLeaking(canConnectPneumatic(leakDir) ? leakDir : null);
    }

    private Direction getLeakDir() {
        return switch (sidesActuallyConnected) {
            case 1 -> Direction.UP;
            case 2 -> Direction.DOWN;
            case 4 -> Direction.SOUTH;
            case 8 -> Direction.NORTH;
            case 16 -> Direction.EAST;
            case 32 -> Direction.WEST;
            default -> null;
        };
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (!nonNullLevel().isClientSide) {
            discoverConnectedNeighbors();
        }

        tubeModules().forEach(AbstractTubeModule::onPlaced);
        purgeShapeCacheKey();
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

    public boolean isSideClosed(Direction dir) {
        return DirectionUtil.getDirectionBit(sidesClosed, dir);
    }

    public void setSideClosed(Direction dir, boolean closed) {
        byte b = DirectionUtil.setDirectionBit(sidesClosed, dir, closed);
        if (b != sidesClosed) {
            sidesClosed = b;
            initializeHullAirHandlers();
            discoverConnectedNeighbors();
            setChanged();
            level.blockUpdated(getBlockPos(), getBlockState().getBlock());
            purgeShapeCacheKey();
        }
    }

    public boolean isSideConnected(Direction dir) {
        return DirectionUtil.getDirectionBit(sidesVisuallyConnected, dir);
    }

    public void setSideConnected(Direction dir, boolean connected) {
        sidesActuallyConnected = DirectionUtil.setDirectionBit(sidesActuallyConnected, dir, connected);
        sidesVisuallyConnected = calculateVisuallyConnected();
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
        if (module != null) {
            modules.put(side, module);
        } else {
            modules.remove(side);
        }
        if (getLevel() != null && !getLevel().isClientSide) {
            discoverConnectedNeighbors();
            sendDescriptionPacket();
            setChanged();
            purgeShapeCacheKey();
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

        // connected neighbour discovery needs to be deferred,
        //   since although the neighbouring block has updated,
        //   the block entity's air handler data may not yet have updated
//        needDiscover = true;
        discoverConnectedNeighbors();
    }

    private void discoverConnectedNeighbors() {
        int prevSidesConnected = sidesActuallyConnected;

        List<Direction> neighbourDirections = new ArrayList<>();
        airHandler.getConnectedAirHandlers(this).forEach(connection -> neighbourDirections.add(connection.getDirection()));
        sidesActuallyConnected = sidesVisuallyConnected = 0;
        neighbourDirections.forEach(d -> setSideConnected(d, true));
        sidesVisuallyConnected = calculateVisuallyConnected();

        if (sidesActuallyConnected != prevSidesConnected) {
            purgeShapeCacheKey();
        }
    }

    /**
     * If only connected in one direction (and no side closed or has a module),
     *   add a visual connection in the opposite direction, so the tube looks open
     */
    private int calculateVisuallyConnected() {
        if (sidesClosed != 0 || !modules.isEmpty()) {
            return sidesActuallyConnected;
        }

        return switch (sidesActuallyConnected) {
            case 1, 2 -> 1 | 2;      // D/U
            case 4, 8 -> 4 | 8;      // N/S
            case 16, 32 -> 16 | 32;  // E/W
            default -> sidesActuallyConnected;
        };
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
            if (tm != null) {
                tm.addInfo(text);
            }
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

    @Override
    protected ModelData.Builder modelDataBuilder() {
        return super.modelDataBuilder()
                .with(PressureTubeBlockEntity.CONNECTION_PROPERTY, (short) (sidesClosed | (sidesVisuallyConnected << 8)));
    }

    /**
     * Shape cache keys depends on connected/closed state of each face, along with whatever modules are attached.
     */
    public int getShapeCacheKey() {
        if (shapeCacheKey == null) {
            int[] vals = new int[8];
            vals[0] = sidesVisuallyConnected;
            vals[1] = sidesClosed;
            for (Direction d : DirectionUtil.VALUES) {
                AbstractTubeModule m = getModule(d);
                vals[d.get3DDataValue() + 2] = m == null ? 0 : m.getInternalId();
            }
            shapeCacheKey = Arrays.hashCode(vals);
        }
        return shapeCacheKey;
    }

    private void purgeShapeCacheKey() {
        shapeCacheKey = null;
    }
}
