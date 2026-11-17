package me.desht.pneumaticcraft.client.pneumatic_armor.entity_tracker;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IEntityTrackEntry;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class EntityTrackEntryPlayer implements IEntityTrackEntry {
    // same order as the pre-26.1 Inventory#armor list
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD
    };

    @Override
    public boolean isApplicable(Entity entity) {
        return entity instanceof Player;
    }

    @Override
    public void addInfo(Entity entity, List<Component> curInfo, boolean isLookingAtTarget) {
        Player player = (Player) entity;

        List<ItemStack> armor = new ArrayList<>();
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            armor.add(player.getItemBySlot(slot));
        }
        addInventory("pneumaticcraft.entityTracker.info.player.armor", curInfo, armor);
        addInventory("pneumaticcraft.entityTracker.info.player.holding", curInfo, player.getInventory().getNonEquipmentItems());
    }

    private static void addInventory(String key, List<Component> curInfo, List<ItemStack> stacks) {
        curInfo.add(xlate(key).withStyle(ChatFormatting.GRAY));
        List<Component> l = PneumaticCraftUtils.summariseItemStacks(new ArrayList<>(), stacks);
        if (l.isEmpty()) {
            curInfo.add(xlate("pneumaticcraft.gui.misc.no_items"));
        } else {
            curInfo.addAll(l);
        }
    }
}
