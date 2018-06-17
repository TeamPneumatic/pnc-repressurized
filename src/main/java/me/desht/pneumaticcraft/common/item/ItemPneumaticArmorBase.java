package me.desht.pneumaticcraft.common.item;

import com.google.common.collect.Sets;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.UpgradeRenderHandlerList;
import me.desht.pneumaticcraft.common.NBTUtil;
import me.desht.pneumaticcraft.common.recipes.CraftingRegistrator;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.lib.ModIds;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import me.desht.pneumaticcraft.proxy.CommonProxy;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.Loader;
import thaumcraft.api.items.IVisDiscountGear;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class ItemPneumaticArmorBase extends ItemArmor implements IPressurizable, IChargingStationGUIHolderItem, IUpgradeAcceptor,
        IVisDiscountGear
{
    private static final ArmorMaterial COMPRESSED_IRON_MATERIAL = EnumHelper.addArmorMaterial(
            "compressedIron", "compressedIron",
            PneumaticValues.PNEUMATIC_ARMOR_DURABILITY_BASE,
            new int[]{2, 5, 6, 2}, 9,
            SoundEvents.ITEM_ARMOR_EQUIP_IRON, 1.0F
    );

    public ItemPneumaticArmorBase(String name, EntityEquipmentSlot equipmentSlotIn) {
        super(COMPRESSED_IRON_MATERIAL, PneumaticCraftRepressurized.proxy.getArmorRenderID(Textures.ARMOR_PNEUMATIC), equipmentSlotIn);

        setRegistryName(name);
        setUnlocalizedName(name);

//        setMaxDamage(PneumaticValues.PNEUMATIC_ARMOR_DURABILITY_BASE * MAX_DAMAGE_ARRAY[armorType.getIndex()]);
        setCreativeTab(PneumaticCraftRepressurized.tabPneumaticCraft);
    }

    /**
     * Check if the player is wearing any pneumatic armor piece.
     *
     * @param player the player
     * @return true if the player is wearing pneumatic armor
     */
    public static boolean isPlayerWearingAnyPneumaticArmor(EntityPlayer player) {
        return player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() instanceof ItemPneumaticArmorBase
                || player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() instanceof ItemPneumaticArmorBase
                || player.getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem() instanceof ItemPneumaticArmorBase
                || player.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem() instanceof ItemPneumaticArmorBase;
    }

    public abstract int getVolume();

    public abstract int getMaxAir();

    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return slot == EntityEquipmentSlot.LEGS ? Textures.ARMOR_PNEUMATIC + "_2.png" : Textures.ARMOR_PNEUMATIC + "_1.png";
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        float pressure = getPressure(stack);
        tooltip.add((pressure < 0.5F ? TextFormatting.RED : TextFormatting.DARK_GREEN) + "Pressure: " + Math.round(pressure * 10D) / 10D + " bar");
        UpgradableItemUtils.addUpgradeInformation(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack) {
        return false;
    }

    @Override
    public Set<Item> getApplicableUpgrades() {
        Set<Item> upgrades = Sets.newHashSet();

        for (IUpgradeRenderHandler handler : UpgradeRenderHandlerList.instance().getHandlersForSlot(armorType)) {
            Collections.addAll(upgrades, handler.getRequiredUpgrades());
        }

        upgrades.add(CraftingRegistrator.getUpgrade(EnumUpgrade.VOLUME).getItem());
        upgrades.add(CraftingRegistrator.getUpgrade(EnumUpgrade.ITEM_LIFE).getItem());
        if (Loader.isModLoaded(ModIds.THAUMCRAFT)) {
            upgrades.add(CraftingRegistrator.getUpgrade(EnumUpgrade.THAUMCRAFT).getItem());
        }

        return upgrades;
    }

    @Override
    public float getPressure(ItemStack iStack) {
        int volume = UpgradableItemUtils.getUpgrades(EnumUpgrade.VOLUME, iStack) * PneumaticValues.VOLUME_VOLUME_UPGRADE + getVolume();
        int oldVolume = NBTUtil.getInteger(iStack, "volume");
        int currentAir = NBTUtil.getInteger(iStack, "air");
        if (volume < oldVolume) {
            currentAir = currentAir * volume / oldVolume;
            NBTUtil.setInteger(iStack, "air", currentAir);
        }
        if (volume != oldVolume) {
            NBTUtil.setInteger(iStack, "volume", volume);
        }
        return (float) currentAir / volume;
    }


    public boolean hasSufficientPressure(ItemStack iStack) {
        return getPressure(iStack) > 0F;
    }

    @Override
    public float maxPressure(ItemStack iStack) {
        return 10F;
    }

    @Override
    public void addAir(ItemStack iStack, int amount) {
        int volume = UpgradableItemUtils.getUpgrades(EnumUpgrade.VOLUME, iStack) * PneumaticValues.VOLUME_VOLUME_UPGRADE + getVolume();
        int maxAir = (int)(maxPressure(iStack) * volume);
        int oldAir = NBTUtil.getInteger(iStack, "air");
        NBTUtil.setInteger(iStack, "air", Math.min(maxAir, Math.max(oldAir + amount, 0)));
    }

    @Override
    public CommonProxy.EnumGuiId getGuiID() {
        // all armor pieces share the same GUI
        return CommonProxy.EnumGuiId.PNEUMATIC_ARMOR;
    }

    @Override
    public String getName() {
        return getUnlocalizedName() + ".name";
    }

    boolean hasThaumcraftUpgradeAndPressure(ItemStack stack) {
        return hasSufficientPressure(stack) && UpgradableItemUtils.getUpgrades(EnumUpgrade.THAUMCRAFT, stack) > 0;
    }

}
