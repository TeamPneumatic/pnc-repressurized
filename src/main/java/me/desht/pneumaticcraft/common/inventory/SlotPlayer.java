package me.desht.pneumaticcraft.common.inventory;

import com.mojang.datafixers.util.Pair;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class SlotPlayer extends Slot {
    private static final ResourceLocation[] ARMOR_SLOT_TEXTURES = new ResourceLocation[]{
            PlayerContainer.EMPTY_ARMOR_SLOT_BOOTS,
            PlayerContainer.EMPTY_ARMOR_SLOT_LEGGINGS,
            PlayerContainer.EMPTY_ARMOR_SLOT_CHESTPLATE,
            PlayerContainer.EMPTY_ARMOR_SLOT_HELMET
    };
    private final EquipmentSlotType slotType;

    public SlotPlayer(PlayerInventory inventoryIn, EquipmentSlotType slotType, int xPosition, int yPosition) {
        super(inventoryIn, getIndexForSlot(slotType), xPosition, yPosition);
        this.slotType = slotType;
    }

    @Override
    public int getSlotStackLimit() {
        return slotType == EquipmentSlotType.OFFHAND ? super.getSlotStackLimit() : 1;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return slotType == EquipmentSlotType.OFFHAND ? super.isItemValid(stack) : stack.canEquip(slotType, ((PlayerInventory) inventory).player);
    }

    @Override
    public boolean canTakeStack(PlayerEntity playerIn) {
        if (slotType == EquipmentSlotType.OFFHAND) return super.canTakeStack(playerIn);

        ItemStack itemstack = this.getStack();
        return (itemstack.isEmpty() || playerIn.isCreative() || !EnchantmentHelper.hasBindingCurse(itemstack)) && super.canTakeStack(playerIn);
    }

    @Override
    public Pair<ResourceLocation, ResourceLocation> getBackground() {
        return slotType.getSlotType() == EquipmentSlotType.Group.ARMOR ?
                Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, ARMOR_SLOT_TEXTURES[slotType.getIndex()]) :
                Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, PlayerContainer.EMPTY_ARMOR_SLOT_SHIELD);
    }

    private static int getIndexForSlot(EquipmentSlotType type) {
        if (type.getSlotType() == EquipmentSlotType.Group.ARMOR) {
            return 36 + type.getIndex();
        } else if (type == EquipmentSlotType.OFFHAND) {
            return 40;
        } else {
            throw new IllegalArgumentException("invalid equipment slot: " + type);
        }
    }
}
