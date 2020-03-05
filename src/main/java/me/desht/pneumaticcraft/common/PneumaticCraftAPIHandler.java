package me.desht.pneumaticcraft.common;

import me.desht.pneumaticcraft.api.PneumaticRegistry.IPneumaticCraftInterface;
import me.desht.pneumaticcraft.api.client.IClientRegistry;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IPneumaticHelmetRegistry;
import me.desht.pneumaticcraft.api.drone.IDroneRegistry;
import me.desht.pneumaticcraft.api.harvesting.IHarvestRegistry;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.api.recipe.IPneumaticRecipeRegistry;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerSupplier;
import me.desht.pneumaticcraft.api.tileentity.IHeatRegistry;
import me.desht.pneumaticcraft.api.universalSensor.ISensorRegistry;
import me.desht.pneumaticcraft.client.GuiRegistry;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.PneumaticHelmetRegistry;
import me.desht.pneumaticcraft.common.fluid.Fluids;
import me.desht.pneumaticcraft.common.harvesting.HarvestRegistry;
import me.desht.pneumaticcraft.common.heat.HeatExchangerManager;
import me.desht.pneumaticcraft.common.item.ItemRegistry;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.pressure.AirHandlerSupplier;
import me.desht.pneumaticcraft.common.recipes.PneumaticRecipeRegistry;
import me.desht.pneumaticcraft.common.sensor.SensorHandler;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * With this class you can register your entities to give more info in the tooltip of the Entity Tracker.
 */
public class PneumaticCraftAPIHandler implements IPneumaticCraftInterface {
    private final static PneumaticCraftAPIHandler INSTANCE = new PneumaticCraftAPIHandler();
    public final Map<Fluid, Integer> liquidXPs = new HashMap<>();
    public final List<Fluid> availableLiquidXPs = new ArrayList<>(); // for cycling through xp fluid types
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
        Validate.isTrue(!world.isRemote, "This method can only be called from the server side!");
        return PneumaticCraftUtils.getProtectingSecurityStations(world, pos, player, showRangeLines, false);
    }

    @Override
    public void registerXPLiquid(Fluid fluid, int liquidToPointRatio) {
        Validate.notNull(fluid, "Fluid may not be null!");
        if (liquidToPointRatio <= 0) {
            liquidXPs.remove(fluid);
            availableLiquidXPs.remove(fluid);
        } else {
            liquidXPs.put(fluid, liquidToPointRatio);
            availableLiquidXPs.add(fluid);
        }
    }

    @Override
    @Deprecated
    public void registerRefineryInput(Fluid fluid) {
    	// Register old refinery mapping for compatibility 
    	PneumaticRecipeRegistry registry = PneumaticRecipeRegistry.getInstance();
    	registry.registerRefineryRecipe(new FluidStack(fluid, 10), new FluidStack(Fluids.DIESEL, 4), new FluidStack(Fluids.LPG, 2));
    	registry.registerRefineryRecipe(new FluidStack(fluid, 10), new FluidStack(Fluids.DIESEL, 2), new FluidStack(Fluids.KEROSENE, 3), new FluidStack(Fluids.LPG, 2));
    	registry.registerRefineryRecipe(new FluidStack(fluid, 10), new FluidStack(Fluids.DIESEL, 2), new FluidStack(Fluids.KEROSENE, 3), new FluidStack(Fluids.GASOLINE, 3), new FluidStack(Fluids.LPG, 2));
    }

    @Override
    public void registerPlasticFluid(Fluid fluid, int ratio) {
        PneumaticRecipeRegistry.getInstance().registerPlasticMixerRecipe(new FluidStack(fluid, ratio), new ItemStack(Itemss.PLASTIC),
                PneumaticValues.PLASTIC_MIXER_MELTING_TEMP, true, true, true, -1);
    }

    @Override
    public void registerFuel(Fluid fluid, int mLPerBucket) {
        Validate.notNull(fluid);
        Validate.isTrue(mLPerBucket >= 0, "mlPerBucket can't be < 0!");
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

    @Override
    public IHarvestRegistry getHarvestRegistry(){
        return HarvestRegistry.getInstance();
    }
}
