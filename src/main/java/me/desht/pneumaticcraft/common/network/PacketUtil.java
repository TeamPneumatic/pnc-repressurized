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

package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.inventory.AbstractPneumaticCraftMenu;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;

public class PacketUtil {
    public static void writeGlobalPos(FriendlyByteBuf buf, GlobalPos gPos) {
        buf.writeResourceLocation(gPos.dimension().location());
        buf.writeBlockPos(gPos.pos());
    }

    public static GlobalPos readGlobalPos(FriendlyByteBuf buf) {
        ResourceKey<Level> worldKey = ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation());
        BlockPos pos = buf.readBlockPos();
        return GlobalPos.of(worldKey, pos);
    }

    public static void writeVec3f(Vector3f vec, FriendlyByteBuf buf) {
        buf.writeFloat(vec.x);
        buf.writeFloat(vec.y);
        buf.writeFloat(vec.z);
    }

    public static Vector3f readVec3f(FriendlyByteBuf buf) {
        return new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    /**
     * Get the relevant target block entity for packet purposes.  When the packet is
     * being received on the server, the player's open container is used to determine
     * the BE; don't trust a blockpos that the client sent, although we'll check the
     * sent blockpos is the same as the BE's actual blockpos.
     * <p>
     * Important: <strong>cannot</strong> be used to sync changes after the server-side
     * container could be closed, i.e. don't use this in packets sent from a GUI {@code onClose()} method.
     *
     * @param player the player, will be null if packet is being received on client
     * @param pos the blockpos, ignored if packet is being received on server
     * @param cls the desired block entity class
     * @return the relevant block entity, or Optional.empty() if none can be found
     */
    @Nonnull
    public static <T extends BlockEntity> Optional<T> getBlockEntity(Player player, BlockPos pos, Class<T> cls) {
        if (player == null || player.level().isClientSide) {
            // client-side: we trust the blockpos the server sends
            return ClientUtils.getOptionalClientLevel().flatMap(level -> PneumaticCraftUtils.getTileEntityAt(level, pos, cls));
        } else {
            // server-side: don't trust the blockpos the client sent us
            // instead get the BE from the player's open container
            if (player.containerMenu instanceof AbstractPneumaticCraftMenu<?> pncMenu) {
                BlockEntity te = pncMenu.blockEntity;
                if (te != null && cls.isAssignableFrom(te.getClass()) && (pos == null || te.getBlockPos().equals(pos))) {
                    //noinspection unchecked
                    return Optional.of((T) te);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Server-only variant of {@link #getBlockEntity(Player, BlockPos, Class)}
     *
     * @param player the player
     * @param cls the desired block entity class
     * @return the relevant block entity, or Optional.empty() if none can be found
     */
    @Nonnull
    public static <T extends BlockEntity> Optional<T> getBlockEntity(Player player, Class<T> cls) {
        if (player.level().isClientSide) throw new RuntimeException("don't call this method client side!");
        return getBlockEntity(player, null, cls);
    }

    /**
     * Write an optional blockstate to the network
     * @param buf the packet buffer
     * @param state the state to write
     */
    public static void writeOptionalBlockState(FriendlyByteBuf buf, @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<BlockState> state) {
        buf.writeOptional(state, (b, state1) -> b.writeNbt(NbtUtils.writeBlockState(state1)));
    }

    /**
     * Read an optional blockstate from the network
     * @param buf the packet buffer
     * @return the blockstate, may be null
     */
    public static Optional<BlockState> readOptionalBlockState(FriendlyByteBuf buf) {
        return buf.readOptional(b -> {
            CompoundTag tag = b.readNbt();
            return NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), Objects.requireNonNull(tag));
        });
    }
}
