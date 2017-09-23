package me.desht.pneumaticcraft.api.client.pneumaticHelmet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

/**
 * Use this interface to specify any hackable block. When it's your block, you can simply implement this interface in the
 * block's class. If you don't have access to the class (vanilla blocks), you can implement this interface in a separate class
 * and register it using PneumaticRegistry.registry.addHackable(blockClass, IHackableBlockClass). With the former way there will be one instance only per type. In the latter, there will
 * be an IHackableBlock instance for every block.
 */
public interface IHackableBlock {
    /**
     * Should return a unique id to represent this hackable. Used in NBT saving to be able to trigger the afterHackTime after a server restart.
     * Null is a valid return: afterHackTick will not be triggered at all in that case.
     * <p>
     * CURRENTLY THIS ISN'T IMPLEMENTED.
     *
     * @return
     */
    String getId();

    /**
     * Returning true will allow the player to hack this block. This can be used to only allow hacking on certain conditions.
     */
    boolean canHack(IBlockAccess world, BlockPos pos, EntityPlayer player);

    /**
     * Add info that is displayed on the tracker tooltip here. Text like "Hack to explode" can be added.
     * This method is only called when canHack(World, int, int, int) returned true.
     * The added lines automatically will be tried to get localized.
     */
    void addInfo(World world, BlockPos pos, List<String> curInfo, EntityPlayer player);

    /**
     * Add info that is being displayed after hacking, as long as 'afterHackTick' is returning true.
     * Things like "Neutralized".
     * The added lines automatically will be tried to get localized.
     *
     * @param curInfo
     * @param player
     * @param entity
     */
    void addPostHackInfo(World world, BlockPos pos, List<String> curInfo, EntityPlayer player);

    /**
     * Return the time it takes to hack this block in ticks. For more powerful hacks, a longer required hacking time is adviced.
     */
    int getHackTime(IBlockAccess world, BlockPos pos, EntityPlayer player);

    /**
     * When the player hacked the block for getHackTime(World, int, int, int) ticks this will be called on both server and client side.
     */
    void onHackFinished(World world, BlockPos pos, EntityPlayer player);

    /**
     * Called every tick after the hacking finished (on both server and client side). Returning true will keep this going (for mob spawners, to keep them neutralized),
     * or false to stop ticking (for door/lever hacking).
     * <p>
     * CURRENTLY THIS METHOD WILL STOP GETTING INVOKED AFTER A SERVER RESTART!
     *
     * @param world
     * @param pos
     * @return
     */
    boolean afterHackTick(World world, BlockPos pos);
}
