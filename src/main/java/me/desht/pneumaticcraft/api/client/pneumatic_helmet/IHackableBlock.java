package me.desht.pneumaticcraft.api.client.pneumatic_helmet;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Use this interface to specify any hackable block. When it's your block, you can simply implement this interface in
 * your block's class. If you don't have access to the class (vanilla blocks or blocks from other mods), you can
 * implement this interface in a separate class and register it using
 * {@link IPneumaticHelmetRegistry#addHackable(Block, Supplier)} . With the former way there will be one
 * instance only per type. In the latter, there will be an IHackableBlock instance for every block.
 */
public interface IHackableBlock {
    /**
     * Get a unique id to represent this hackable. Used in NBT saving to be able to trigger the afterHackTime
     * after a server restart.  Null is a valid return: afterHackTick will not be triggered at all in that case.
     * <p>
     * The returned ResourceLocation should be in the namespace of the mod which adds the hack (which is not necessarily
     * the mod that adds the hackable block).
     * <p>
     * CURRENTLY THIS ISN'T IMPLEMENTED.
     *
     * @return a unique ID for this hack type
     */
    ResourceLocation getHackableId();

    /**
     * Returning true will allow the player to hack this block. This can be used to only allow hacking on certain conditions.
     *
     * @param world the world
     * @param pos the block pos
     * @param player the player observing the block
     */
    default boolean canHack(IBlockReader world, BlockPos pos, PlayerEntity player) {
        return true;
    }

    /**
     * Add info that is displayed on the tracker tooltip here. Text like "Hack to explode" can be added.
     * This method is only called when {@link #canHack(IBlockReader, BlockPos, PlayerEntity)} has returned true.
     * Added lines are automatically localised where possible.
     *
     * @param world the world
     * @param pos the block pos
     * @param curInfo string list to add info to
     * @param player the player observing the hackable block
     */
    void addInfo(IBlockReader world, BlockPos pos, List<String> curInfo, PlayerEntity player);

    /**
     * Add info that is being displayed after hacking, as long as 'afterHackTick' is returning true.
     * Things like "Neutralized".
     * Added lines are automatically localised where possible.
     *
     * @param world the world
     * @param pos the block pos
     * @param curInfo string list to add info to
     * @param player the player observing the hacked block
     */
    void addPostHackInfo(IBlockReader world, BlockPos pos, List<String> curInfo, PlayerEntity player);

    /**
     * Get the time it takes to hack this block in ticks. For more powerful hacks, a longer hacking time
     * is recommended.
     *
     * @param world the world
     * @param pos the block pos
     * @param player the player observing the hackable block
     */
    int getHackTime(IBlockReader world, BlockPos pos, PlayerEntity player);

    /**
     * When the player has been hacking the block for {@link #getHackTime(IBlockReader, BlockPos, PlayerEntity)} ticks,
     * this will be called on both server and client side.
     *
     * @param world the world
     * @param pos the block pos
     * @param player the player observing the hacked block
     */
    void onHackComplete(World world, BlockPos pos, PlayerEntity player);

    /**
     * Called every tick after the hacking finished (on both server and client side). Returning true will keep this
     * going (for mob spawners, to keep them neutralized), or false to stop ticking for one-shot hacks (e.g. door/lever
     * hacking).
     * <p>
     * CURRENTLY THIS METHOD WILL STOP GETTING INVOKED AFTER A SERVER RESTART!
     *
     * @param world the world
     * @param pos the block pos
     * @return true to keep the hack running (e.g. mob spawners), or false for one-shot hacks (e.g. levers/doors)
     */
    default boolean afterHackTick(IBlockReader world, BlockPos pos) {
        return false;
    }

    /**
     * Fake up a ray trace result for a targetted block. This is intended to be passed into
     * {@link BlockState#onBlockActivated(World, PlayerEntity, Hand, BlockRayTraceResult)}, which needs a non-null
     * ray trace result to get the block's position.
     *
     * @param player player doing the hacking
     * @param targetPos position of the to-be-hacked block
     * @return a ray trace result, or null if the result can't be found
     */
    default Optional<BlockRayTraceResult> fakeRayTrace(PlayerEntity player, BlockPos targetPos) {
        BlockState state = player.world.getBlockState(targetPos);
        AxisAlignedBB aabb = state.getShape(player.world, targetPos).getBoundingBox().offset(targetPos);
        Optional<Vector3d> hit = aabb.rayTrace(player.getEyePosition(1f), aabb.getCenter());
        Direction dir = Direction.getFacingDirections(player)[0];
        return hit.map(v -> new BlockRayTraceResult(v, dir.getOpposite(), targetPos, false));
    }
}
