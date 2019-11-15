package me.desht.pneumaticcraft.common;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.client.IClientRegistry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IPneumaticHelmetRegistry;
import me.desht.pneumaticcraft.api.drone.IDroneRegistry;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.api.recipe.IPneumaticRecipeRegistry;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerSupplier;
import me.desht.pneumaticcraft.api.tileentity.IHeatRegistry;
import me.desht.pneumaticcraft.api.universal_sensor.ISensorRegistry;
import me.desht.pneumaticcraft.client.GuiRegistry;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.PneumaticHelmetRegistry;
import me.desht.pneumaticcraft.common.heat.HeatExchangerManager;
import me.desht.pneumaticcraft.common.item.ItemRegistry;
import me.desht.pneumaticcraft.common.pressure.AirHandlerSupplier;
import me.desht.pneumaticcraft.common.recipes.PneumaticRecipeRegistry;
import me.desht.pneumaticcraft.common.sensor.SensorHandler;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * With this class you can register your entities to give more info in the tooltip of the Entity Tracker.
 */
public class PneumaticCraftAPIHandler implements PneumaticRegistry.IPneumaticCraftInterface {
    private final static PneumaticCraftAPIHandler INSTANCE = new PneumaticCraftAPIHandler();
    public final Map<Fluid, Integer> liquidXPs = new HashMap<>();
    public final List<Fluid> availableLiquidXPs = new ArrayList<>(); // for cycling through xp fluid types
    public final Map<ResourceLocation, Integer> liquidFuels = new HashMap<>();

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
    public int getProtectingSecurityStations(World world, BlockPos pos, PlayerEntity player, boolean showRangeLines) {
        Validate.isTrue(!world.isRemote, "This method can only be called from the server side!");
        return PneumaticCraftUtils.getProtectingSecurityStations(world, pos, player, showRangeLines, false);
    }

    @Override
    public void registerXPFluid(Fluid fluid, int liquidToPointRatio) {
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
    public void registerFuel(Fluid fluid, int mLPerBucket) {
        Validate.notNull(fluid);
        Validate.isTrue(mLPerBucket >= 0, "mlPerBucket can't be < 0!");
        if (liquidFuels.containsKey(fluid.getRegistryName())) {
            Log.info("Overriding liquid fuel entry " + new FluidStack(fluid, 1).getDisplayName().getFormattedText() + " (" + fluid.getRegistryName() + ") with a fuel value of " + mLPerBucket + " (previously " + liquidFuels.get(fluid.getRegistryName()) + ")");
            if (mLPerBucket == 0) {
                liquidFuels.remove(fluid.getRegistryName());
            }
        }
        if (mLPerBucket > 0) liquidFuels.put(fluid.getRegistryName(), mLPerBucket);
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
