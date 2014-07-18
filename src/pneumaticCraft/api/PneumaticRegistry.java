package pneumaticCraft.api;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import pneumaticCraft.api.client.pneumaticHelmet.IEntityTrackEntry;
import pneumaticCraft.api.client.pneumaticHelmet.IHackableBlock;
import pneumaticCraft.api.client.pneumaticHelmet.IHackableEntity;
import pneumaticCraft.api.drone.IPathfindHandler;

/**
 * This class can be used to register and access various things to and from the mod.
 */
public class PneumaticRegistry{
    /**
     * This field, which is initialized in PneumaticCraft's preInit, will give you access to various registration and access options.
     */
    public static IPneumaticCraftInterface instance;

    public static interface IPneumaticCraftInterface{

        /*
         * ------------- Hacking --------------
         */

        public void registerEntityTrackEntry(Class<? extends IEntityTrackEntry> entry);

        public void addHackable(Class<? extends Entity> entityClazz, Class<? extends IHackableEntity> iHackable);

        public void addHackable(Block block, Class<? extends IHackableBlock> iHackable);

        /**
         * Returns a list of all current successful hacks of a given entity. This is used for example in Enderman hacking, so the user
         * can only hack an enderman once (more times wouldn't have any effect). This is mostly used for display purposes.
         * @param entity
         * @return empty list if no hacks.
         */
        public List<IHackableEntity> getCurrentEntityHacks(Entity entity);

        /*
         * ------------- Drones --------------
         */

        /**
         * Normally drones will pathfind through any block that doesn't have any collisions (Block#getBlocksMovement returns true).
         * With this method you can register custom blocks to allow the drone to pathfind through them. If the block requires any special
         * handling, like allow pathfinding on certain conditions, you can pass a IPathFindHandler with the registry.
         * @param block
         * @param handler can be null, to always allow pathfinding through this block.
         */
        public void addPathfindableBlock(Block block, IPathfindHandler handler);

        /**
         * Returns the amount of Security Stations that disallow interaction with the given coordinate for the given player.
         * Usually you'd disallow interaction when this returns > 0.
         * @param world
         * @param x
         * @param y
         * @param z
         * @param player
         * @param showRangeLines When true, any Security Station that prevents interaction will show the line grid (server --> client update is handled internally).
         * @return The amount of Security Stations that disallow interaction for the given player.
         * This method throws an IllegalArgumentException when tried to be called from the client side!
         */
        public int getProtectingSecurityStations(World world, int x, int y, int z, EntityPlayer player, boolean showRangeLines);
    }
}
