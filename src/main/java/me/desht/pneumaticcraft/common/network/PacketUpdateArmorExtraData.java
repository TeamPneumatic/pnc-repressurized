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
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.*;
import java.util.function.Supplier;

/**
 * Received on: SERVER
 * General packet for updating various pneumatic armor settings from client GUI
 */
public class PacketUpdateArmorExtraData {
    private static final List<Map<String, Integer>> VALID_KEYS = new ArrayList<>();
    private final ResourceLocation upgradeID;

    private static void addKey(EquipmentSlot slot, String key, int nbtType) {
        VALID_KEYS.get(slot.getIndex()).put(key, nbtType);
    }

    static {
        Arrays.stream(ArmorUpgradeRegistry.ARMOR_SLOTS)
                .<Map<String, Integer>>map(slot -> new HashMap<>())
                .forEach(VALID_KEYS::add);
        addKey(EquipmentSlot.HEAD, ItemPneumaticArmor.NBT_ENTITY_FILTER, Tag.TAG_STRING);
        addKey(EquipmentSlot.HEAD, ItemPneumaticArmor.NBT_COORD_TRACKER, Tag.TAG_COMPOUND);
        addKey(EquipmentSlot.LEGS, ItemPneumaticArmor.NBT_SPEED_BOOST, Tag.TAG_INT);
        addKey(EquipmentSlot.LEGS, ItemPneumaticArmor.NBT_JUMP_BOOST, Tag.TAG_INT);
        addKey(EquipmentSlot.FEET, ItemPneumaticArmor.NBT_BUILDER_MODE, Tag.TAG_BYTE);
        addKey(EquipmentSlot.FEET, ItemPneumaticArmor.NBT_JET_BOOTS_POWER, Tag.TAG_INT);
        addKey(EquipmentSlot.FEET, ItemPneumaticArmor.NBT_FLIGHT_STABILIZERS, Tag.TAG_BYTE);
        addKey(EquipmentSlot.FEET, ItemPneumaticArmor.NBT_SMART_HOVER, Tag.TAG_BYTE);
    }

    private final EquipmentSlot slot;
    private final CompoundTag data;

    public PacketUpdateArmorExtraData(EquipmentSlot slot, CompoundTag data, ResourceLocation upgradeID) {
        this.slot = slot;
        this.data = data;
        this.upgradeID = upgradeID;
    }

    PacketUpdateArmorExtraData(FriendlyByteBuf buffer) {
        slot = EquipmentSlot.values()[buffer.readByte()];
        data = buffer.readNbt();
        upgradeID = buffer.readResourceLocation();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeByte(slot.ordinal());
        buf.writeNbt(data);
        buf.writeResourceLocation(upgradeID);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.getItem() instanceof ItemPneumaticArmor) {
                CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
                NBTUtils.initNBTTagCompound(stack);
                for (String key : data.getAllKeys()) {
                    Tag dataTag = data.get(key);
                    if (isKeyOKForSlot(key, slot, dataTag.getId())) {
                        stack.getTag().put(key, dataTag);
                        IArmorUpgradeHandler<?> upgradeHandler = ArmorUpgradeRegistry.getInstance().getUpgradeEntry(upgradeID);
                        if (upgradeHandler != null) {
                            upgradeHandler.onDataFieldUpdated(handler, key, dataTag);
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static boolean isKeyOKForSlot(String key, EquipmentSlot slot, int nbtType) {
        return VALID_KEYS.get(slot.getIndex()).get(key) == nbtType;
    }
}
