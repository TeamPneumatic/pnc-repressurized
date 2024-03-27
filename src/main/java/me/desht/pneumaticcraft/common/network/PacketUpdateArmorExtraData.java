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
import me.desht.pneumaticcraft.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.*;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * General packet for updating various pneumatic armor settings from client GUI
 */
public record PacketUpdateArmorExtraData(EquipmentSlot slot, ResourceLocation upgradeID, CompoundTag data) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("update_armor_extradata");
    private static final List<Map<String, Integer>> VALID_KEYS = new ArrayList<>();

    private static void addKey(EquipmentSlot slot, String key, int nbtType) {
        VALID_KEYS.get(slot.getIndex()).put(key, nbtType);
    }

    static {
        Arrays.stream(ArmorUpgradeRegistry.ARMOR_SLOTS)
                .<Map<String, Integer>>map(slot -> new HashMap<>())
                .forEach(VALID_KEYS::add);
        addKey(EquipmentSlot.HEAD, PneumaticArmorItem.NBT_ENTITY_FILTER, Tag.TAG_STRING);
        addKey(EquipmentSlot.HEAD, PneumaticArmorItem.NBT_COORD_TRACKER, Tag.TAG_COMPOUND);
        addKey(EquipmentSlot.LEGS, PneumaticArmorItem.NBT_SPEED_BOOST, Tag.TAG_INT);
        addKey(EquipmentSlot.LEGS, PneumaticArmorItem.NBT_JUMP_BOOST, Tag.TAG_INT);
        addKey(EquipmentSlot.FEET, PneumaticArmorItem.NBT_BUILDER_MODE, Tag.TAG_BYTE);
        addKey(EquipmentSlot.FEET, PneumaticArmorItem.NBT_JET_BOOTS_POWER, Tag.TAG_INT);
        addKey(EquipmentSlot.FEET, PneumaticArmorItem.NBT_FLIGHT_STABILIZERS, Tag.TAG_BYTE);
        addKey(EquipmentSlot.FEET, PneumaticArmorItem.NBT_HOVER, Tag.TAG_BYTE);
        addKey(EquipmentSlot.FEET, PneumaticArmorItem.NBT_SMART_HOVER, Tag.TAG_BYTE);
    }

    public static PacketUpdateArmorExtraData fromNetwork(FriendlyByteBuf buffer) {
        return new PacketUpdateArmorExtraData(buffer.readEnum(EquipmentSlot.class), buffer.readResourceLocation(), buffer.readNbt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeEnum(slot);
        buf.writeResourceLocation(upgradeID);
        buf.writeNbt(data);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketUpdateArmorExtraData message, PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
            ItemStack stack = player.getItemBySlot(message.slot());
            if (stack.getItem() instanceof PneumaticArmorItem) {
                CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
                NBTUtils.initNBTTagCompound(stack);
                for (String key : message.data().getAllKeys()) {
                    Tag dataTag = message.data().get(key);
                    if (isKeyOKForSlot(key, message.slot(), Objects.requireNonNull(dataTag).getId())) {
                        Objects.requireNonNull(stack.getTag()).put(key, dataTag);
                        IArmorUpgradeHandler<?> upgradeHandler = ArmorUpgradeRegistry.getInstance().getUpgradeEntry(message.upgradeID());
                        if (upgradeHandler != null) {
                            upgradeHandler.onDataFieldUpdated(handler, key, dataTag);
                        }
                    }
                }
            }
        }));
    }

    private static boolean isKeyOKForSlot(String key, EquipmentSlot slot, int nbtType) {
        return VALID_KEYS.get(slot.getIndex()).get(key) == nbtType;
    }
}
