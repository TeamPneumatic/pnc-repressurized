package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IAirListener;
import me.desht.pneumaticcraft.api.tileentity.IManoMeasurable;
import me.desht.pneumaticcraft.common.block.tubes.IInfluenceDispersing;
import me.desht.pneumaticcraft.common.block.tubes.ModuleRegistrator;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.thirdparty.ModInteractionUtils;
import me.desht.pneumaticcraft.common.thirdparty.mcmultipart.IMultipartTE;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;

public class TileEntityPressureTube extends TileEntityPneumaticBase implements IAirListener, IManoMeasurable, IMultipartTE {
    @DescSynced
    public boolean[] sidesConnected = new boolean[6];
    public TubeModule[] modules = new TubeModule[6];

    private Object part;

    public TileEntityPressureTube() {
        super(PneumaticValues.DANGER_PRESSURE_PRESSURE_TUBE, PneumaticValues.MAX_PRESSURE_PRESSURE_TUBE, PneumaticValues.VOLUME_PRESSURE_TUBE, 0);
    }

    public TileEntityPressureTube(float dangerPressurePressureTube, float maxPressurePressureTube,
                                  int volumePressureTube) {
        super(dangerPressurePressureTube, maxPressurePressureTube, volumePressureTube, 0);
    }

    public TileEntityPressureTube setPart(Object part) {
        this.part = part;
        for (TubeModule module : modules) {
            if (module != null) module.shouldDrop = false;
        }
        return this;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        for (int i = 0; i < 6; i++) {
            sidesConnected[i] = nbt.getBoolean("sideConnected" + i);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        for (int i = 0; i < 6; i++) {
            nbt.setBoolean("sideConnected" + i, sidesConnected[i]);
        }
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
            module.readFromNBT(moduleTag);
            setModule(module, EnumFacing.getFront(moduleTag.getInteger("side")));
        }
        if (hasWorld() && getWorld().isRemote) {
            rerenderChunk();
        }
    }

    @Override
    public void update() {
        super.update();

        for (TubeModule module : modules) {
            if (module != null) {
                module.shouldDrop = true;
                module.update();
            }
        }

        List<Pair<EnumFacing, IAirHandler>> teList = getAirHandler(null).getConnectedPneumatics();

        boolean hasModules = false;
        for (TubeModule module : modules) {
            if (module != null) {
                hasModules = true;
                break;
            }
        }
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
            int intSide = side/*.getOpposite()*/.ordinal();
            if (modules[intSide] instanceof IInfluenceDispersing) {
                ((IInfluenceDispersing) modules[intSide]).onAirDispersion(amount);
            }
        }
    }

    @Override
    public int getMaxDispersion(IAirHandler handler, EnumFacing side) {
        if (side != null) {
            int intSide = side/*.getOpposite()*/.ordinal();
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
        if (!getWorld().isRemote) {
//            if (part != null) updatePart();
            sendDescriptionPacket();
        }
    }

    @Override
    public boolean isConnectedTo(EnumFacing side) {
        return (modules[side.ordinal()] == null || modules[side.ordinal()].isInline()) && (part == null || ModInteractionUtils.getInstance().isMultipartWiseConnected(part, side));
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

    public void updateConnections() {
        sidesConnected = new boolean[6];

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
                    EnumFacing opposite = EnumFacing.getFront(i).getOpposite();
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
        return new AxisAlignedBB(getPos().getX(), getPos().getY(), getPos().getZ(), getPos().getX() + 1, getPos().getY() + 1, getPos().getZ() + 1);
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
    public String getMultipartId() {
        return "pressure_tube";
    }

//    @Override
//    @Optional.Method(modid = ModIds.MCMP)
//    public void sendDescriptionPacket() {
//        if (part != null && !getWorld().isRemote) {
//            ((PartPressureTube) part).sendUpdatePacket();
//        }
//        super.sendDescriptionPacket();
//    }
//
//    @Optional.Method(modid = ModIds.FMP)
//    public void updatePart() {
//        ((PartPressureTube) part).onNeighborTileChange(null);
//    }
}
