package me.desht.pneumaticcraft.common.util.chunkloading;

import com.mojang.datafixers.util.Either;
import me.desht.pneumaticcraft.common.block.entity.drone.ProgrammableControllerBlockEntity;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.common.world.chunk.TicketController;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class DynamicChunkLoader {
    private final TicketController ticketController;
    private final Either<DroneEntity, BlockPos> subject;
    private final Supplier<UUID> playerIdGetter;
    private final Predicate<ChunkPos> loadChecker;
    private final Set<ChunkPos> loadedChunks = new HashSet<>();

    private DynamicChunkLoader(TicketController ticketController, Either<DroneEntity, BlockPos> subject, Supplier<UUID> playerIdGetter, Predicate<ChunkPos> loadChecker) {
        this.ticketController = ticketController;
        this.subject = subject;
        this.playerIdGetter = playerIdGetter;
        this.loadChecker = loadChecker;
    }

    public static DynamicChunkLoader forDrone(DroneEntity drone) {
        return new DynamicChunkLoader(ForcedChunks.INSTANCE.getDroneController(), Either.left(drone), drone::getOwnerUUID, drone::shouldLoadChunk);
    }

    public static DynamicChunkLoader forProgrammableController(ProgrammableControllerBlockEntity pc) {
        return new DynamicChunkLoader(ForcedChunks.INSTANCE.getPcController(), Either.right(pc.getBlockPos()), pc::getOwnerUUID, pc::shouldLoadChunk);
    }

    public void updateLoadedChunks(ServerLevel level, ChunkPos origin) {
        for (int cx = origin.x - 1; cx <= origin.x + 1; cx++) {
            for (int cz = origin.z - 1; cz <= origin.z + 1; cz++) {
                ChunkPos cp = new ChunkPos(cx, cz);
                if (shouldLoadChunk(level.getServer(), cp)) {
                    loadedChunks.add(cp);
                }
            }
        }

        Iterator<ChunkPos> iter = loadedChunks.iterator();
        while (iter.hasNext()) {
            ChunkPos cp = iter.next();
            boolean shouldLoad = shouldLoadChunk(level.getServer(), cp);
            setChunkLoaded(level, cp, shouldLoad);
            if (!shouldLoad) {
                iter.remove();
            }
        }
        Log.info("updated chunks for {} - {} loaded", playerIdGetter.get(), loadedChunks.size());
    }

    public void unloadAll(ServerLevel level) {
        Log.info("unloading chunks for {}", playerIdGetter.get());
        loadedChunks.forEach(cp -> setChunkLoaded(level, cp, false));
        loadedChunks.clear();
    }

    private boolean shouldLoadChunk(MinecraftServer server, ChunkPos cp) {
        return !PlayerLogoutTracker.INSTANCE.isPlayerLoggedOutTooLong(server, playerIdGetter.get())
                && loadChecker.test(cp);
    }

    private void setChunkLoaded(ServerLevel level, ChunkPos cp, boolean loaded) {
        subject.map(
                drone -> ticketController.forceChunk(level, drone, cp.x, cp.z, loaded, true),
                blockPos -> ticketController.forceChunk(level, blockPos, cp.x, cp.z, loaded, true)
        );
    }
}
