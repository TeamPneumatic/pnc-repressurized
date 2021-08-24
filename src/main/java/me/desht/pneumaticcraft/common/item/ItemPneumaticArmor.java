package me.desht.pneumaticcraft.common.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.client.IFOVModifierItem;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.ICustomDurabilityBar;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.api.lib.NBTKeys;
import me.desht.pneumaticcraft.api.pressure.IPressurizableItem;
import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.capabilities.AirHandlerItemStack;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.entity.EntityProgrammableController;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.entity.living.EntityDroneBase;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.recipes.special.OneProbeCrafting;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammableController;
import me.desht.pneumaticcraft.common.util.GlobalPosHelper;
import me.desht.pneumaticcraft.common.util.NBTUtils;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.common.util.upgrade.ApplicableUpgradesDB;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

//@Optional.InterfaceList({
//        @Optional.Interface(iface = "thaumcraft.api.items.IGoggles", modid = ModIds.THAUMCRAFT),
//        @Optional.Interface(iface = "thaumcraft.api.items.IVisDiscountGear", modid = ModIds.THAUMCRAFT),
//        @Optional.Interface(iface = "thaumcraft.api.items.IRevealer", modid = ModIds.THAUMCRAFT)
//})
public class ItemPneumaticArmor extends ArmorItem implements
        IChargeableContainerProvider, IUpgradeAcceptor, IFOVModifierItem, ICustomDurabilityBar, IPressurizableItem,
        IDyeableArmorItem, ColorHandlers.ITintableItem
        /*, IVisDiscountGear, IGoggles, IRevealer,*/
{
    private static final UUID[] PNEUMATIC_ARMOR_MODIFIERS = new UUID[] {
            UUID.fromString("4a6bf01d-2e83-4b13-aaf0-a4c05958ea3c"),
            UUID.fromString("ad78a169-0409-47fb-8ca2-126b19196b56"),
            UUID.fromString("87bf456d-7360-407d-8592-5a2583eb948c"),
            UUID.fromString("e836e6c9-355e-49f2-87fc-331fadfdd642")
    };

    private static final IArmorMaterial PNEUMATIC_ARMOR_MATERIAL = new CompressedIronArmorMaterial(0.2f);

    private static final int[] ARMOR_VOLUMES = new int[] {
            PneumaticValues.PNEUMATIC_BOOTS_VOLUME,
            PneumaticValues.PNEUMATIC_LEGGINGS_VOLUME,
            PneumaticValues.PNEUMATIC_CHESTPLATE_VOLUME,
            PneumaticValues.PNEUMATIC_HELMET_VOLUME
    };
//    private static final int[] VIS_DISCOUNTS = new int[] { 1, 2, 2, 5 };

    public static final String NBT_SEARCH_ITEM = "SearchStack";
    public static final String NBT_COORD_TRACKER = "CoordTracker";
    public static final String NBT_ENTITY_FILTER = "entityFilter";
    public static final String NBT_JUMP_BOOST = "jumpBoost";
    public static final String NBT_SPEED_BOOST = "speedBoost";
    public static final String NBT_BUILDER_MODE = "JetBootsBuilderMode";
    public static final String NBT_JET_BOOTS_POWER = "JetBootsPower";
    public static final String NBT_FLIGHT_STABILIZERS = "JetBootsStabilizers";
    public static final String NBT_SMART_HOVER = "SmartHover";
    public static final int DEFAULT_PRIMARY_COLOR = 0xFF969696;
    public static final int DEFAULT_SECONDARY_COLOR = 0xFFC0C0C0;
    public static final int DEFAULT_EYEPIECE_COLOR = 0xFF00AA00;

    public ItemPneumaticArmor(EquipmentSlotType equipmentSlotIn) {
        super(PNEUMATIC_ARMOR_MATERIAL, equipmentSlotIn, ModItems.defaultProps());
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return new AirHandlerItemStack(stack, 10F);
    }

    /**
     * Check if the player is wearing any pneumatic armor piece.
     *
     * @param player the player
     * @return true if the player is wearing pneumatic armor
     */
    public static boolean isPlayerWearingAnyPneumaticArmor(PlayerEntity player) {
        for (EquipmentSlotType slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
            if (isPneumaticArmorPiece(player, slot)) return true;
        }
        return false;
    }

    public static boolean isPneumaticArmorPiece(PlayerEntity player, EquipmentSlotType slot) {
        return player.getItemBySlot(slot).getItem() instanceof ItemPneumaticArmor;
    }

    @Override
    public int getBaseVolume() {
        return ARMOR_VOLUMES[slot.getIndex()];
    }

    @Override
    public int getVolumeUpgrades(ItemStack stack) {
        return UpgradableItemUtils.getUpgrades(stack, EnumUpgrade.VOLUME);
    }

    @Override
    public int getAir(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        return tag != null ? tag.getInt(AirHandlerItemStack.AIR_NBT_KEY) : 0;
    }

    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type) {
        String s = slot == EquipmentSlotType.LEGS ? Textures.ARMOR_PNEUMATIC + "_2" : Textures.ARMOR_PNEUMATIC + "_1";
        return type == null ? s + ".png" : s + "_" + type + ".png";
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        if (slot == EquipmentSlotType.HEAD && worldIn != null) {
            addHelmetInformation(stack, worldIn, tooltip, flagIn);
        }
    }

    private void addHelmetInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        if (OneProbeCrafting.isOneProbeEnabled(stack)) {
            tooltip.add(xlate("gui.tooltip.item.pneumaticcraft.pneumatic_helmet.one_probe").withStyle(TextFormatting.BLUE));
        }

        Item searchedItem = getSearchedItem(stack);
        if (searchedItem != null) {
            ItemStack searchStack = new ItemStack(searchedItem);
            if (!searchStack.isEmpty()) {
                tooltip.add(xlate("pneumaticcraft.armor.upgrade.search").append(": ").append(searchStack.getHoverName()).withStyle(TextFormatting.YELLOW));
            }
        }

        BlockPos pos = getCoordTrackerPos(stack, worldIn);
        if (pos != null) {
            tooltip.add(xlate("pneumaticcraft.armor.upgrade.coordinate_tracker")
                    .append(": ").append(PneumaticCraftUtils.posToString(pos)).withStyle(TextFormatting.YELLOW));
        }
    }

    @Override
    public boolean isValidRepairItem(ItemStack par1ItemStack, ItemStack par2ItemStack) {
        return false;
    }

    @Override
    public Map<EnumUpgrade,Integer> getApplicableUpgrades() {
        return ApplicableUpgradesDB.getInstance().getApplicableUpgrades(this);
    }

    @Override
    public String getUpgradeAcceptorTranslationKey() {
        return getDescriptionId();
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return PNCConfig.Client.Armor.showEnchantGlint && super.isFoil(stack);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> multimap = HashMultimap.create(super.getAttributeModifiers(equipmentSlot, stack));

        // in future?
//        Multimap<Attribute, AttributeModifier> m = MultimapBuilder.hashKeys().hashSetValues()
//                .build(super.getAttributeModifiers(equipmentSlot, stack));

        if (equipmentSlot == this.slot) {
            int upgrades = UpgradableItemUtils.getUpgrades(stack, EnumUpgrade.ARMOR);
            multimap.put(Attributes.ARMOR, new AttributeModifier(PNEUMATIC_ARMOR_MODIFIERS[equipmentSlot.getIndex()], "Pneumatic Armor modifier boost", (double) upgrades / 2d, AttributeModifier.Operation.ADDITION));
            multimap.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(PNEUMATIC_ARMOR_MODIFIERS[equipmentSlot.getIndex()], "Pneumatic Armor toughness boost", upgrades, AttributeModifier.Operation.ADDITION));
        }

        return multimap;
    }

    @Nullable
    @Override
    public CompoundNBT getShareTag(ItemStack stack) {
        return ItemPressurizable.roundedPressure(stack);
    }

    /* ----------- Pneumatic Helmet helpers ---------- */

    public static int getIntData(ItemStack stack, String key, int def) {
        if (stack.getItem() instanceof ItemPneumaticArmor && stack.hasTag() && stack.getTag().contains(key, Constants.NBT.TAG_INT)) {
            return stack.getTag().getInt(key);
        } else {
            return def;
        }
    }

    public static int getIntData(ItemStack stack, String key, int def, int min, int max) {
        if (stack.getItem() instanceof ItemPneumaticArmor && stack.hasTag() && stack.getTag().contains(key, Constants.NBT.TAG_INT)) {
            return MathHelper.clamp(stack.getTag().getInt(key), min, max);
        } else {
            return def;
        }
    }

    public static boolean getBooleanData(ItemStack stack, String key, boolean def) {
        if (stack.getItem() instanceof ItemPneumaticArmor && stack.hasTag() && stack.getTag().contains(key, Constants.NBT.TAG_BYTE)) {
            return stack.getTag().getByte(key) == 1;
        } else {
            return def;
        }
    }

    public static Item getSearchedItem(ItemStack helmetStack) {
        if (helmetStack.isEmpty() || !NBTUtils.hasTag(helmetStack, NBT_SEARCH_ITEM)) return null;
        String itemName = NBTUtils.getString(helmetStack, NBT_SEARCH_ITEM);
        return itemName == null || itemName.isEmpty() ? null : ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
    }

    public static void setSearchedItem(ItemStack helmetStack, Item searchedItem) {
        if (helmetStack.getItem() instanceof ItemPneumaticArmor) {
            NBTUtils.setString(helmetStack, NBT_SEARCH_ITEM, searchedItem.getRegistryName().toString());
        }
    }

    public static BlockPos getCoordTrackerPos(ItemStack helmetStack, World world) {
        if (helmetStack.isEmpty() || !NBTUtils.hasTag(helmetStack, NBT_COORD_TRACKER)) return null;
        CompoundNBT tag = NBTUtils.getCompoundTag(helmetStack, NBT_COORD_TRACKER);
        GlobalPos gPos = GlobalPosHelper.fromNBT(tag);
        if (gPos.pos().getY() < 0 || !GlobalPosHelper.isSameWorld(gPos, world)) {
            return null;
        }
        return gPos.pos();
    }

    public static void setCoordTrackerPos(ItemStack helmetStack, GlobalPos gPos) {
        NBTUtils.setCompoundTag(helmetStack, ItemPneumaticArmor.NBT_COORD_TRACKER, GlobalPosHelper.toNBT(gPos));
    }

    public static String getEntityFilter(ItemStack helmetStack) {
        if (helmetStack.isEmpty() || !NBTUtils.hasTag(helmetStack, NBT_ENTITY_FILTER)) return "";
        return NBTUtils.getString(helmetStack, NBT_ENTITY_FILTER);
    }

    public static boolean isPlayerDebuggingDrone(PlayerEntity player, EntityDroneBase e) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlotType.HEAD);
        if (helmet.getItem() != ModItems.PNEUMATIC_HELMET.get()) return false;
        if (e instanceof EntityDrone) {
            return NBTUtils.getInteger(helmet, NBTKeys.PNEUMATIC_HELMET_DEBUGGING_DRONE) == e.getId();
        } else if (e instanceof EntityProgrammableController) {
            CompoundNBT tag = helmet.getTagElement(NBTKeys.PNEUMATIC_HELMET_DEBUGGING_PC);
            return tag != null && NBTUtil.readBlockPos(tag).equals(((EntityProgrammableController) e).getControllerPos());
        } else {
            return false;
        }
    }

    public static boolean isPlayerDebuggingDrone(PlayerEntity player, IDroneBase e) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlotType.HEAD);
        if (helmet.getItem() != ModItems.PNEUMATIC_HELMET.get()) return false;
        if (e instanceof EntityDrone) {
            return NBTUtils.getInteger(helmet, NBTKeys.PNEUMATIC_HELMET_DEBUGGING_DRONE) == ((EntityDrone)e).getId();
        } else if (e instanceof TileEntityProgrammableController) {
            CompoundNBT tag = helmet.getTagElement(NBTKeys.PNEUMATIC_HELMET_DEBUGGING_PC);
            return tag != null && NBTUtil.readBlockPos(tag).equals(((TileEntityProgrammableController) e).getBlockPos());
        } else {
            return false;
        }
    }

    /**
     * Client-side method to get the debugged drone or programmable controller
     * @return the debugged whatever
     */
    public static IDroneBase getDebuggedDrone() {
        ItemStack helmet = ClientUtils.getClientPlayer().getItemBySlot(EquipmentSlotType.HEAD);
        if (helmet.getItem() == ModItems.PNEUMATIC_HELMET.get() && helmet.hasTag()) {
            CompoundNBT tag = helmet.getTag();
            if (tag.contains(NBTKeys.PNEUMATIC_HELMET_DEBUGGING_DRONE)) {
                int id = tag.getInt(NBTKeys.PNEUMATIC_HELMET_DEBUGGING_DRONE);
                if (id > 0) {
                    Entity e = ClientUtils.getClientWorld().getEntity(id);
                    if (e instanceof IDroneBase) return (IDroneBase) e;
                }
            }
            if (tag.contains(NBTKeys.PNEUMATIC_HELMET_DEBUGGING_PC)) {
                TileEntity te = ClientUtils.getClientWorld().getBlockEntity(NBTUtil.readBlockPos(tag.getCompound(NBTKeys.PNEUMATIC_HELMET_DEBUGGING_PC)));
                if (te instanceof IDroneBase) {
                    return (IDroneBase) te;
                }
            }
        }
        return null;
    }

    @Override
    public float getFOVModifier(ItemStack stack, PlayerEntity player, EquipmentSlotType slot) {
        if (slot == EquipmentSlotType.LEGS && PNCConfig.Client.Armor.leggingsFOVFactor > 0) {
            double boost = ArmorUpgradeRegistry.getInstance().runSpeedHandler.getSpeedBoostFromLegs(CommonArmorHandler.getHandlerForPlayer());
            if (boost > 0) {
                return 1.0f + (float) (boost * 2.0 * PNCConfig.Client.Armor.leggingsFOVFactor);
            }
        }
        return 1.0f;
    }

    @Override
    public INamedContainerProvider getContainerProvider(TileEntityChargingStation te) {
        return new IChargeableContainerProvider.Provider(te, ModContainers.CHARGING_ARMOR.get());
    }

    @Override
    public boolean shouldShowCustomDurabilityBar(ItemStack stack) {
        return ItemPressurizable.shouldShowPressureDurability(stack);
    }

    @Override
    public int getCustomDurabilityColour(ItemStack stack) {
        return ItemPressurizable.getPressureDurabilityColor(stack);
    }

    @Override
    public float getCustomDurability(ItemStack stack) {
        return stack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY)
                .map(h -> h.getPressure() / h.maxPressure())
                .orElseThrow(RuntimeException::new);
    }

    @Override
    public int getTintColor(ItemStack stack, int tintIndex) {
        switch (tintIndex) {
            case 0: return getColor(stack);
            case 1: return getSecondaryColor(stack);
            case 2: return stack.getItem() == ModItems.PNEUMATIC_HELMET.get() ? getEyepieceColor(stack) : 0xFFFFFFFF;
            default: return 0xFFFFFFFF;
        }
    }

    @Override
    public int getColor(ItemStack stack) {
        // default IDyeableArmor gives undyed items a leather-brown colour... override for compressed-iron-grey
        CompoundNBT nbt = stack.getTagElement("display");
        return nbt != null && nbt.contains("color", Constants.NBT.TAG_ANY_NUMERIC) ? nbt.getInt("color") : DEFAULT_PRIMARY_COLOR;
    }

    /**
     * Get the overlay colour
     * @param stack the armor piece
     * @return the overlay colour in ARGB format
     */
    public int getSecondaryColor(ItemStack stack) {
        CompoundNBT nbt = stack.getTagElement("display");
        return nbt != null && nbt.contains("color2", Constants.NBT.TAG_ANY_NUMERIC) ? nbt.getInt("color2") : DEFAULT_SECONDARY_COLOR;
    }

    public void setSecondaryColor(ItemStack stack, int color) {
        stack.getOrCreateTagElement("display").putInt("color2", color);
    }

    public int getEyepieceColor(ItemStack stack) {
        CompoundNBT nbt = stack.getTagElement("display");
        return nbt != null && nbt.contains("color_eye", Constants.NBT.TAG_ANY_NUMERIC) ? nbt.getInt("color_eye") : DEFAULT_EYEPIECE_COLOR;
    }

    public void setEyepieceColor(ItemStack stack, int color) {
        stack.getOrCreateTagElement("display").putInt("color_eye", color);
    }

    /*------- Thaumcraft -------- */

//    private boolean hasThaumcraftUpgradeAndPressure(ItemStack stack) {
//        return getPressure(stack) > 0F && UpgradableItemUtils.getUpgrades(EnumUpgrade.THAUMCRAFT, stack) > 0;
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.THAUMCRAFT)
//    public int getVisDiscount(ItemStack stack, PlayerEntity player) {
//        return hasThaumcraftUpgradeAndPressure(stack) ? VIS_DISCOUNTS[armorType.getIndex()] : 0;
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.THAUMCRAFT)
//    public boolean showIngamePopups(ItemStack itemstack, LivingEntity player) {
//        return armorType == EquipmentSlotType.HEAD && hasThaumcraftUpgradeAndPressure(itemstack);
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.THAUMCRAFT)
//    public boolean showNodes(ItemStack itemstack, LivingEntity player) {
//        return armorType == EquipmentSlotType.HEAD && hasThaumcraftUpgradeAndPressure(itemstack);
//    }

}
