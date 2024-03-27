package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.item.ClassifyFilterItem;
import me.desht.pneumaticcraft.common.item.ClassifyFilterItem.FilterCondition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.EnumSet;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Sent by Processing Filter GUI to update the filter settings on a held Processing Filter item
 */
public record PacketSyncClassifyFilter(boolean matchAll, EnumSet<FilterCondition> conditions, InteractionHand hand) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("sync_classify_filter");

    public static PacketSyncClassifyFilter fromNetwork(FriendlyByteBuf buf) {
        return new PacketSyncClassifyFilter(buf.readBoolean(), buf.readEnumSet(FilterCondition.class), buf.readEnum(InteractionHand.class));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(matchAll);
        buf.writeEnumSet(conditions, FilterCondition.class);
        buf.writeEnum(hand);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketSyncClassifyFilter message, PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
                ItemStack stack = player.getItemInHand(message.hand());
                if (stack.getItem() instanceof ClassifyFilterItem) {
                    new ClassifyFilterItem.FilterSettings(message.matchAll(), message.conditions()).save(stack);
                }
        }));
    }
}
