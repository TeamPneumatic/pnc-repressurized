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

package me.desht.pneumaticcraft.common.block.entity;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.lib.NBTKeys;
import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.common.core.ModUpgrades;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.LuaConstant;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.LuaMethod;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.LuaMethodRegistry;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Base class for all PNC tile entities which handle air. Provides one default air handler; machines with multiple
 * air handlers can add extra handlers in their subclass.
 */
public abstract class AbstractAirHandlingBlockEntity extends AbstractTickingBlockEntity {
    @GuiSynced
    protected final IAirHandlerMachine airHandler;
    private LazyOptional<IAirHandlerMachine> airHandlerCap;
    private final Map<IAirHandlerMachine, List<Direction>> airHandlerMap = new IdentityHashMap<>();

    public AbstractAirHandlingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, PressureTier pressureTier, int volume, int upgradeSlots) {
        super(type, pos, state, upgradeSlots);

        this.airHandler = PneumaticRegistry.getInstance().getAirHandlerMachineFactory().createAirHandler(pressureTier, volume);
        this.airHandlerCap = LazyOptional.of(() -> airHandler);
    }

    @Override
    public void invalidateCaps() {
        this.airHandlerCap.invalidate();
        this.airHandlerCap = LazyOptional.empty();
        super.invalidateCaps();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        this.airHandlerCap = LazyOptional.of(() -> airHandler);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);

        initializeHullAirHandlers();
    }

    @Override
    public void onLoad() {
        super.onLoad();

        initializeHullAirHandlers();
    }

    @Override
    public void tickCommonPre() {
        super.tickCommonPre();

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

        airHandler.setVolumeUpgrades(getUpgrades(ModUpgrades.VOLUME.get()));
        handleSecurityUpgrade(airHandler);

        airHandlerMap.keySet().forEach(h -> {
            h.setVolumeUpgrades(getUpgrades(ModUpgrades.VOLUME.get()));
            handleSecurityUpgrade(h);
        });
    }

    private void handleSecurityUpgrade(IAirHandlerMachine handler) {
        if (getUpgrades(ModUpgrades.SECURITY.get()) > 0) {
            handler.enableSafetyVenting(p -> p > getDangerPressure(), Direction.UP);
        } else {
            handler.disableSafetyVenting();
        }
    }

    @Override
    public void onBlockRotated() {
        super.onBlockRotated();

        // force a recalculation of where any possible leak might be coming from
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
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(NBTKeys.NBT_AIR_HANDLER, airHandler.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        airHandler.deserializeNBT(tag.getCompound(NBTKeys.NBT_AIR_HANDLER));
        airHandler.setVolumeUpgrades(getUpgrades(ModUpgrades.VOLUME.get()));
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
    // this ensures the BE can tick this air handler for air leak sound and particle purposes
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
    public void writeToPacket(CompoundTag tag) {
        super.writeToPacket(tag);
        tag.put(NBTKeys.NBT_AIR_HANDLER, airHandler.serializeNBT());
    }

    @Override
    public void readFromPacket(CompoundTag tag) {
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

        if (this instanceof final IMinWorkingPressure mwp) {
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
     * Checks if the given side of this BE can be pneumatically connected to.
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

    public boolean hasNoConnectedAirHandlers() {
        return airHandler.getConnectedAirHandlers(this).isEmpty();
    }
}
