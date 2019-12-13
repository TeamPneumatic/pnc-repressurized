package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.common.block.tubes.IPneumaticPosProvider;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.thirdparty.computercraft.LuaConstant;
import me.desht.pneumaticcraft.common.thirdparty.computercraft.LuaMethod;
import me.desht.pneumaticcraft.common.thirdparty.computercraft.LuaMethodRegistry;
import me.desht.pneumaticcraft.lib.NBTKeys;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class TileEntityPneumaticBase extends TileEntityTickableBase implements IPneumaticPosProvider {
    @GuiSynced
    final IAirHandler airHandler;
    public final float dangerPressure, criticalPressure;
    private final int defaultVolume;
    private final LazyOptional<IAirHandler> airHandlerCap;

    public TileEntityPneumaticBase(TileEntityType type, float dangerPressure, float criticalPressure, int volume, int upgradeSlots) {
        super(type, upgradeSlots);

        this.airHandler = PneumaticRegistry.getInstance().getAirHandlerSupplier().createAirHandler(dangerPressure, criticalPressure, volume);
        this.airHandlerCap = LazyOptional.of(() -> airHandler);
        this.dangerPressure = dangerPressure;
        this.criticalPressure = criticalPressure;
        this.defaultVolume = volume;

        addApplicableUpgrade(IItemRegistry.EnumUpgrade.VOLUME);
        addApplicableUpgrade(IItemRegistry.EnumUpgrade.SECURITY);

    }

    @Override
    public void tick() {
        super.tick();
        airHandler.tick();
    }

    @Override
    public void validate() {
        super.validate();
        airHandler.validate(this);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == PneumaticRegistry.AIR_HANDLER_CAPABILITY) {
            return side == null || canConnectTo(side) ? airHandlerCap.cast() : LazyOptional.empty();
        } else {
            return super.getCapability(cap, side);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        tag.put(NBTKeys.NBT_AIR_HANDLER, airHandler.serializeNBT());
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        airHandler.deserializeNBT(tag.getCompound(NBTKeys.NBT_AIR_HANDLER));
        if (tag.contains(NBTKeys.NBT_AIR_AMOUNT)) {
            // when restoring from item NBT
            airHandler.addAir(tag.getInt(NBTKeys.NBT_AIR_AMOUNT));
        }
    }

    @Override
    public void onNeighborTileUpdate() {
        super.onNeighborTileUpdate();
        airHandler.onNeighborChange();
    }

    @Override
    public void onNeighborBlockUpdate() {
        super.onNeighborBlockUpdate();
        airHandler.onNeighborChange();
    }

    @Override
    public void writeToPacket(CompoundNBT tag) {
        super.writeToPacket(tag);
        tag.put(NBTKeys.NBT_AIR_HANDLER, airHandler.serializeNBT());
    }

    @Override
    public void readFromPacket(CompoundNBT tag) {
        super.readFromPacket(tag);
        airHandler.deserializeNBT(tag.getCompound(NBTKeys.NBT_AIR_HANDLER));
    }

    @Override
    protected void addLuaMethods(LuaMethodRegistry registry) {
        super.addLuaMethods(registry);
        registry.registerLuaMethod(new LuaMethod("getPressure") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 0, 1, "face (down/up/north/south/west/east)");
                if (args.length == 0) {
                    return new Object[]{airHandler.getPressure()};
                } else {
                    IAirHandler handler = getAirHandler(getDirForString((String) args[0]));
                    return new Object[]{handler != null ? handler.getPressure() : 0};
                }
            }
        });

        if (this instanceof IMinWorkingPressure) {
            final IMinWorkingPressure mwp = (IMinWorkingPressure) this;
            registry.registerLuaMethod(new LuaMethod("getMinWorkingPressure") {
                @Override
                public Object[] call(Object[] args) {
                    requireNoArgs(args);
                    return new Object[] { mwp.getMinWorkingPressure() };
                }
            });
        }

        registry.registerLuaMethod(new LuaConstant("getDangerPressure", dangerPressure));
        registry.registerLuaMethod(new LuaConstant("getCriticalPressure", criticalPressure));
        registry.registerLuaMethod(new LuaConstant("getDefaultVolume", defaultVolume));
    }

    /*
     * End ComputerCraft API 
     */

    @Override
    public World world() {
        return getWorld();
    }

    @Override
    public BlockPos pos() {
        return getPos();
    }

    @Override
    public IAirHandler getAirHandler(Direction side) {
        return side == null || canConnectTo(side) ? airHandler : null;
    }

    public float getPressure() {
        return getAirHandler(null).getPressure();
    }

    public void addAir(int air) {
        getAirHandler(null).addAir(air);
    }

    /**
     * Checks if the given side of this TE can be pneumatically connected to.
     *
     * @param side the side to check
     * @return true if connected, false otherwise
     */
    public boolean canConnectTo(Direction side) {
        return true;
    }

    public int getDefaultVolume() {
        return defaultVolume;
    }
}
