package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.client.render.pneumatic_armor.UpgradeRenderHandlerList;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.*;
import java.util.function.Supplier;

/**
 * Received on: SERVER
 * General packet for updating various pneumatic armor settings from client GUI
 */
public class PacketUpdateArmorExtraData {
    private static final List<Map<String, Integer>> VALID_KEYS = new ArrayList<>();

    private static void addKey(EquipmentSlotType slot, String key, int nbtType) {
        VALID_KEYS.get(slot.getIndex()).put(key, nbtType);
    }

    static {
        Arrays.stream(UpgradeRenderHandlerList.ARMOR_SLOTS)
                .<Map<String, Integer>>map(slot -> new HashMap<>())
                .forEach(VALID_KEYS::add);
        addKey(EquipmentSlotType.HEAD, ItemPneumaticArmor.NBT_ENTITY_FILTER, NBT.TAG_STRING);
        addKey(EquipmentSlotType.LEGS, ItemPneumaticArmor.NBT_SPEED_BOOST, NBT.TAG_INT);
        addKey(EquipmentSlotType.LEGS, ItemPneumaticArmor.NBT_JUMP_BOOST, NBT.TAG_INT);
        addKey(EquipmentSlotType.FEET, ItemPneumaticArmor.NBT_BUILDER_MODE, NBT.TAG_BYTE);
    }

    private EquipmentSlotType slot;
    private CompoundNBT data;

    public PacketUpdateArmorExtraData() {
        // empty
    }

    public PacketUpdateArmorExtraData(EquipmentSlotType slot, CompoundNBT data) {
        this.slot = slot;
        this.data = data;
    }

    PacketUpdateArmorExtraData(PacketBuffer buffer) {
        slot = EquipmentSlotType.values()[buffer.readByte()];
        data = buffer.readCompoundTag();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeByte(slot.ordinal());
        buf.writeCompoundTag(data);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            ItemStack stack = player.getItemStackFromSlot(slot);
            if (stack.getItem() instanceof ItemPneumaticArmor) {
                CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
                NBTUtil.initNBTTagCompound(stack);
                for (String key : data.keySet()) {
                    INBT dataTag = data.get(key);
                    if (isKeyOKForSlot(key, slot, dataTag.getId())) {
                        stack.getTag().put(key, dataTag);
                        handler.onDataFieldUpdated(slot, key, dataTag);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static boolean isKeyOKForSlot(String key, EquipmentSlotType slot, int nbtType) {
        return VALID_KEYS.get(slot.getIndex()).get(key) == nbtType;
    }
}
