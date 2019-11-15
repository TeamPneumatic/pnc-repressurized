package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IAirListener;
import me.desht.pneumaticcraft.api.tileentity.IManoMeasurable;
import me.desht.pneumaticcraft.common.block.BlockPressureTube;
import me.desht.pneumaticcraft.common.block.BlockPressureTube.ConnectionType;
import me.desht.pneumaticcraft.common.block.tubes.IInfluenceDispersing;
import me.desht.pneumaticcraft.common.block.tubes.ModuleRegistrator;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.core.ModTileEntityTypes;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;

public class TileEntityPressureTube extends TileEntityPneumaticBase implements IAirListener, IManoMeasurable, ICamouflageableTE {
    @DescSynced
    public final boolean[] sidesConnected = new boolean[6];
    @DescSynced
    public final boolean[] sidesClosed = new boolean[6];
    public TubeModule[] modules = new TubeModule[6];
    @DescSynced
    private ItemStack camoStack = ItemStack.EMPTY;
    private BlockState camoState;
    private AxisAlignedBB renderBoundingBox = null;

    public TileEntityPressureTube() {
        this(ModTileEntityTypes.PRESSURE_TUBE, PneumaticValues.DANGER_PRESSURE_PRESSURE_TUBE, PneumaticValues.MAX_PRESSURE_PRESSURE_TUBE, PneumaticValues.VOLUME_PRESSURE_TUBE, 0);
    }

    TileEntityPressureTube(TileEntityType type, float dangerPressurePressureTube, float maxPressurePressureTube, int volumePressureTube, int upgradeSlots) {
        super(type, dangerPressurePressureTube, maxPressurePressureTube, volumePressureTube, upgradeSlots);
    }

    @Override
    public void read(CompoundNBT nbt) {
        super.read(nbt);

        byte connected = nbt.getByte("sidesConnected");
        byte closed = nbt.getByte("sidesClosed");
        for (int i = 0; i < 6; i++) {
            sidesConnected[i] = ((connected & 1 << i) != 0);
            sidesClosed[i] = ((closed & 1 << i) != 0);
        }
        camoStack = ICamouflageableTE.readCamoStackFromNBT(nbt);
        camoState = ICamouflageableTE.getStateForStack(camoStack);
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        super.write(nbt);

        byte connected = 0, closed = 0;
        for (int i = 0; i < 6; i++) {
            if (sidesConnected[i]) connected |= 1 << i;
            if (sidesClosed[i]) closed |= 1 << i;
        }
        nbt.putByte("sidesConnected", connected);
        nbt.putByte("sidesClosed", closed);
        ICamouflageableTE.writeCamoStackToNBT(camoStack, nbt);
        return nbt;
    }

    @Override
    public void writeToPacket(CompoundNBT tag) {
        super.writeToPacket(tag);
        writeModulesToNBT(tag);
    }

    public void writeModulesToNBT(CompoundNBT tag) {
        ListNBT moduleList = new ListNBT();
        for (int i = 0; i < modules.length; i++) {
            if (modules[i] != null) {
                CompoundNBT moduleTag = new CompoundNBT();
                moduleTag.putString("type", modules[i].getType());
                modules[i].writeToNBT(moduleTag);
                moduleTag.putInt("side", i);
                moduleList.add(moduleTag);
            }
        }
        tag.put("modules", moduleList);
    }

    @Override
    public void readFromPacket(CompoundNBT tag) {
        super.readFromPacket(tag);
        modules = new TubeModule[6];
        ListNBT moduleList = tag.getList("modules", 10);
        for (int i = 0; i < moduleList.size(); i++) {
            CompoundNBT moduleTag = moduleList.getCompound(i);
            TubeModule module = ModuleRegistrator.getModule(moduleTag.getString("type"));
            if (module != null) {
                module.readFromNBT(moduleTag);
                setModule(module, Direction.byIndex(moduleTag.getInt("side")));
            }
        }
        updateRenderBoundingBox();
        if (hasWorld() && getWorld().isRemote) {
            rerenderTileEntity();
        }
    }

    private void updateRenderBoundingBox() {
        renderBoundingBox = new AxisAlignedBB(getPos().getX(), getPos().getY(), getPos().getZ(), getPos().getX() + 1, getPos().getY() + 1, getPos().getZ() + 1);

        for (int i = 0; i < 6; i++) {
            if (modules[i] != null && modules[i].getRenderBoundingBox() != null) {
                renderBoundingBox = renderBoundingBox.union(modules[i].getRenderBoundingBox());
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        boolean hasModules = false;
        for (TubeModule module : modules) {
            if (module != null) {
                hasModules = true;
                module.shouldDrop = true;
                module.update();
            }
        }

        List<Pair<Direction, IAirHandler>> teList = getAirHandler(null).getConnectedPneumatics();

        if (!hasModules && teList.size() == 1 && !getWorld().isRemote) {
            for (Pair<Direction, IAirHandler> entry : teList) {
                if (entry.getKey() != null && modules[entry.getKey().getOpposite().ordinal()] == null && canConnectTo(entry.getKey().getOpposite()))
                    getAirHandler(null).airLeak(entry.getKey().getOpposite());
            }
        }
    }

    @Override
    public void onAirDispersion(IAirHandler handler, Direction side, int amount) {
        if (side != null) {
            int intSide = side.ordinal();
            if (modules[intSide] instanceof IInfluenceDispersing) {
                ((IInfluenceDispersing) modules[intSide]).onAirDispersion(amount);
            }
        }
    }

    @Override
    public int getMaxDispersion(IAirHandler handler, Direction side) {
        if (side != null) {
            int intSide = side.ordinal();
            if (modules[intSide] instanceof IInfluenceDispersing) {
                return ((IInfluenceDispersing) modules[intSide]).getMaxDispersion();
            }
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public void addConnectedPneumatics(List<Pair<Direction, IAirHandler>> pneumatics) {
    }

    public void setModule(TubeModule module, Direction side) {
        if (module != null) {
            module.setDirection(side);
            module.setTube(this);
        }
        modules[side.ordinal()] = module;
        if (getWorld() != null && !getWorld().isRemote) {
            sendDescriptionPacket();
        }
        markDirty();
    }

    @Override
    public boolean canConnectTo(Direction side) {
        return !sidesClosed[side.ordinal()]
                && (modules[side.ordinal()] == null || modules[side.ordinal()].isInline());
    }

    @Override
    public void onNeighborTileUpdate() {
        super.onNeighborTileUpdate();
        updateConnections();
        for (TubeModule module : modules) {
            if (module != null) module.onNeighborTileUpdate();
        }
    }

    @Override
    public void onNeighborBlockUpdate() {
        super.onNeighborBlockUpdate();
        updateConnections();
        for (TubeModule module : modules) {
            if (module != null) module.onNeighborBlockUpdate();
        }
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return null;
    }

    private void updateConnections() {
        List<Pair<Direction, IAirHandler>> connections = getAirHandler(null).getConnectedPneumatics();
        Arrays.fill(sidesConnected, false);
        for (Pair<Direction, IAirHandler> entry : connections) {
            sidesConnected[entry.getKey().ordinal()] = true;
        }

        boolean hasModule = false;
        for (int i = 0; i < 6; i++) {
            if (modules[i] != null) {
                hasModule = true;
                break;
            }
        }

        int sidesCount = 0;
        for (boolean bool : sidesConnected) {
            if (bool) sidesCount++;
        }
        if (sidesCount == 1 && !hasModule) {
            for (int i = 0; i < 6; i++) {
                if (sidesConnected[i]) {
                    Direction opposite = Direction.byIndex(i).getOpposite();
                    if (canConnectTo(opposite)) sidesConnected[opposite.ordinal()] = true;
                    break;
                }
            }
        }
        for (int i = 0; i < 6; i++) {
            if (modules[i] != null && modules[i].isInline()) sidesConnected[i] = false;
        }

        // update the blockstate
        BlockState state = getBlockState();
        for (int i = 0; i < 6; i++)  {
            ConnectionType val = sidesClosed[i] ? ConnectionType.CLOSED : (sidesConnected[i] ? ConnectionType.CONNECTED : ConnectionType.UNCONNECTED);
            state = state.with(BlockPressureTube.CONNECTION_PROPERTIES_3[i], val);
        }
        world.setBlockState(pos, state);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return renderBoundingBox != null ? renderBoundingBox : new AxisAlignedBB(getPos());
    }

    @Override
    public void printManometerMessage(PlayerEntity player, List<ITextComponent> text) {
        RayTraceResult mop = PneumaticCraftUtils.getEntityLookedObject(player);
        if (mop != null && mop.hitInfo instanceof Direction) {
            Direction dir = (Direction) mop.hitInfo;
            if (modules[dir.ordinal()] != null) {
                modules[dir.ordinal()].addInfo(text);
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
        camoStack = ICamouflageableTE.getStackForState(state);
        sendDescriptionPacket();
        markDirty();
    }

    @Override
    public void onDescUpdate() {
        camoState = ICamouflageableTE.getStateForStack(camoStack);

        super.onDescUpdate();
    }

    public static TileEntityPressureTube getTube(TileEntity te) {
        return te instanceof TileEntityPressureTube ? (TileEntityPressureTube) te : null;
    }
}
