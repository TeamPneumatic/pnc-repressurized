package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableBlock;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.event.HackTickHandler;
import me.desht.pneumaticcraft.common.hacking.HackableHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server when a block hack has completed.
 */
public class PacketHackingBlockFinish extends LocationIntPacket {

    public PacketHackingBlockFinish() {
    }

    public PacketHackingBlockFinish(BlockPos pos) {
        super(pos);
    }

    public PacketHackingBlockFinish(GlobalPos gPos) {
        super(gPos.getPos());
    }

    public PacketHackingBlockFinish(PacketBuffer buffer) {
        super(buffer);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = PneumaticCraftRepressurized.proxy.getClientPlayer();
            IHackableBlock hackableBlock = HackableHandler.getHackableForCoord(player.world, pos, player);
            if (hackableBlock != null) {
                hackableBlock.onHackFinished(player.world, pos, player);
                HackTickHandler.instance().trackBlock(GlobalPos.of(player.world.getDimension().getType(), pos), hackableBlock);
                CommonArmorHandler.getHandlerForPlayer(player).setHackedBlockPos(null);
                player.playSound(ModSounds.HELMET_HACK_FINISH, 1.0F, 1.0F);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
