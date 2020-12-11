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
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
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
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class TileEntityPressureTube extends TileEntityPneumaticBase implements IAirListener, IManoMeasurable, ICamouflageableTE {
    @DescSynced
    private final boolean[] sidesClosed = new boolean[6];
    private final TubeModule[] modules = new TubeModule[6];
    private BlockState camoState;
    private AxisAlignedBB renderBoundingBox = null;
    private Direction inLineModuleDir = null;  // only one inline module allowed
    private final List<Direction> connectedNeighbours = new ArrayList<>();
    private VoxelShape cachedModuleShape = null;
    
    public TileEntityPressureTube() {
        this(ModTileEntities.PRESSURE_TUBE.get(), PneumaticValues.DANGER_PRESSURE_PRESSURE_TUBE, PneumaticValues.MAX_PRESSURE_PRESSURE_TUBE, PneumaticValues.VOLUME_PRESSURE_TUBE, 0);
    }

    TileEntityPressureTube(TileEntityType type, float dangerPressurePressureTube, float maxPressurePressureTube, int volumePressureTube, int upgradeSlots) {
        super(type, dangerPressurePressureTube, maxPressurePressureTube, volumePressureTube, upgradeSlots);
    }

    @Override
    public void read(BlockState state, CompoundNBT tag) {
        super.read(state, tag);

        byte closed = tag.getByte("sidesClosed");
        for (int i = 0; i < 6; i++) {
            sidesClosed[i] = ((closed & 1 << i) != 0);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        super.write(nbt);

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
        for (Direction d : Direction.VALUES) {
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

        cachedModuleShape = null;
        Arrays.fill(modules, null);
        ListNBT moduleList = tag.getList("modules", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < moduleList.size(); i++) {
            CompoundNBT moduleTag = moduleList.getCompound(i);
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(moduleTag.getString("type")));
            if (item instanceof ItemTubeModule) {
                TubeModule module = ((ItemTubeModule) item).createModule();
                module.readFromNBT(moduleTag);
                setModule(module.getDirection(), module);
            } else {
                Log.error("unknown tube module type: " + moduleTag.getString("type"));
            }
        }
        updateRenderBoundingBox();
        if (hasWorld() && getWorld().isRemote) {
            rerenderTileEntity();
        }
        camoState = ICamouflageableTE.readCamo(tag);
    }

    public void updateRenderBoundingBox() {
        renderBoundingBox = new AxisAlignedBB(getPos().getX(), getPos().getY(), getPos().getZ(), getPos().getX() + 1, getPos().getY() + 1, getPos().getZ() + 1);

        for (int i = 0; i < 6; i++) {
            if (modules[i] != null && modules[i].getRenderBoundingBox() != null) {
                renderBoundingBox = renderBoundingBox.union(modules[i].getRenderBoundingBox());
            }
        }
    }

    @Override
    public void tick() {
        boolean hasModules = false;
        boolean hasClosedSide = false;

        if (!getWorld().isRemote) airHandler.setSideLeaking(null);

        for (Direction dir : Direction.VALUES) {
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
        if (!getWorld().isRemote && !hasModules && !hasClosedSide && connectedNeighbours.size() == 1) {
            Direction d = connectedNeighbours.get(0).getOpposite();
            airHandler.setSideLeaking(canConnectPneumatic(d) ? d : null);
        }

        super.tick();

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
        return modules[side.getIndex()];
    }

    public boolean isSideClosed(Direction side) {
        return sidesClosed[side.getIndex()];
    }

    public void setSideClosed(Direction side, boolean closed) {
        sidesClosed[side.getIndex()] = closed;
    }

    public Stream<TubeModule> tubeModules() {
        return Arrays.stream(modules).filter(Objects::nonNull);
    }

    public boolean mayPlaceModule(Direction side) {
        return inLineModuleDir == null && getModule(side) == null && !isSideClosed(side);
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
        }
        cachedModuleShape = null;
        modules[side.getIndex()] = module;
        if (getWorld() != null && !getWorld().isRemote) {
            world.setBlockState(getPos(), BlockPressureTube.recalculateState(world, pos, getBlockState()), Constants.BlockFlags.DEFAULT);
            sendDescriptionPacket();
            markDirty();
        }
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return (inLineModuleDir == null || inLineModuleDir.getAxis() == side.getAxis())
                && !isSideClosed(side)
                && (getModule(side) == null || getModule(side).isInline());
    }

    @Override
    public void onNeighborTileUpdate() {
        super.onNeighborTileUpdate();
        for (TubeModule module : modules) {
            if (module != null) module.onNeighborTileUpdate();
        }
    }

    @Override
    public void onNeighborBlockUpdate() {
        super.onNeighborBlockUpdate();
        for (TubeModule module : modules) {
            if (module != null) module.onNeighborBlockUpdate();
        }

        List<IAirHandlerMachine.Connection> l = airHandler.getConnectedAirHandlers(this);
        connectedNeighbours.clear();
        l.forEach(connection -> connectedNeighbours.add(connection.getDirection()));
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
        return renderBoundingBox != null ? renderBoundingBox : new AxisAlignedBB(getPos());
    }



    @Override
    public void printManometerMessage(PlayerEntity player, List<ITextComponent> text) {
        RayTraceResult mop = PneumaticCraftUtils.getEntityLookedObject(player);
        if (mop != null && mop.hitInfo instanceof Direction) {
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

    public VoxelShape getCachedModuleShape() {
        if (cachedModuleShape == null) {
            cachedModuleShape = VoxelShapes.empty();
            for (TubeModule module : modules) {
                if (module != null) cachedModuleShape = VoxelShapes.or(cachedModuleShape, module.getShape());
            }
        }
        return cachedModuleShape;
    }
    
    public static TileEntityPressureTube getTube(TileEntity te) {
        return te instanceof TileEntityPressureTube ? (TileEntityPressureTube) te : null;
    }
}
