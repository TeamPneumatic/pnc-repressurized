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

package me.desht.pneumaticcraft.common.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.client.IFOVModifierItem;
import me.desht.pneumaticcraft.api.item.ICustomDurabilityBar;
import me.desht.pneumaticcraft.api.lib.NBTKeys;
import me.desht.pneumaticcraft.api.pressure.IPressurizableItem;
import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler.EnderVisorClientHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.block.entity.ChargingStationBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.ProgrammableControllerBlockEntity;
import me.desht.pneumaticcraft.common.capabilities.AirHandlerItemStack;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.entity.drone.AbstractDroneEntity;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import me.desht.pneumaticcraft.common.entity.drone.ProgrammableControllerEntity;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.ElytraHandler;
import me.desht.pneumaticcraft.common.recipes.special.OneProbeCrafting;
import me.desht.pneumaticcraft.common.registry.ModAttachmentTypes;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.GlobalPosHelper;
import me.desht.pneumaticcraft.common.util.NBTUtils;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class PneumaticArmorItem extends ArmorItem implements
        IChargeableContainerProvider, IFOVModifierItem, ICustomDurabilityBar, IPressurizableItem,
        DyeableLeatherItem, ColorHandlers.ITintableItem
        /*, IVisDiscountGear, IGoggles, IRevealer,*/
{
    private static final UUID[] PNEUMATIC_ARMOR_MODIFIERS = new UUID[] {
            UUID.fromString("4a6bf01d-2e83-4b13-aaf0-a4c05958ea3c"),
            UUID.fromString("ad78a169-0409-47fb-8ca2-126b19196b56"),
            UUID.fromString("87bf456d-7360-407d-8592-5a2583eb948c"),
            UUID.fromString("e836e6c9-355e-49f2-87fc-331fadfdd642")
    };

    private static final ArmorMaterial PNEUMATIC_ARMOR_MATERIAL = new CompressedIronArmorMaterial(0.2f);

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
    public static final String NBT_HOVER = "Hover";
    public static final String NBT_SMART_HOVER = "SmartHover";
    public static final int DEFAULT_PRIMARY_COLOR = 0xFF969696;
    public static final int DEFAULT_SECONDARY_COLOR = 0xFFC0C0C0;
    public static final int DEFAULT_EYEPIECE_COLOR = 0xFF00AA00;

    public PneumaticArmorItem(ArmorItem.Type equipmentSlotIn) {
        super(PNEUMATIC_ARMOR_MATERIAL, equipmentSlotIn, ModItems.defaultProps());
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new EnderVisorClientHandler.PumpkinOverlay());
    }

    /**
     * Check if the player is wearing any pneumatic armor piece.
     *
     * @param player the player
     * @return true if the player is wearing pneumatic armor
     */
    public static boolean isPlayerWearingAnyPneumaticArmor(Player player) {
        for (EquipmentSlot slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
            if (isPneumaticArmorPiece(player, slot)) return true;
        }
        return false;
    }

    public static boolean isPneumaticArmorPiece(Player player, EquipmentSlot slot) {
        return player.getItemBySlot(slot).getItem() instanceof PneumaticArmorItem;
    }

    @Override
    public int getBaseVolume() {
        return ARMOR_VOLUMES[type.getSlot().getIndex()];
    }

    @Override
    public int getVolumeUpgrades(ItemStack stack) {
        return UpgradableItemUtils.getUpgradeCount(stack, ModUpgrades.VOLUME.get());
    }

    @Override
    public int getAir(ItemStack stack) {
        return stack.getData(ModAttachmentTypes.AIR.get());
    }

    @Override
    public float getMaxPressure() {
        return 10F;
    }

    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        String s = slot == EquipmentSlot.LEGS ? Textures.ARMOR_PNEUMATIC + "_2" : Textures.ARMOR_PNEUMATIC + "_1";
        return type == null ? s + ".png" : s + "_" + type + ".png";
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        if (type == Type.HELMET && worldIn != null) {
            addHelmetInformation(stack, worldIn, tooltip);
        }
    }

    private void addHelmetInformation(ItemStack stack, Level worldIn, List<Component> tooltip) {
        if (OneProbeCrafting.isOneProbeEnabled(stack)) {
            tooltip.add(xlate("gui.tooltip.item.pneumaticcraft.pneumatic_helmet.one_probe").withStyle(ChatFormatting.BLUE));
        }

        Item searchedItem = getSearchedItem(stack);
        if (searchedItem != null) {
            ItemStack searchStack = new ItemStack(searchedItem);
            if (!searchStack.isEmpty()) {
                tooltip.add(xlate("pneumaticcraft.armor.upgrade.search").append(": ").append(searchStack.getHoverName()).withStyle(ChatFormatting.YELLOW));
            }
        }

        BlockPos pos = getCoordTrackerPos(stack, worldIn);
        if (pos != null) {
            tooltip.add(xlate("pneumaticcraft.armor.upgrade.coordinate_tracker")
                    .append(": ").append(PneumaticCraftUtils.posToString(pos)).withStyle(ChatFormatting.YELLOW));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return ConfigHelper.client().armor.showEnchantGlint.get() && super.isFoil(stack);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot equipmentSlot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> multimap = HashMultimap.create(super.getAttributeModifiers(equipmentSlot, stack));

        // in future?
//        Multimap<Attribute, AttributeModifier> m = MultimapBuilder.hashKeys().hashSetValues()
//                .build(super.getAttributeModifiers(equipmentSlot, stack));

        if (equipmentSlot == type.getSlot()) {
            int upgrades = UpgradableItemUtils.getUpgradeCount(stack, ModUpgrades.ARMOR.get());
            multimap.put(Attributes.ARMOR, new AttributeModifier(PNEUMATIC_ARMOR_MODIFIERS[equipmentSlot.getIndex()], "Pneumatic Armor modifier boost", (double) upgrades / 2d, AttributeModifier.Operation.ADDITION));
            multimap.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(PNEUMATIC_ARMOR_MODIFIERS[equipmentSlot.getIndex()], "Pneumatic Armor toughness boost", upgrades, AttributeModifier.Operation.ADDITION));
        }

        return multimap;
    }

//    @Nullable
//    @Override
//    public CompoundTag getShareTag(ItemStack stack) {
//        return ConfigHelper.common().advanced.nbtToClientModification.get() ? PressurizableItem.roundedPressure(stack) : super.getShareTag(stack);
//    }

    /* ----------- Pneumatic Helmet helpers ---------- */

    public static int getIntData(ItemStack stack, String key, int def) {
        if (stack.getItem() instanceof PneumaticArmorItem && stack.hasTag() && stack.getTag().contains(key, Tag.TAG_INT)) {
            return stack.getTag().getInt(key);
        } else {
            return def;
        }
    }

    public static int getIntData(ItemStack stack, String key, int def, int min, int max) {
        if (stack.getItem() instanceof PneumaticArmorItem && stack.hasTag() && stack.getTag().contains(key, Tag.TAG_INT)) {
            return Mth.clamp(stack.getTag().getInt(key), min, max);
        } else {
            return def;
        }
    }

    public static boolean getBooleanData(ItemStack stack, String key, boolean def) {
        if (stack.getItem() instanceof PneumaticArmorItem && stack.hasTag() && stack.getTag().contains(key, Tag.TAG_BYTE)) {
            return stack.getTag().getByte(key) == 1;
        } else {
            return def;
        }
    }

    public static Item getSearchedItem(ItemStack helmetStack) {
        if (helmetStack.isEmpty() || !NBTUtils.hasTag(helmetStack, NBT_SEARCH_ITEM)) return null;
        String itemName = NBTUtils.getString(helmetStack, NBT_SEARCH_ITEM);
        return itemName.isEmpty() ? null : BuiltInRegistries.ITEM.get(new ResourceLocation(itemName));
    }

    public static void setSearchedItem(ItemStack helmetStack, Item searchedItem) {
        if (helmetStack.getItem() instanceof PneumaticArmorItem) {
            NBTUtils.setString(helmetStack, NBT_SEARCH_ITEM, PneumaticCraftUtils.getRegistryName(searchedItem).orElseThrow().toString());
        }
    }

    public static BlockPos getCoordTrackerPos(ItemStack helmetStack, Level world) {
        if (helmetStack.isEmpty() || !NBTUtils.hasTag(helmetStack, NBT_COORD_TRACKER)) return null;
        CompoundTag tag = NBTUtils.getCompoundTag(helmetStack, NBT_COORD_TRACKER);
        GlobalPos gPos = GlobalPosHelper.fromNBT(tag);
        if (world.isOutsideBuildHeight(gPos.pos().getY()) || !GlobalPosHelper.isSameWorld(gPos, world)) {
            return null;
        }
        return gPos.pos();
    }

    public static void setCoordTrackerPos(ItemStack helmetStack, GlobalPos gPos) {
        NBTUtils.setCompoundTag(helmetStack, PneumaticArmorItem.NBT_COORD_TRACKER, GlobalPosHelper.toNBT(gPos));
    }

    public static String getEntityFilter(ItemStack helmetStack) {
        if (helmetStack.isEmpty() || !NBTUtils.hasTag(helmetStack, NBT_ENTITY_FILTER)) return "";
        return NBTUtils.getString(helmetStack, NBT_ENTITY_FILTER);
    }

    public static boolean isPlayerDebuggingDrone(Player player, AbstractDroneEntity e) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.getItem() != ModItems.PNEUMATIC_HELMET.get()) return false;
        if (e instanceof DroneEntity) {
            return NBTUtils.getInteger(helmet, NBTKeys.PNEUMATIC_HELMET_DEBUGGING_DRONE) == e.getId();
        } else if (e instanceof ProgrammableControllerEntity) {
            CompoundTag tag = helmet.getTagElement(NBTKeys.PNEUMATIC_HELMET_DEBUGGING_PC);
            return tag != null && NbtUtils.readBlockPos(tag).equals(((ProgrammableControllerEntity) e).getControllerPos());
        } else {
            return false;
        }
    }

    public static boolean isPlayerDebuggingDrone(Player player, IDroneBase e) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.getItem() != ModItems.PNEUMATIC_HELMET.get()) return false;
        if (e instanceof DroneEntity) {
            return NBTUtils.getInteger(helmet, NBTKeys.PNEUMATIC_HELMET_DEBUGGING_DRONE) == ((DroneEntity)e).getId();
        } else if (e instanceof ProgrammableControllerBlockEntity) {
            CompoundTag tag = helmet.getTagElement(NBTKeys.PNEUMATIC_HELMET_DEBUGGING_PC);
            return tag != null && NbtUtils.readBlockPos(tag).equals(((ProgrammableControllerBlockEntity) e).getBlockPos());
        } else {
            return false;
        }
    }

    /**
     * Client-side method to get the debugged drone or programmable controller
     * @return the debugged whatever
     */
    public static IDroneBase getDebuggedDrone() {
        ItemStack helmet = ClientUtils.getClientPlayer().getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.getItem() == ModItems.PNEUMATIC_HELMET.get() && helmet.hasTag()) {
            CompoundTag tag = Objects.requireNonNull(helmet.getTag());
            if (tag.contains(NBTKeys.PNEUMATIC_HELMET_DEBUGGING_DRONE)) {
                int id = tag.getInt(NBTKeys.PNEUMATIC_HELMET_DEBUGGING_DRONE);
                if (id > 0) {
                    Entity e = ClientUtils.getClientLevel().getEntity(id);
                    if (e instanceof IDroneBase) return (IDroneBase) e;
                }
            }
            if (tag.contains(NBTKeys.PNEUMATIC_HELMET_DEBUGGING_PC)) {
                BlockEntity te = ClientUtils.getClientLevel().getBlockEntity(NbtUtils.readBlockPos(tag.getCompound(NBTKeys.PNEUMATIC_HELMET_DEBUGGING_PC)));
                if (te instanceof IDroneBase) {
                    return (IDroneBase) te;
                }
            }
        }
        return null;
    }

    @Override
    public float getFOVModifier(ItemStack stack, Player player, EquipmentSlot slot) {
        if (slot == EquipmentSlot.LEGS && ConfigHelper.client().armor.leggingsFOVFactor.get() > 0) {
            double boost = CommonUpgradeHandlers.runSpeedHandler.getSpeedBoostFromLegs(CommonArmorHandler.getHandlerForPlayer());
            if (boost > 0) {
                return 1.0f + (float) (boost * 2.0 * ConfigHelper.client().armor.leggingsFOVFactor.get());
            }
        }
        return 1.0f;
    }

    @Override
    public MenuProvider getContainerProvider(ChargingStationBlockEntity te) {
        return new IChargeableContainerProvider.Provider(te, ModMenuTypes.CHARGING_ARMOR.get());
    }

    @Override
    public boolean shouldShowCustomDurabilityBar(ItemStack stack) {
        return PressurizableItem.shouldShowPressureDurability(stack);
    }

    @Override
    public int getCustomDurabilityColour(ItemStack stack) {
        return PressurizableItem.getPressureDurabilityColor(stack);
    }

    @Override
    public float getCustomDurability(ItemStack stack) {
        return PNCCapabilities.getAirHandler(stack)
                .map(h -> h.getPressure() / h.maxPressure())
                .orElseThrow(RuntimeException::new);
    }

    @Override
    public int getTintColor(ItemStack stack, int tintIndex) {
        return switch (tintIndex) {
            case 0 -> getColor(stack);
            case 1 -> getSecondaryColor(stack);
            case 2 -> stack.getItem() == ModItems.PNEUMATIC_HELMET.get() ? getEyepieceColor(stack) : 0xFFFFFFFF;
            default -> 0xFFFFFFFF;
        };
    }

    @Override
    public int getColor(ItemStack stack) {
        // default IDyeableArmor gives undyed items a leather-brown colour... override for compressed-iron-grey
        CompoundTag nbt = stack.getTagElement("display");
        return nbt != null && nbt.contains("color", Tag.TAG_ANY_NUMERIC) ? nbt.getInt("color") : DEFAULT_PRIMARY_COLOR;
    }

    @Override
    public boolean makesPiglinsNeutral(ItemStack stack, LivingEntity wearer) {
        if (wearer instanceof Player player && stack.getItem() instanceof PneumaticArmorItem armor) {
            return CommonArmorHandler.getHandlerForPlayer(player)
                    .getUpgradeCount(armor.type.getSlot(), ModUpgrades.GILDED.get()) > 0;
        }
        return false;
    }

    @Override
    public boolean isEnderMask(ItemStack stack, Player player, EnderMan endermanEntity) {
        CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
        return handler.upgradeUsable(CommonUpgradeHandlers.enderVisorHandler, true);
    }

    @Override
    public boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity wearer) {
        if (wearer instanceof Player player) {
            return CommonArmorHandler.getHandlerForPlayer(player)
                    .getUpgradeCount(EquipmentSlot.FEET, ModUpgrades.FLIPPERS.get()) > 0;
        }
        return false;
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return UpgradableItemUtils.getUpgradeCount(stack, ModUpgrades.CREATIVE.get()) == 0;
    }

    @Override
    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        return entity instanceof Player player
                && CommonArmorHandler.getHandlerForPlayer(player).upgradeUsable(CommonUpgradeHandlers.elytraHandler, true);
    }

    @Override
    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        return ElytraHandler.handleFlightTick(entity, flightTicks);
    }

    /**
     * Get the overlay colour
     * @param stack the armor piece
     * @return the overlay colour in ARGB format
     */
    public int getSecondaryColor(ItemStack stack) {
        CompoundTag nbt = stack.getTagElement("display");
        return nbt != null && nbt.contains("color2", Tag.TAG_ANY_NUMERIC) ? nbt.getInt("color2") : DEFAULT_SECONDARY_COLOR;
    }

    public void setSecondaryColor(ItemStack stack, int color) {
        stack.getOrCreateTagElement("display").putInt("color2", color);
    }

    public int getEyepieceColor(ItemStack stack) {
        CompoundTag nbt = stack.getTagElement("display");
        return nbt != null && nbt.contains("color_eye", Tag.TAG_ANY_NUMERIC) ? nbt.getInt("color_eye") : DEFAULT_EYEPIECE_COLOR;
    }

    public void setEyepieceColor(ItemStack stack, int color) {
        stack.getOrCreateTagElement("display").putInt("color_eye", color);
    }

    /*------- Thaumcraft -------- */

//    private boolean hasThaumcraftUpgradeAndPressure(ItemStack stack) {
//        return getPressure(stack) > 0F && UpgradableItemUtils.getUpgrades(ModUpgrades.THAUMCRAFT.get(), stack) > 0;
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
