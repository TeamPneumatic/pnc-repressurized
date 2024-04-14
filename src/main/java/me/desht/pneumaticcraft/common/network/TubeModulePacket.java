package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.block.entity.tube.PressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.tubemodules.AbstractTubeModule;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public interface TubeModulePacket<T extends AbstractTubeModule> extends CustomPacketPayload {
    static <T extends AbstractTubeModule> void handle(TubeModulePacket<T> message, PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
            PneumaticCraftUtils.getTileEntityAt(player.getCommandSenderWorld(), message.locator().pos(), PressureTubeBlockEntity.class).ifPresent(te -> {
                try {
                    // should be safe normally, but we'll catch the exception anyway
                    @SuppressWarnings("unchecked") T tm = (T) te.getModule(message.locator().side());
                    if (tm != null && PneumaticCraftUtils.canPlayerReach(player, te.getBlockPos())) {
                        message.onModuleUpdate(tm, player);
                    }
                } catch (ClassCastException ignored) {
                    // should not happen under normal circumstance
                }
            });
        }));
    }

    ModuleLocator locator();

    void onModuleUpdate(T module, Player player);

    record ModuleLocator(BlockPos pos, Direction side) {
        public static ModuleLocator forModule(AbstractTubeModule module) {
            return new ModuleLocator(module.getTube().getBlockPos(), module.getDirection());
        }

        public static ModuleLocator fromNetwork(FriendlyByteBuf buf) {
            return new ModuleLocator(buf.readBlockPos(), buf.readEnum(Direction.class));
        }

        public void write(FriendlyByteBuf buf) {
            buf.writeBlockPos(pos);
            buf.writeEnum(side);
        }
    }
}
