package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.item.IGPSToolSync;
import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.commons.lang3.Validate;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Send when the GPS Tool GUI is closed, to update the held GPS tool settings
 */
public class PacketChangeGPSToolCoordinate extends LocationIntPacket {
    private final Hand hand;
    private final String variable;
    private final int index;

    public PacketChangeGPSToolCoordinate(BlockPos pos, Hand hand, String variable, int index) {
        super(pos);
        Validate.isTrue(variable.isEmpty() || GlobalVariableHelper.hasPrefix(variable), "variable missing # or %% prefix!");
        this.hand = hand;
        this.variable = variable;
        this.index = index;
    }

    public PacketChangeGPSToolCoordinate(PacketBuffer buf) {
        super(buf);
        variable = buf.readString(GlobalVariableManager.MAX_VARIABLE_LEN + 1);
        index = buf.readByte();
        hand = buf.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        super.toBytes(buf);
        buf.writeString(variable);
        buf.writeByte(index);
        buf.writeBoolean(hand == Hand.MAIN_HAND);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null) {
                ItemStack stack = player.getHeldItem(hand);
                if (stack.getItem() instanceof IGPSToolSync) {
                    ((IGPSToolSync) stack.getItem()).syncFromClient(player, stack, index, pos, variable);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
