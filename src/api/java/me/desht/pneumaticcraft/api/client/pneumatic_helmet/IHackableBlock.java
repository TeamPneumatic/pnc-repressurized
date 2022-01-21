/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.api.client.pneumatic_helmet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

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
     * Returning true will allow the player to hack this block. This can be used to only allow hacking under certain
     * conditions.
     *
     * @param world the world
     * @param pos the block pos
     * @param player the player observing the block
     */
    default boolean canHack(BlockGetter world, BlockPos pos, Player player) {
        return true;
    }

    /**
     * Add info that is displayed on the tracker tooltip here. Text like "Hack to explode" can be added.
     * This method is only called when {@link #canHack(BlockGetter, BlockPos, Player)} has returned true.
     * Keep this message short; one short sentence is enough.
     * @param world the world
     * @param pos the block pos of the to-be-hacked block
     * @param curInfo text component list to add info to
     * @param player the player observing the hackable block
     */
    void addInfo(BlockGetter world, BlockPos pos, List<Component> curInfo, Player player);

    /**
     * Add info to be displayed on the HUD after hacking is complete, as long as
     * {@link #afterHackTick(BlockGetter, BlockPos)} continues to returning true, e.g. "Spawner Disabled".
     * Keep this message short; one short sentence or even a couple of words is enough.
     * @param world the world
     * @param pos the block pos of the hacked block
     * @param curInfo text component list to add info to
     * @param player the player observing the hacked block
     */
    void addPostHackInfo(BlockGetter world, BlockPos pos, List<Component> curInfo, Player player);

    /**
     * Get the time it takes to hack this block in ticks. For more powerful hacks, a longer hacking time
     * is recommended.
     *
     * @param world the world
     * @param pos the block pos
     * @param player the player observing the hackable block
     */
    int getHackTime(BlockGetter world, BlockPos pos, Player player);

    /**
     * When the player has been hacking the block for {@link #getHackTime(BlockGetter, BlockPos, Player)} ticks,
     * this will be called on both server and client side.
     *
     * @param world the world
     * @param pos the block pos
     * @param player the player observing the hacked block
     */
    void onHackComplete(Level world, BlockPos pos, Player player);

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
    default boolean afterHackTick(BlockGetter world, BlockPos pos) {
        return false;
    }

    /**
     * Fake up a ray trace result for a targeted block. This is intended to be passed into
     * {@link BlockState#use(Level, Player, InteractionHand, BlockHitResult)}, which needs a non-null
     * ray trace result to get the block's position.
     *
     * @param player player doing the hacking
     * @param targetPos position of the to-be-hacked block
     * @return an optional ray trace result
     */
    default Optional<BlockHitResult> fakeRayTrace(Player player, BlockPos targetPos) {
        BlockState state = player.level.getBlockState(targetPos);
        AABB aabb = state.getShape(player.level, targetPos).bounds().move(targetPos);
        Optional<Vec3> hit = aabb.clip(player.getEyePosition(1f), aabb.getCenter());
        Direction dir = Direction.orderedByNearest(player)[0];
        return hit.map(v -> new BlockHitResult(v, dir.getOpposite(), targetPos, false));
    }
}
