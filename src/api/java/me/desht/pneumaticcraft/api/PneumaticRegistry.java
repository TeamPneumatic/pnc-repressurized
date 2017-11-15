package me.desht.pneumaticcraft.api;

import me.desht.pneumaticcraft.api.client.IClientRegistry;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IPneumaticHelmetRegistry;
import me.desht.pneumaticcraft.api.drone.IDroneRegistry;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.api.recipe.IPneumaticRecipeRegistry;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerSupplier;
import me.desht.pneumaticcraft.api.tileentity.IHeatRegistry;
import me.desht.pneumaticcraft.api.universalSensor.ISensorRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.Loader;

/**
 * This class can be used to register and access various things to and from the mod.
 */
public final class PneumaticRegistry {
    private static IPneumaticCraftInterface instance;

    public static IPneumaticCraftInterface getInstance() {
        return instance;
    }

    public static void init(IPneumaticCraftInterface inter) {
        if (instance == null && Loader.instance().activeModContainer().getModId().equals("pneumaticcraft"))
            instance = inter;//only allow initialization once; by PneumaticCraft
        else throw new IllegalStateException("Only pneumaticcraft is allowed to call this method!");
    }

    public interface IPneumaticCraftInterface {

        IPneumaticRecipeRegistry getRecipeRegistry();

        IAirHandlerSupplier getAirHandlerSupplier();

        IPneumaticHelmetRegistry getHelmetRegistry();

        IDroneRegistry getDroneRegistry();

        IHeatRegistry getHeatRegistry();

        IClientRegistry getGuiRegistry();

        ISensorRegistry getSensorRegistry();

        IItemRegistry getItemRegistry();

        /*
         * ---------------- Power Generation -----------
         */

        /**
         * Adds a burnable liquid to the Liquid Compressor's available burnable fuels.  This also allows a bucket
         * of that liquid to be used in furnaces, the burn time being half the mLPerBucket value.
         *
         * @param fluid the fluid
         * @param mLPerBucket the amount of mL generated for 1000mB of the fuel. As comparison, one piece of coal
         *                    generates 16000mL in an Air Compressor.
         */
        void registerFuel(Fluid fluid, int mLPerBucket);

        /*
         * --------------- Misc -------------------
         */

        /**
         * Returns the amount of Security Stations that disallow interaction with the given coordinate for the given player.
         * Usually you'd disallow interaction when this returns > 0.
         *
         * @param world
         * @param pos
         * @param player
         * @param showRangeLines When true, any Security Station that prevents interaction will show the line grid (server --> client update is handled internally).
         * @return The amount of Security Stations that disallow interaction for the given player.
         * This method throws an IllegalArgumentException when tried to be called from the client side!
         */
        int getProtectingSecurityStations(World world, BlockPos pos, EntityPlayer player, boolean showRangeLines);

        /**
         * Used to register a liquid that represents liquid XP (like MFR mob essence, OpenBlocks liquid XP).
         * This is used in the Aerial Interface to pump XP in/out of the player.
         *
         * @param fluid registered name of the fluid (may have been registered by another mod)
         * @param liquidToPointRatio The amount of liquid (in mB) used to get one XP point. In OpenBlocks this is 20 (mB/point).
         */
        void registerXPLiquid(Fluid fluid, int liquidToPointRatio);

        /**
         * Register this fluid as a raw input to the refinery. This methods is deprecated.
         * You should use {@link IPneumaticRecipeRegistry#registerRefineryRecipe(net.minecraftforge.fluids.FluidStack, net.minecraftforge.fluids.FluidStack...)} instead.
         *
         * @param fluid the fluid to register
         */
        @Deprecated
        void registerRefineryInput(Fluid fluid);
    }
}
