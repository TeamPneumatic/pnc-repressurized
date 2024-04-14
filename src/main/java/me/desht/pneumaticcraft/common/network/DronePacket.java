package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.block.entity.drone.ProgrammableControllerBlockEntity;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import javax.annotation.Nullable;

public interface DronePacket extends CustomPacketPayload {
    static void handle(DronePacket message, PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
            Level level = player.level();;
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
        }));
    }

    default int entityId() {
        return droneTarget().entityId();
    }

    default BlockPos pos() {
        return droneTarget().pos();
    }

    DroneTarget droneTarget();

    void handle(Player player, IDroneBase drone);

    record DroneTarget(int entityId, @Nullable BlockPos pos) {
        private static final DroneTarget NONE = new DroneTarget(-1, null);

        public static DroneTarget fromNetwork(FriendlyByteBuf buf) {
            return new DroneTarget(buf.readInt(), buf.readNullable(FriendlyByteBuf::readBlockPos));
        }

        public static DroneTarget forEntity(int entityId) {
            return new DroneTarget(entityId, null);
        }

        public static DroneTarget forPos(BlockPos pos) {
            return new DroneTarget(-1, pos);
        }

        public static DroneTarget none() {
            return NONE;
        }

        public void toNetwork(FriendlyByteBuf buf) {
            buf.writeInt(entityId);
            buf.writeNullable(pos, FriendlyByteBuf::writeBlockPos);
        }
    }
}
