package me.desht.pneumaticcraft.common.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.client.IFOVModifierItem;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.ICustomDurabilityBar;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.PneumaticCraftTags;
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
import me.desht.pneumaticcraft.lib.NBTKeys;
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
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
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
public class ItemPneumaticArmor extends ArmorItem
        implements IChargeableContainerProvider, IUpgradeAcceptor, IFOVModifierItem, ICustomDurabilityBar, IPressurizableItem
        /*, IVisDiscountGear, IGoggles, IRevealer,*/
{
    private static final UUID[] PNEUMATIC_ARMOR_MODIFIERS = new UUID[] {
            UUID.fromString("4a6bf01d-2e83-4b13-aaf0-a4c05958ea3c"),
            UUID.fromString("ad78a169-0409-47fb-8ca2-126b19196b56"),
            UUID.fromString("87bf456d-7360-407d-8592-5a2583eb948c"),
            UUID.fromString("e836e6c9-355e-49f2-87fc-331fadfdd642")
    };

    private static final IArmorMaterial COMPRESSED_IRON_MATERIAL = new CompressedArmorMaterial();

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
    private static final String NBT_SYNCED_AIR = "SyncedAir";

    public ItemPneumaticArmor(EquipmentSlotType equipmentSlotIn) {
        super(COMPRESSED_IRON_MATERIAL, equipmentSlotIn, ModItems.defaultProps());
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        if (stack.getItem() instanceof ItemPneumaticArmor) {
            return new AirHandlerItemStack(stack, ARMOR_VOLUMES[getEquipmentSlot().getIndex()], 10F);
        } else {
            return super.initCapabilities(stack, nbt);
        }
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
        return player.getItemStackFromSlot(slot).getItem() instanceof ItemPneumaticArmor;
    }

    @Override
    public int getBaseVolume() {
        return ARMOR_VOLUMES[slot.getIndex()];
    }

    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type) {
        return slot == EquipmentSlotType.LEGS ? Textures.ARMOR_PNEUMATIC + "_2.png" : Textures.ARMOR_PNEUMATIC + "_1.png";
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        if (slot == EquipmentSlotType.HEAD && worldIn != null) {
            addHelmetInformation(stack, worldIn, tooltip, flagIn);
        }
    }

    private void addHelmetInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        if (OneProbeCrafting.isOneProbeEnabled(stack)) {
            tooltip.add(new StringTextComponent("The One Probe installed").mergeStyle(TextFormatting.BLUE));
        }

        Item searchedItem = getSearchedItem(stack);
        if (searchedItem != null) {
            ItemStack searchStack = new ItemStack(searchedItem);
            if (!searchStack.isEmpty()) {
                tooltip.add(xlate("pneumaticcraft.armor.upgrade.search").appendString(": ").append(searchStack.getDisplayName()).mergeStyle(TextFormatting.YELLOW));
            }
        }

        BlockPos pos = getCoordTrackerPos(stack, worldIn);
        if (pos != null) {
            tooltip.add(xlate("pneumaticcraft.armor.upgrade.coordinate_tracker")
                    .appendString(": ").appendString(PneumaticCraftUtils.posToString(pos)).mergeStyle(TextFormatting.YELLOW));
        }
    }

    @Override
    public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack) {
        return false;
    }

    @Override
    public Map<EnumUpgrade,Integer> getApplicableUpgrades() {
        return ApplicableUpgradesDB.getInstance().getApplicableUpgrades(this);
    }

    @Override
    public String getUpgradeAcceptorTranslationKey() {
        return getTranslationKey();
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
        if (gPos.getPos().getY() < 0 || !GlobalPosHelper.isSameWorld(gPos, world)) {
            return null;
        }
        return gPos.getPos();
    }

    public static void setCoordTrackerPos(ItemStack helmetStack, GlobalPos gPos) {
        NBTUtils.setCompoundTag(helmetStack, ItemPneumaticArmor.NBT_COORD_TRACKER, GlobalPosHelper.toNBT(gPos));
    }

    public static String getEntityFilter(ItemStack helmetStack) {
        if (helmetStack.isEmpty() || !NBTUtils.hasTag(helmetStack, NBT_ENTITY_FILTER)) return "";
        return NBTUtils.getString(helmetStack, NBT_ENTITY_FILTER);
    }

    public static boolean isPlayerDebuggingDrone(PlayerEntity player, EntityDroneBase e) {
        ItemStack helmet = player.getItemStackFromSlot(EquipmentSlotType.HEAD);
        if (helmet.getItem() != ModItems.PNEUMATIC_HELMET.get()) return false;
        if (e instanceof EntityDrone) {
            return NBTUtils.getInteger(helmet, NBTKeys.PNEUMATIC_HELMET_DEBUGGING_DRONE) == e.getEntityId();
        } else if (e instanceof EntityProgrammableController) {
            CompoundNBT tag = helmet.getChildTag(NBTKeys.PNEUMATIC_HELMET_DEBUGGING_PC);
            return tag != null && NBTUtil.readBlockPos(tag).equals(((EntityProgrammableController) e).getControllerPos());
        } else {
            return false;
        }
    }

    public static boolean isPlayerDebuggingDrone(PlayerEntity player, IDroneBase e) {
        ItemStack helmet = player.getItemStackFromSlot(EquipmentSlotType.HEAD);
        if (helmet.getItem() != ModItems.PNEUMATIC_HELMET.get()) return false;
        if (e instanceof EntityDrone) {
            return NBTUtils.getInteger(helmet, NBTKeys.PNEUMATIC_HELMET_DEBUGGING_DRONE) == ((EntityDrone)e).getEntityId();
        } else if (e instanceof TileEntityProgrammableController) {
            CompoundNBT tag = helmet.getChildTag(NBTKeys.PNEUMATIC_HELMET_DEBUGGING_PC);
            return tag != null && NBTUtil.readBlockPos(tag).equals(((TileEntityProgrammableController) e).getPos());
        } else {
            return false;
        }
    }

    /**
     * Client-side method to get the debugged drone or programmable controller
     * @return the debugged whatever
     */
    public static IDroneBase getDebuggedDrone() {
        ItemStack helmet = ClientUtils.getClientPlayer().getItemStackFromSlot(EquipmentSlotType.HEAD);
        if (helmet.getItem() == ModItems.PNEUMATIC_HELMET.get() && helmet.hasTag()) {
            CompoundNBT tag = helmet.getTag();
            if (tag.contains(NBTKeys.PNEUMATIC_HELMET_DEBUGGING_DRONE)) {
                int id = tag.getInt(NBTKeys.PNEUMATIC_HELMET_DEBUGGING_DRONE);
                if (id > 0) {
                    Entity e = ClientUtils.getClientWorld().getEntityByID(id);
                    if (e instanceof IDroneBase) return (IDroneBase) e;
                }
            }
            if (tag.contains(NBTKeys.PNEUMATIC_HELMET_DEBUGGING_PC)) {
                TileEntity te = ClientUtils.getClientWorld().getTileEntity(NBTUtil.readBlockPos(tag.getCompound(NBTKeys.PNEUMATIC_HELMET_DEBUGGING_PC)));
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
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer();
            double boost = handler.getSpeedBoostFromLegs();
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

    private static class CompressedArmorMaterial implements IArmorMaterial {
        static final int[] DMG_REDUCTION = new int[]{2, 5, 6, 2};
        private static final int[] MAX_DAMAGE_ARRAY = new int[]{13, 15, 16, 11};

        @Override
        public int getDurability(EquipmentSlotType equipmentSlotType) {
            return PneumaticValues.PNEUMATIC_ARMOR_DURABILITY_BASE * MAX_DAMAGE_ARRAY[equipmentSlotType.getIndex()];
        }

        @Override
        public int getDamageReductionAmount(EquipmentSlotType equipmentSlotType) {
            return DMG_REDUCTION[equipmentSlotType.getIndex()];
        }

        @Override
        public int getEnchantability() {
            return 9;
        }

        @Override
        public SoundEvent getSoundEvent() {
            return SoundEvents.ITEM_ARMOR_EQUIP_IRON;
        }

        @Override
        public Ingredient getRepairMaterial() {
            return Ingredient.fromTag(PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON);
        }

        @Override
        public String getName() {
            return "compressed_iron";
        }

        @Override
        public float getToughness() {
            return 1.0f;
        }

        @Override
        public float getKnockbackResistance() {
            // TODO: upgrades to improve knockback resistance ?
            return 0;
        }
    }
}
