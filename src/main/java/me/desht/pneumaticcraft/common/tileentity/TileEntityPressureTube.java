package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.api.tileentity.IAirListener;
import me.desht.pneumaticcraft.api.tileentity.IManoMeasurable;
import me.desht.pneumaticcraft.common.block.BlockPressureTube;
import me.desht.pneumaticcraft.common.block.tubes.IInfluenceDispersing;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.item.ItemTubeModule;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.RayTraceUtils;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

public class TileEntityPressureTube extends TileEntityPneumaticBase implements IAirListener, IManoMeasurable, ICamouflageableTE {
    @DescSynced
    private final boolean[] sidesClosed = new boolean[6];
    private final EnumMap<Direction,TubeModule> modules = new EnumMap<>(Direction.class);
    private BlockState camoState;
    private AxisAlignedBB renderBoundingBox = null;
    private Direction inLineModuleDir = null;  // only one inline module allowed
    private final List<Direction> neighbourDirections = new ArrayList<>();
    private VoxelShape cachedTubeShape = null; // important for performance
    private int pendingCacheShapeClear = 0;

    public TileEntityPressureTube() {
        this(ModTileEntities.PRESSURE_TUBE.get(), PneumaticValues.DANGER_PRESSURE_PRESSURE_TUBE, PneumaticValues.MAX_PRESSURE_PRESSURE_TUBE, PneumaticValues.VOLUME_PRESSURE_TUBE, 0);
    }

    TileEntityPressureTube(TileEntityType type, float dangerPressurePressureTube, float maxPressurePressureTube, int volumePressureTube, int upgradeSlots) {
        super(type, dangerPressurePressureTube, maxPressurePressureTube, volumePressureTube, upgradeSlots);
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);

        byte closed = tag.getByte("sidesClosed");
        for (int i = 0; i < 6; i++) {
            sidesClosed[i] = ((closed & 1 << i) != 0);
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        super.save(nbt);

        byte closed = 0;
        for (int i = 0; i < 6; i++) {
            if (sidesClosed[i]) closed |= 1 << i;
        }
        nbt.putByte("sidesClosed", closed);
        return nbt;
    }

    @Override
    public void writeToPacket(CompoundNBT tag) {
        super.writeToPacket(tag);

        writeModulesToNBT(tag);
        ICamouflageableTE.writeCamo(tag, camoState);
    }

    public void writeModulesToNBT(CompoundNBT tag) {
        ListNBT moduleList = new ListNBT();
        for (Direction d : DirectionUtil.VALUES) {
            TubeModule tm = getModule(d);
            if (tm != null) {
                CompoundNBT moduleTag = new CompoundNBT();
                moduleTag.putString("type", tm.getType().toString());
                tm.writeToNBT(moduleTag);
                moduleList.add(moduleTag);
            }
        }
        tag.put("modules", moduleList);
    }

    @Override
    public void readFromPacket(CompoundNBT tag) {
        super.readFromPacket(tag);

        clearCachedShape();

        EnumSet<Direction> dirs = EnumSet.allOf(Direction.class);
        ListNBT moduleList = tag.getList("modules", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < moduleList.size(); i++) {
            CompoundNBT moduleTag = moduleList.getCompound(i);
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(moduleTag.getString("type")));
            if (item instanceof ItemTubeModule) {
                TubeModule module = ((ItemTubeModule) item).createModule();
                module.readFromNBT(moduleTag);
                TubeModule oldModule = getModule(module.getDirection());
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
            TubeModule module = getModule(d);
            if (module != null) {
                setModule(d, null);
            }
        }

        updateRenderBoundingBox();
        if (hasLevel() && getLevel().isClientSide) {
            rerenderTileEntity();
        }
        camoState = ICamouflageableTE.readCamo(tag);
    }

    public void updateRenderBoundingBox() {
        renderBoundingBox = new AxisAlignedBB(getBlockPos());

        for (Direction dir : DirectionUtil.VALUES) {
            if (modules.containsKey(dir) && modules.get(dir).getRenderBoundingBox() != null) {
                renderBoundingBox = renderBoundingBox.minmax(modules.get(dir).getRenderBoundingBox());
            }
        }
    }

    @Override
    public void tick() {
        boolean hasModules = false;
        boolean hasClosedSide = false;

        if (pendingCacheShapeClear > 0 && --pendingCacheShapeClear == 0) {
            cachedTubeShape = null;
        }

        if (!getLevel().isClientSide) airHandler.setSideLeaking(null);

        for (Direction dir : DirectionUtil.VALUES) {
            TubeModule tm = getModule(dir);
            if (tm != null) {
                hasModules = true;
                tm.shouldDrop = true;
                tm.update();
            }
            if (isSideClosed(dir)) {
                hasClosedSide = true;
            }
        }

        // check for possibility of air leak due to unconnected tube
        if (!getLevel().isClientSide && !hasModules && !hasClosedSide && neighbourDirections.size() == 1) {
            Direction d = neighbourDirections.get(0).getOpposite();
            airHandler.setSideLeaking(canConnectPneumatic(d) ? d : null);
        }

        super.tick();
    }

    @Override
    protected void onFirstServerTick() {
        super.onFirstServerTick();

        neighbourDirections.clear();
        airHandler.getConnectedAirHandlers(this).forEach(connection -> neighbourDirections.add(connection.getDirection()));
    }

    @Override
    public void onAirDispersion(IAirHandlerMachine handler, @Nullable Direction side, int airDispersed) {
        if (side != null) {
            TubeModule tm = getModule(side);
            if (tm instanceof IInfluenceDispersing) {
                ((IInfluenceDispersing) tm).onAirDispersion(airDispersed);
            }
        }
    }

    @Override
    public int getMaxDispersion(IAirHandlerMachine handler, @Nullable Direction side) {
        if (side != null) {
            TubeModule tm = getModule(side);
            if (tm instanceof IInfluenceDispersing) {
                return ((IInfluenceDispersing) tm).getMaxDispersion();
            }
        }
        return Integer.MAX_VALUE;
    }

    public TubeModule getModule(Direction side) {
        return modules.get(side);
    }

    public boolean isSideClosed(Direction side) {
        return sidesClosed[side.get3DDataValue()];
    }

    public void setSideClosed(Direction side, boolean closed) {
        sidesClosed[side.get3DDataValue()] = closed;
    }

    public Stream<TubeModule> tubeModules() {
        return modules.values().stream().filter(Objects::nonNull);
    }

    public boolean mayPlaceModule(TubeModule module, Direction side) {
        return inLineModuleDir == null
                && getModule(side) == null
                && !isSideClosed(side)
                && (!module.isInline() || getConnectedNeighbor(side) == null);
    }

    public void setModule(Direction side, TubeModule module) {
        if (module != null) {
            module.setDirection(side);
            module.setTube(this);
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
            getLevel().setBlock(getBlockPos(), BlockPressureTube.recalculateState(level, worldPosition, getBlockState()), Constants.BlockFlags.DEFAULT);
            sendDescriptionPacket();
            setChanged();
        }
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return (inLineModuleDir == null || inLineModuleDir.getAxis() == side.getAxis())
                && !isSideClosed(side)
                && (getModule(side) == null || getModule(side).isInline());
    }

    @Override
    public void onNeighborTileUpdate(BlockPos tilePos) {
        super.onNeighborTileUpdate(tilePos);

        tubeModules().filter(module -> getBlockPos().relative(module.getDirection()).equals(tilePos))
                .forEach(TubeModule::onNeighborTileUpdate);
    }

    @Override
    public void onNeighborBlockUpdate(BlockPos fromPos) {
        super.onNeighborBlockUpdate(fromPos);

        tubeModules().forEach(TubeModule::onNeighborBlockUpdate);

        neighbourDirections.clear();
        airHandler.getConnectedAirHandlers(this).forEach(connection -> neighbourDirections.add(connection.getDirection()));
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return null;
    }

    public TileEntity getConnectedNeighbor(Direction dir) {
        TubeModule tm = getModule(dir);
        if (!isSideClosed(dir) && (tm == null || tm.isInline() && dir.getAxis() == tm.getDirection().getAxis())) {
            TileEntity te = getCachedNeighbor(dir);
            if (te != null && te.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, dir.getOpposite()).isPresent()) {
                return te;
            }
        }
        return null;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return renderBoundingBox != null ? renderBoundingBox : new AxisAlignedBB(getBlockPos());
    }

    @Override
    public void printManometerMessage(PlayerEntity player, List<ITextComponent> text) {
        RayTraceResult mop = RayTraceUtils.getEntityLookedObject(player, PneumaticCraftUtils.getPlayerReachDistance(player));
        if (mop.hitInfo instanceof Direction) {
            TubeModule tm = getModule((Direction) mop.hitInfo);
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
        ICamouflageableTE.syncToClient(this);
    }

    public VoxelShape getCachedTubeShape(VoxelShape blockShape) {
        if (cachedTubeShape == null) {
            cachedTubeShape = blockShape;
            tubeModules().forEach(module -> cachedTubeShape = VoxelShapes.or(cachedTubeShape, module.getShape()));
        }
        return cachedTubeShape;
    }

    public void clearCachedShape() {
        // needs to be deferred by a couple of ticks, it seems
        // otherwise the old shape (now wrong) can get recached on the client
        pendingCacheShapeClear = 2;
    }
}
