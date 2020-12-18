package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client to set the status of multiple armor features at once
 * (used in armor init & core components reset)
 */
public class PacketToggleArmorFeatureBulk {
    private final List<FeatureSetting> features;

    public PacketToggleArmorFeatureBulk(List<FeatureSetting> features) {
        this.features = features;
    }

    PacketToggleArmorFeatureBulk(PacketBuffer buffer) {
        this.features = new ArrayList<>();
        int len = buffer.readVarInt();
        for (int i = 0; i < len; i++) {
            features.add(new FeatureSetting(buffer));
        }
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeVarInt(features.size());
        features.forEach(f -> f.toBytes(buf));
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = ctx.get().getSender();
            if (player != null) {
                CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
                features.forEach(f -> {
                    if (f.featureIndex >= 0 && f.featureIndex < ArmorUpgradeRegistry.getInstance().getHandlersForSlot(f.slot).size()
                        && ItemPneumaticArmor.isPneumaticArmorPiece(player, f.slot)
                        && handler.isUpgradeInserted(f.slot, f.featureIndex))
                    {
                        handler.setUpgradeEnabled(f.slot, f.featureIndex, f.state);
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public static class FeatureSetting {
        private final EquipmentSlotType slot;
        private final byte featureIndex;
        private final boolean state;

        FeatureSetting(PacketBuffer buffer) {
            this(EquipmentSlotType.values()[buffer.readByte()], buffer.readByte(), buffer.readBoolean());
        }

        public FeatureSetting(EquipmentSlotType slot, byte featureIndex, boolean state) {
            this.slot = slot;
            this.featureIndex = featureIndex;
            this.state = state;
        }

        void toBytes(PacketBuffer buffer) {
            buffer.writeByte(slot.ordinal());
            buffer.writeByte(featureIndex);
            buffer.writeBoolean(state);
        }
    }
}
