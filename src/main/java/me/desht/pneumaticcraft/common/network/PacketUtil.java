package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.inventory.ContainerPneumaticBase;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Optional;

public class PacketUtil {
    public static void writeGlobalPos(PacketBuffer buf, GlobalPos gPos) {
        buf.writeResourceLocation(gPos.getDimension().getLocation());
        buf.writeBlockPos(gPos.getPos());
    }

    public static GlobalPos readGlobalPos(PacketBuffer buf) {
        RegistryKey<World> worldKey = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, buf.readResourceLocation());
        BlockPos pos = buf.readBlockPos();
        return GlobalPos.getPosition(worldKey, pos);
    }

    /**
     * Get the relevant target tile entity for packet purposes.  When the packet is
     * being received on the server, the player's open container is used to determine
     * the TE; don't trust a blockpos that the client sent, although we'll check the
     * sent blockpos is the same as the TE's actual blockpos.
     *
     * @param player the player, will be null if packet is being received on client
     * @param pos the blockpos, ignored if packet is being received on server
     * @param cls the desired tile entity class
     * @return the relevant tile entity, or Optional.empty() if none can be found
     */
    @Nonnull
    public static <T extends TileEntity> Optional<T> getTE(PlayerEntity player, BlockPos pos, Class<T> cls) {
        if (player == null) {
            // client-side: we trust the blockpos the server sends
            World w = ClientUtils.getClientWorld();
            if (w != null) {
                return PneumaticCraftUtils.getTileEntityAt(w, pos, cls);
            }
        } else {
            // server-side: don't trust the blockpos the client sent us
            // instead get the TE from the player's open container
            if (player.openContainer instanceof ContainerPneumaticBase) {
                TileEntity te = ((ContainerPneumaticBase<?>) player.openContainer).te;
                if (te != null && cls.isAssignableFrom(te.getClass()) && (pos == null || te.getPos().equals(pos))) {
                    //noinspection unchecked
                    return Optional.of((T) te);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Server-only variant of {@link #getTE(PlayerEntity, BlockPos, Class)}
     *
     * @param player the player
     * @param cls the desired tile entity class
     * @return the relevant tile entity, or Optional.empty() if none can be found
     */
    @Nonnull
    public static <T extends TileEntity> Optional<T> getTE(PlayerEntity player, Class<T> cls) {
        if (player.world.isRemote) throw new RuntimeException("don't call this method client side!");
        return getTE(player, null, cls);
    }
}
