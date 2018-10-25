package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.CommonHUDHandler;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.*;

public class PacketUpdateArmorExtraData extends AbstractPacket<PacketUpdateArmorExtraData> {
    private static final List<Map<String, Integer>> VALID_KEYS = new ArrayList<>();
    private static void addKey(EntityEquipmentSlot slot, String key, int nbtType) {
        VALID_KEYS.get(slot.getIndex()).put(key, nbtType);
    }
    static {
        Arrays.stream(EntityEquipmentSlot.values())
                .filter(slot -> slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR)
                .<Map<String, Integer>>map(slot -> new HashMap<>())
                .forEach(VALID_KEYS::add);
        addKey(EntityEquipmentSlot.HEAD, "entityFilter", NBT.TAG_STRING);
        addKey(EntityEquipmentSlot.LEGS, "speedBoost", NBT.TAG_INT);
        addKey(EntityEquipmentSlot.LEGS, "jumpBoost", NBT.TAG_INT);

    }

    private EntityEquipmentSlot slot;
    private NBTTagCompound data;

    public PacketUpdateArmorExtraData() {
    }

    public PacketUpdateArmorExtraData(EntityEquipmentSlot slot, NBTTagCompound data) {
        this.slot = slot;
        this.data = data;
    }

    @Override
    public void handleClientSide(PacketUpdateArmorExtraData message, EntityPlayer player) {
    }

    @Override
    public void handleServerSide(PacketUpdateArmorExtraData message, EntityPlayer player) {
        ItemStack stack = player.getItemStackFromSlot(message.slot);
        if (stack.getItem() instanceof ItemPneumaticArmor) {
            CommonHUDHandler handler = CommonHUDHandler.getHandlerForPlayer(player);
            NBTUtil.initNBTTagCompound(stack);
            for (String key : message.data.getKeySet()) {
                NBTBase dataTag = message.data.getTag(key);
                if (isKeyOKForSlot(key, message.slot, dataTag.getId())) {
                    stack.getTagCompound().setTag(key, dataTag);
                    handler.onDataFieldUpdated(message.slot, key, dataTag);
                }
            }
        }
    }

    private static boolean isKeyOKForSlot(String key, EntityEquipmentSlot slot, int nbtType) {
        return VALID_KEYS.get(slot.getIndex()).get(key) == nbtType;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        slot = EntityEquipmentSlot.values()[buf.readByte()];
        data = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(slot.ordinal());
        ByteBufUtils.writeTag(buf, data);
    }
}
