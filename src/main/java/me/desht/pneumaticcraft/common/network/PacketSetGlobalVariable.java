package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.client.gui.GuiRemote;
import me.desht.pneumaticcraft.client.render.area.AreaRenderManager;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Received on: BOTH
 * Sync's global variable data between server and client. Used by the Remote and GPS Tool.
 */
public class PacketSetGlobalVariable extends LocationIntPacket {
    private final String varName;

    public PacketSetGlobalVariable(String varName, BlockPos value) {
        super(value);
        this.varName = varName; //.startsWith("#") ? varName.substring(1) : varName;
    }

    public PacketSetGlobalVariable(String varName, int value) {
        this(varName, new BlockPos(value, 0, 0));
    }

    public PacketSetGlobalVariable(String varName, boolean value) {
        this(varName, value ? 1 : 0);
    }

    public PacketSetGlobalVariable(PacketBuffer buf) {
        super(buf);
        this.varName = buf.readString(32767);
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        super.toBytes(buf);
        buf.writeString(varName);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (varName.startsWith("#")) {
                // global
                GlobalVariableManager.getInstance().set(varName.substring(1), pos);
            } else if (varName.startsWith("%")) {
                // player-global
                PlayerEntity p = ctx.get().getSender() == null ? ClientUtils.getClientPlayer() : ctx.get().getSender();
                GlobalVariableManager.getInstance().set(p.getUniqueID(), varName.substring(1), pos);
            }
            if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                GuiRemote.maybeHandleVariableChange(varName);
                AreaRenderManager.getInstance().clearPosProviderCache();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
