package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.common.block.tubes.IPneumaticPosProvider;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityPneumaticBase extends TileEntityBase implements IPneumaticPosProvider {
    @GuiSynced
    final IAirHandler airHandler;
    public final float dangerPressure, criticalPressure;
    public final int defaultVolume;

    public TileEntityPneumaticBase(float dangerPressure, float criticalPressure, int volume, int upgradeSlots) {
        super(upgradeSlots);
        airHandler = PneumaticRegistry.getInstance().getAirHandlerSupplier().createAirHandler(dangerPressure, criticalPressure, volume);
        airHandler.setUpgradeSlots(upgradeSlots);
        for (Item upgrade : airHandler.getApplicableUpgrades()) {
            addApplicableUpgrade(upgrade);
        }

        this.dangerPressure = dangerPressure;
        this.criticalPressure = criticalPressure;
        defaultVolume = volume;
        addLuaMethods();
    }

    @Override
    public void update() {
        super.update();
        airHandler.update();
    }

    @Override
    public void validate() {
        super.validate();
        airHandler.validate(this);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        airHandler.writeToNBT(tag);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        airHandler.readFromNBT(tag);
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

//    @Override
//    protected void addLuaMethods() {
//        super.addLuaMethods();
//        luaMethods.add(new LuaMethod("getPressure") {
//            @Override
//            public Object[] call(Object[] args) throws Exception {
//                if (args.length == 0) {
//                    return new Object[]{airHandler.getPressure()};
//                } else if (args.length == 1) {
//                    IAirHandler handler = getAirHandler(getDirForString((String) args[0]));
//                    return new Object[]{handler != null ? handler.getPressure() : 0};
//                } else {
//                    throw new IllegalArgumentException("getPressure method requires 0 or 1 argument (direction: up, down, east, west, north, south!");
//                }
//            }
//        });
//
//        luaMethods.add(new LuaConstant("getDangerPressure", dangerPressure));
//        luaMethods.add(new LuaConstant("getCriticalPressure", criticalPressure));
//        luaMethods.add(new LuaConstant("getDefaultVolume", defaultVolume));
//    }

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
    public IAirHandler getAirHandler(EnumFacing side) {
        return side == null || isConnectedTo(side) ? airHandler : null;
    }

    public float getPressure() {
        return getAirHandler(null).getPressure();
    }

    public void addAir(int air) {
        getAirHandler(null).addAir(air);
    }

    /**
     * Returns if TE's is connected pneumatically to the given side of this TE.
     *
     * @param side
     * @return
     */
    public boolean isConnectedTo(EnumFacing side) {
        return true;
    }
}
