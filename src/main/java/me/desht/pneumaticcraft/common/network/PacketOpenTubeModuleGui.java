package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.client.gui.tubemodule.GuiTubeModule;
import me.desht.pneumaticcraft.common.block.BlockPressureTube;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server when it needs the client to open a (containerless) module GUI
 */
public class PacketOpenTubeModuleGui extends LocationIntPacket {
    private String moduleType;

    public PacketOpenTubeModuleGui() {
        // empty
    }

    public PacketOpenTubeModuleGui(String type, BlockPos pos) {
        super(pos);
        this.moduleType = type;
    }

    PacketOpenTubeModuleGui(PacketBuffer buffer) {
        super(buffer);
        moduleType = buffer.readString();
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        super.toBytes(buf);
        buf.writeString(moduleType);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = PneumaticCraftRepressurized.proxy.getClientPlayer();
            if (BlockPressureTube.getFocusedModule(player.world, pos, player) != null) {
                GuiTubeModule.openGuiForType(moduleType, pos);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
