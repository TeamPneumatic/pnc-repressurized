package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IAirListener;
import me.desht.pneumaticcraft.api.tileentity.IManoMeasurable;
import me.desht.pneumaticcraft.common.block.tubes.IInfluenceDispersing;
import me.desht.pneumaticcraft.common.block.tubes.ModuleRegistrator;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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
    private IBlockState camoState;
    private AxisAlignedBB renderBoundingBox = null;

    public TileEntityPressureTube() {
        super(PneumaticValues.DANGER_PRESSURE_PRESSURE_TUBE, PneumaticValues.MAX_PRESSURE_PRESSURE_TUBE, PneumaticValues.VOLUME_PRESSURE_TUBE, 0);
    }

    public TileEntityPressureTube(float dangerPressurePressureTube, float maxPressurePressureTube, int volumePressureTube) {
        super(dangerPressurePressureTube, maxPressurePressureTube, volumePressureTube, 0);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        if (nbt.hasKey("sidesConnected")) {
            // new-style: far more compact storage
            byte connected = nbt.getByte("sidesConnected");
            byte closed = nbt.getByte("sidesClosed");
            for (int i = 0; i < 6; i++) {
                sidesConnected[i] = ((connected & 1 << i) != 0);
                sidesClosed[i] = ((closed & 1 << i) != 0);
            }
        } else {
            // old-style
            for (int i = 0; i < 6; i++) {
                sidesConnected[i] = nbt.getBoolean("sideConnected" + i);
                sidesClosed[i] = nbt.getBoolean("sideClosed" + i);
            }
        }
        camoStack = ICamouflageableTE.readCamoStackFromNBT(nbt);
        camoState = ICamouflageableTE.getStateForStack(camoStack);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        byte connected = 0, closed = 0;
        for (int i = 0; i < 6; i++) {
            if (sidesConnected[i]) connected |= 1 << i;
            if (sidesClosed[i]) closed |= 1 << i;
        }
        nbt.setByte("sidesConnected", connected);
        nbt.setByte("sidesClosed", closed);
        ICamouflageableTE.writeCamoStackToNBT(camoStack, nbt);
        return nbt;
    }

    @Override
    public void writeToPacket(NBTTagCompound tag) {
        super.writeToPacket(tag);
        writeModulesToNBT(tag);
    }

    public void writeModulesToNBT(NBTTagCompound tag) {
        NBTTagList moduleList = new NBTTagList();
        for (int i = 0; i < modules.length; i++) {
            if (modules[i] != null) {
                NBTTagCompound moduleTag = new NBTTagCompound();
                moduleTag.setString("type", modules[i].getType());
                modules[i].writeToNBT(moduleTag);
                moduleTag.setInteger("side", i);
                moduleList.appendTag(moduleTag);
            }
        }
        tag.setTag("modules", moduleList);
    }

    @Override
    public void readFromPacket(NBTTagCompound tag) {
        super.readFromPacket(tag);
        modules = new TubeModule[6];
        NBTTagList moduleList = tag.getTagList("modules", 10);
        for (int i = 0; i < moduleList.tagCount(); i++) {
            NBTTagCompound moduleTag = moduleList.getCompoundTagAt(i);
            TubeModule module = ModuleRegistrator.getModule(moduleTag.getString("type"));
            if (module != null) {
                module.readFromNBT(moduleTag);
                setModule(module, EnumFacing.byIndex(moduleTag.getInteger("side")));
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
    public void update() {
        super.update();

        boolean hasModules = false;
        for (TubeModule module : modules) {
            if (module != null) {
                hasModules = true;
                module.shouldDrop = true;
                module.update();
            }
        }

        List<Pair<EnumFacing, IAirHandler>> teList = getAirHandler(null).getConnectedPneumatics();

        if (!hasModules && teList.size() == 1 && !getWorld().isRemote) {
            for (Pair<EnumFacing, IAirHandler> entry : teList) {
                if (entry.getKey() != null && modules[entry.getKey().getOpposite().ordinal()] == null && isConnectedTo(entry.getKey().getOpposite()))
                    getAirHandler(null).airLeak(entry.getKey().getOpposite());
            }
        }
    }

    @Override
    public void onAirDispersion(IAirHandler handler, EnumFacing side, int amount) {
        if (side != null) {
            int intSide = side.ordinal();
            if (modules[intSide] instanceof IInfluenceDispersing) {
                ((IInfluenceDispersing) modules[intSide]).onAirDispersion(amount);
            }
        }
    }

    @Override
    public int getMaxDispersion(IAirHandler handler, EnumFacing side) {
        if (side != null) {
            int intSide = side.ordinal();
            if (modules[intSide] instanceof IInfluenceDispersing) {
                return ((IInfluenceDispersing) modules[intSide]).getMaxDispersion();
            }
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public void addConnectedPneumatics(List<Pair<EnumFacing, IAirHandler>> pneumatics) {
    }

    public void setModule(TubeModule module, EnumFacing side) {
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
    public boolean isConnectedTo(EnumFacing side) {
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

    private void updateConnections() {
        List<Pair<EnumFacing, IAirHandler>> connections = getAirHandler(null).getConnectedPneumatics();
        Arrays.fill(sidesConnected, false);
        for (Pair<EnumFacing, IAirHandler> entry : connections) {
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
                    EnumFacing opposite = EnumFacing.byIndex(i).getOpposite();
                    if (isConnectedTo(opposite)) sidesConnected[opposite.ordinal()] = true;
                    break;
                }
            }
        }
        for (int i = 0; i < 6; i++) {
            if (modules[i] != null && modules[i].isInline()) sidesConnected[i] = false;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return renderBoundingBox != null ? renderBoundingBox : new AxisAlignedBB(getPos());
    }

    @Override
    public void printManometerMessage(EntityPlayer player, List<String> text) {
        RayTraceResult mop = PneumaticCraftUtils.getEntityLookedObject(player);
        if (mop != null && mop.hitInfo instanceof EnumFacing) {
            EnumFacing dir = (EnumFacing) mop.hitInfo;
            if (modules[dir.ordinal()] != null) {
                modules[dir.ordinal()].addInfo(text);
            }
        }
    }

    @Override
    public IBlockState getCamouflage() {
        return camoState;
    }

    @Override
    public void setCamouflage(IBlockState state) {
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
