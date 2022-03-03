package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.item.ClassifyFilterItem;
import me.desht.pneumaticcraft.common.item.ClassifyFilterItem.FilterCondition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by Processing Filter GUI to update the filter settings on a held Processing Filter item
 */
public class PacketSyncClassifyFilter {
    private final boolean matchAll;
    private final Set<FilterCondition> conditions;
    private final InteractionHand handIn;

    public PacketSyncClassifyFilter(boolean matchAll, Set<FilterCondition> conditions, InteractionHand handIn) {
        this.matchAll = matchAll;
        this.conditions = conditions;
        this.handIn = handIn;
    }

    public PacketSyncClassifyFilter(FriendlyByteBuf buf) {
        this.matchAll = buf.readBoolean();
        int n = buf.readVarInt();
        this.conditions = EnumSet.noneOf(FilterCondition.class);
        for (int i = 0; i < n; i++) {
            conditions.add(buf.readEnum(FilterCondition.class));
        }
        this.handIn = buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(matchAll);
        buf.writeVarInt(conditions.size());
        conditions.forEach(buf::writeEnum);
        buf.writeBoolean(handIn == InteractionHand.MAIN_HAND);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ItemStack stack = player.getItemInHand(handIn);
                if (stack.getItem() instanceof ClassifyFilterItem) {
                    new ClassifyFilterItem.FilterSettings(matchAll, conditions).save(stack);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
