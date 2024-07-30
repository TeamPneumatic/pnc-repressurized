package me.desht.pneumaticcraft.common.network;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.common.block.entity.drone.ProgrammableControllerBlockEntity;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public interface DronePacket extends CustomPacketPayload {
    static void handle(DronePacket message, IPayloadContext ctx) {
        Player player = ctx.player();
        Level level = player.level();
        if (message.entityId() >= 0) {
            Entity entity = level.getEntity(message.entityId());
            if (entity instanceof DroneEntity drone) {
                message.handle(player, drone);
            }
        } else if (message.pos() != null) {
            BlockEntity te = level.getBlockEntity(message.pos());
            if (te instanceof ProgrammableControllerBlockEntity pc) {
                message.handle(player, pc);
            }
        } else {
            message.handle(player, null);
        }
    }

    default int entityId() {
        return droneTarget().idOrPos.left().orElse(-1);
    }

    default BlockPos pos() {
        return droneTarget().idOrPos.right().orElse(null);
    }

    DroneTarget droneTarget();

    void handle(Player player, IDroneBase drone);

    record DroneTarget(Either<Integer,BlockPos> idOrPos) {
        public static final DroneTarget NONE = new DroneTarget(Either.left(-1));

        public static final Codec<DroneTarget> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.either(Codec.INT, BlockPos.CODEC).fieldOf("id_or_pos").forGetter(DroneTarget::idOrPos)
        ).apply(builder, DroneTarget::new));

        public static final StreamCodec<FriendlyByteBuf, DroneTarget> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.either(ByteBufCodecs.INT, BlockPos.STREAM_CODEC), DroneTarget::idOrPos,
                DroneTarget::new
        );

        public static DroneTarget forEntityId(int entityId) {
            return new DroneTarget(Either.left(entityId));
        }

        public static DroneTarget forPos(BlockPos pos) {
            return new DroneTarget(Either.right(pos));
        }

        public static DroneTarget none() {
            return NONE;
        }

        public boolean is(Entity e) {
            return idOrPos.map(id -> id == e.getId(), pos -> false);
        }

        public boolean is(BlockPos p) {
            return idOrPos.map(id -> false, p::equals);
        }

        public IDroneBase getDrone(Level level) {
            return this == NONE ? null : idOrPos.map(
                    id -> level.getEntity(id) instanceof IDroneBase drone ? drone : null,
                    pos -> level.getBlockEntity(pos) instanceof IDroneBase drone ? drone : null
            );
        }
    }
}
