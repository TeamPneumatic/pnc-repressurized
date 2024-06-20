/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.*;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * General packet for updating various pneumatic armor settings from client GUI
 */
public record PacketUpdateArmorExtraData(EquipmentSlot slot, ResourceLocation upgradeID,
                                         DataComponentPatch patch) implements CustomPacketPayload {
    public static final Type<PacketUpdateArmorExtraData> TYPE = new Type<>(RL("update_armor_extradata"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketUpdateArmorExtraData> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(EquipmentSlot.class), PacketUpdateArmorExtraData::slot,
            ResourceLocation.STREAM_CODEC, PacketUpdateArmorExtraData::upgradeID,
            DataComponentPatch.STREAM_CODEC, PacketUpdateArmorExtraData::patch,
            PacketUpdateArmorExtraData::new
    );

    public static <T> void sendToServer(IArmorUpgradeHandler<?> handler, DataComponentType<T> type, T data) {
        DataComponentPatch patch = DataComponentPatch.builder().set(type, data).build();
        NetworkHandler.sendToServer(new PacketUpdateArmorExtraData(handler.getEquipmentSlot(), handler.getID(), patch));
    }

    private static final List<Set<DataComponentType<?>>> VALID_KEYS = new ArrayList<>();

    private static void addKey(EquipmentSlot slot, DataComponentType<?> type) {
        VALID_KEYS.get(slot.getIndex()).add(type);
    }

    static {
        for (EquipmentSlot ignored : ArmorUpgradeRegistry.ARMOR_SLOTS) {
            VALID_KEYS.add(new HashSet<>());
        }
        addKey(EquipmentSlot.HEAD, ModDataComponents.ENTITY_FILTER.get());
        addKey(EquipmentSlot.HEAD, ModDataComponents.COORD_TRACKER.get());
        addKey(EquipmentSlot.LEGS, ModDataComponents.SPEED_BOOST_PCT.get());
        addKey(EquipmentSlot.LEGS, ModDataComponents.JET_BOOTS_PCT.get());
        addKey(EquipmentSlot.FEET, ModDataComponents.JET_BOOTS_STABILIZERS.get());
        addKey(EquipmentSlot.FEET, ModDataComponents.JET_BOOTS_BUILDER_MODE.get());
        addKey(EquipmentSlot.FEET, ModDataComponents.JET_BOOTS_HOVER.get());
        addKey(EquipmentSlot.FEET, ModDataComponents.JET_BOOTS_SMART_HOVER.get());
    }

    @Override
    public Type<PacketUpdateArmorExtraData> type() {
        return TYPE;
    }

    public static void handle(PacketUpdateArmorExtraData message, IPayloadContext ctx) {
        ItemStack stack = ctx.player().getItemBySlot(message.slot());
        if (stack.getItem() instanceof PneumaticArmorItem) {
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(ctx.player());

            if (stack.getComponents() instanceof PatchedDataComponentMap pdcm) {
                message.patch.entrySet().forEach(entry -> {
                    if (isTypeOKForSlot(message.slot, entry.getKey())) {
                        pdcm.applyPatch(message.patch);
                        IArmorUpgradeHandler<?> upgradeHandler = ArmorUpgradeRegistry.getInstance().getUpgradeEntry(message.upgradeID());
                        if (upgradeHandler != null) {
                            upgradeHandler.onDataFieldUpdated(handler, entry.getKey(), entry.getValue());
                        }
                    }
                });
            }
        }
    }

    private static boolean isTypeOKForSlot(EquipmentSlot slot, DataComponentType<?> type) {
        return VALID_KEYS.get(slot.getIndex()).contains(type);
    }
}
