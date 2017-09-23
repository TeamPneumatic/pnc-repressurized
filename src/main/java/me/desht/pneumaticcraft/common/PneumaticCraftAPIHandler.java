package me.desht.pneumaticcraft.common;

import me.desht.pneumaticcraft.api.PneumaticRegistry.IPneumaticCraftInterface;
import me.desht.pneumaticcraft.api.client.IClientRegistry;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IPneumaticHelmetRegistry;
import me.desht.pneumaticcraft.api.drone.IDroneRegistry;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.api.recipe.IPneumaticRecipeRegistry;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerSupplier;
import me.desht.pneumaticcraft.api.tileentity.IHeatRegistry;
import me.desht.pneumaticcraft.api.universalSensor.ISensorRegistry;
import me.desht.pneumaticcraft.client.GuiRegistry;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.PneumaticHelmetRegistry;
import me.desht.pneumaticcraft.common.heat.HeatExchangerManager;
import me.desht.pneumaticcraft.common.item.ItemRegistry;
import me.desht.pneumaticcraft.common.pressure.AirHandlerSupplier;
import me.desht.pneumaticcraft.common.recipes.PneumaticRecipeRegistry;
import me.desht.pneumaticcraft.common.sensor.SensorHandler;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.HashMap;
import java.util.Map;

/**
 * With this class you can register your entities to give more info in the tooltip of the Entity Tracker.
 */
public class PneumaticCraftAPIHandler implements IPneumaticCraftInterface {
    private final static PneumaticCraftAPIHandler INSTANCE = new PneumaticCraftAPIHandler();
    public final Map<Fluid, Integer> liquidXPs = new HashMap<>();
    public final Map<String, Integer> liquidFuels = new HashMap<>();

    public static PneumaticCraftAPIHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public IPneumaticRecipeRegistry getRecipeRegistry() {
        return PneumaticRecipeRegistry.getInstance();
    }

    @Override
    public IAirHandlerSupplier getAirHandlerSupplier() {
        return AirHandlerSupplier.getInstance();
    }

    @Override
    public IPneumaticHelmetRegistry getHelmetRegistry() {
        return PneumaticHelmetRegistry.getInstance();
    }

    @Override
    public IDroneRegistry getDroneRegistry() {
        return DroneRegistry.getInstance();
    }

    @Override
    public IHeatRegistry getHeatRegistry() {
        return HeatExchangerManager.getInstance();
    }

    @Override
    public int getProtectingSecurityStations(World world, BlockPos pos, EntityPlayer player, boolean showRangeLines) {
        if (world.isRemote) throw new IllegalArgumentException("This method can only be called from the server side!");
        return PneumaticCraftUtils.getProtectingSecurityStations(world, pos, player, showRangeLines, false);
    }

    @Override
    public void registerXPLiquid(Fluid fluid, int liquidToPointRatio) {
        if (fluid == null) throw new NullPointerException("Fluid can't be null!");
        if (liquidToPointRatio <= 0) throw new IllegalArgumentException("liquidToPointRatio can't be <= 0");
        liquidXPs.put(fluid, liquidToPointRatio);
    }

    @Override
    public void registerFuel(Fluid fluid, int mLPerBucket) {
        if (fluid == null) throw new NullPointerException("Fluid can't be null!");
        if (mLPerBucket < 0) throw new IllegalArgumentException("mLPerBucket can't be < 0");
        if (liquidFuels.containsKey(fluid.getName())) {
            Log.info("Overriding liquid fuel entry " + fluid.getLocalizedName(new FluidStack(fluid, 1)) + " (" + fluid.getName() + ") with a fuel value of " + mLPerBucket + " (previously " + liquidFuels.get(fluid.getName()) + ")");
            if (mLPerBucket == 0) liquidFuels.remove(fluid.getName());
        }
        if (mLPerBucket > 0) liquidFuels.put(fluid.getName(), mLPerBucket);
    }

    @Override
    public IClientRegistry getGuiRegistry() {
        return GuiRegistry.getInstance();
    }

    @Override
    public ISensorRegistry getSensorRegistry() {
        return SensorHandler.getInstance();
    }

    @Override
    public IItemRegistry getItemRegistry() {
        return ItemRegistry.getInstance();
    }
}
