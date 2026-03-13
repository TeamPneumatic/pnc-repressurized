package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.misc.ITranslatableEnum;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public enum ChargeMode implements StringRepresentable, ITranslatableEnum {
    NONE("none", false, false),
    ARMOR("armor", true, false),
    HELD("held", false, true),
    HELD_AND_ARMOR("held_armor", true, true),
    ALL("all", true, true);

    private final String name;
    private final boolean chargeArmor;
    private final boolean chargeHeld;

    ChargeMode(String name, boolean chargeArmor, boolean chargeHeld) {
        this.name = name;
        this.chargeArmor = chargeArmor;
        this.chargeHeld = chargeHeld;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    @Override
    public String getTranslationKey() {
        return "pneumaticcraft.gui.air_canister.charging." + name;
    }

    public ChargeMode nextMode() {
        return values()[(ordinal() + 1) % values().length];
    }

    public boolean shouldChargeArmor() {
        return chargeArmor;
    }

    public boolean shouldChargeHeldItem() {
        return chargeHeld;
    }

    public List<ItemStack> getStacksToCharge(Player player) {
        List<ItemStack> toCharge = new ArrayList<>();
        if (shouldChargeArmor()) {
            toCharge.add(player.getItemBySlot(EquipmentSlot.HEAD));
            toCharge.add(player.getItemBySlot(EquipmentSlot.LEGS));
            toCharge.add(player.getItemBySlot(EquipmentSlot.FEET));
        }
        if (shouldChargeHeldItem()) {
            toCharge.add(player.getItemBySlot(EquipmentSlot.MAINHAND));
            toCharge.add(player.getItemBySlot(EquipmentSlot.OFFHAND));
        }
        if (this == ChargeMode.ALL) {
            toCharge.addAll(player.getInventory().items);
        }
        return toCharge;
    }
}
