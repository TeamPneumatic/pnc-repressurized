package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.lib.NBTKeys;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.LuaConstant;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.LuaMethod;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.LuaMethodRegistry;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Base class for all PNC tile entities which handle air. Provides one default air handler; machines with multiple
 * air handlers can add extra handlers in their subclass.
 */
public abstract class TileEntityPneumaticBase extends TileEntityTickableBase {
    @GuiSynced
    protected final IAirHandlerMachine airHandler;
    private final LazyOptional<IAirHandlerMachine> airHandlerCap;
    private final Map<IAirHandlerMachine, List<Direction>> airHandlerMap = new IdentityHashMap<>();

    public TileEntityPneumaticBase(TileEntityType type, float dangerPressure, float criticalPressure, int volume, int upgradeSlots) {
        super(type, upgradeSlots);

        this.airHandler = PneumaticRegistry.getInstance().getAirHandlerMachineFactory()
                .createAirHandler(dangerPressure, criticalPressure, volume);
        this.airHandlerCap = LazyOptional.of(() -> airHandler);
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        super.handleUpdateTag(state, tag);

        initializeHullAirHandlers();
    }

    @Override
    protected void onFirstServerTick() {
        super.onFirstServerTick();

        initializeHullAirHandlers();
    }

    @Override
    public void tick() {
        super.tick();

        // note: needs to tick client-side too (for handling leak particles & sounds)
        airHandlerMap.keySet().forEach(handler -> handler.tick(this));
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        airHandlerMap.forEach((handler, sides) -> {
            if (!sides.isEmpty()) getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, sides.get(0)).invalidate();
        });
    }

    @Override
    public void onUpgradesChanged() {
        super.onUpgradesChanged();

        airHandler.setVolumeUpgrades(getUpgrades(EnumUpgrade.VOLUME));
        airHandler.setHasSecurityUpgrade(getUpgrades(EnumUpgrade.SECURITY) > 0);

        airHandlerMap.keySet().forEach(h -> {
            h.setVolumeUpgrades(getUpgrades(EnumUpgrade.VOLUME));
            h.setHasSecurityUpgrade(getUpgrades(EnumUpgrade.SECURITY) > 0);
        });
    }

    @Override
    public void onBlockRotated() {
        super.onBlockRotated();

        // force a resync of where any leak might be coming from
        initializeHullAirHandlers();
        airHandlerMap.keySet().forEach(h -> h.setSideLeaking(null));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY ) {
            return level != null && (side == null || canConnectPneumatic(side)) ? airHandlerCap.cast() : LazyOptional.empty();
        } else {
            return super.getCapability(cap, side);
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        super.save(tag);
        tag.put(NBTKeys.NBT_AIR_HANDLER, airHandler.serializeNBT());
        return tag;
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);

        airHandler.deserializeNBT(tag.getCompound(NBTKeys.NBT_AIR_HANDLER));
        airHandler.setVolumeUpgrades(getUpgrades(EnumUpgrade.VOLUME));
        if (tag.contains(NBTKeys.NBT_AIR_AMOUNT)) {
            // when restoring from item NBT
            airHandler.addAir(tag.getInt(NBTKeys.NBT_AIR_AMOUNT));
        }
    }

    public void initializeHullAirHandlers() {
        airHandlerMap.clear();
        for (Direction side : DirectionUtil.VALUES) {
            getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, side)
                    .ifPresent(handler -> airHandlerMap.computeIfAbsent(handler, k -> new ArrayList<>()).add(side));
        }
        airHandlerMap.forEach(IAirHandlerMachine::setConnectedFaces);
    }

    // called clientside when a PacketUpdatePressureBlock is received
    // this ensures the TE can tick this air handler for air leak sound and particle purposes
    public void initializeHullAirHandlerClient(Direction dir, IAirHandlerMachine handler) {
        airHandlerMap.clear();
        List<Direction> l = Collections.singletonList(dir);
        airHandlerMap.put(handler, l);
        handler.setConnectedFaces(l);
    }

    @Override
    public void onNeighborBlockUpdate(BlockPos fromPos) {
        super.onNeighborBlockUpdate(fromPos);

        initializeHullAirHandlers();
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
    public void addLuaMethods(LuaMethodRegistry registry) {
        super.addLuaMethods(registry);

        registry.registerLuaMethod(new LuaMethod("getPressure") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 0, 1, "face (down/up/north/south/west/east)");
                if (args.length == 0) {
                    return new Object[]{airHandler.getPressure()};
                } else {
                    LazyOptional<IAirHandlerMachine> cap = getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, getDirForString((String) args[0]));
                    return new Object[]{ cap.map(IAirHandler::getPressure).orElse(0f) };
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

        registry.registerLuaMethod(new LuaConstant("getDangerPressure", getDangerPressure()));
        registry.registerLuaMethod(new LuaConstant("getCriticalPressure", getCriticalPressure()));
        registry.registerLuaMethod(new LuaConstant("getDefaultVolume", getDefaultVolume()));
    }

    public float getPressure() {
        return airHandler.getPressure();
    }

    public float getDangerPressure() {
        return airHandler.getDangerPressure();
    }

    public float getCriticalPressure() {
        return airHandler.getCriticalPressure();
    }

    public void addAir(int air) {
        airHandler.addAir(air);
    }

    /**
     * Checks if the given side of this TE can be pneumatically connected to.
     *
     * @param side the side to check
     * @return true if connected, false otherwise
     */
    public boolean canConnectPneumatic(Direction side) {
        return true;
    }

    public int getDefaultVolume() {
        return airHandler.getBaseVolume();
    }

    public void forceLeak(Direction dir) {
        airHandler.setSideLeaking(dir);
    }

    public boolean hasNoConnectedAirHandlers() {
        return airHandler.getConnectedAirHandlers(this).isEmpty();
    }
}
