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

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.client.IFOVModifierItem;
import me.desht.pneumaticcraft.api.item.ICustomDurabilityBar;
import me.desht.pneumaticcraft.api.pressure.IPressurizableItem;
import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.block.entity.drone.ProgrammableControllerBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.utility.ChargingStationBlockEntity;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.entity.drone.AbstractDroneEntity;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import me.desht.pneumaticcraft.common.entity.drone.ProgrammableControllerEntity;
import me.desht.pneumaticcraft.common.network.DronePacket.DroneTarget;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.ElytraHandler;
import me.desht.pneumaticcraft.common.recipes.special.OneProbeCrafting;
import me.desht.pneumaticcraft.common.registry.ModArmorMaterials;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.upgrades.UpgradableItemUtils;
import me.desht.pneumaticcraft.common.util.GlobalPosHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class PneumaticArmorItem extends ArmorItem implements
        IChargeableContainerProvider, IFOVModifierItem, ICustomDurabilityBar, IPressurizableItem,
        ColorHandlers.ITintableItem
        /*, IVisDiscountGear, IGoggles, IRevealer,*/
{
    private static final Map<EquipmentSlotGroup,ResourceLocation> MODIFIER_IDS = Util.make(
            new EnumMap<>(EquipmentSlotGroup.class), map -> {
                map.put(EquipmentSlotGroup.HEAD, RL("armor_mod_head"));
                map.put(EquipmentSlotGroup.CHEST, RL("armor_mod_chest"));
                map.put(EquipmentSlotGroup.LEGS, RL("armor_mod_legs"));
                map.put(EquipmentSlotGroup.FEET, RL("armor_mod_feet"));
            });

    private static final int[] ARMOR_VOLUMES = new int[] {
            PneumaticValues.PNEUMATIC_BOOTS_VOLUME,
            PneumaticValues.PNEUMATIC_LEGGINGS_VOLUME,
            PneumaticValues.PNEUMATIC_CHESTPLATE_VOLUME,
            PneumaticValues.PNEUMATIC_HELMET_VOLUME
    };
//    private static final int[] VIS_DISCOUNTS = new int[] { 1, 2, 2, 5 };

    public static final DyedItemColor DEFAULT_PRIMARY_COLOR = new DyedItemColor(0xFF969696, false);
    public static final DyedItemColor DEFAULT_SECONDARY_COLOR = new DyedItemColor(0xFFC0C0C0, false);
    public static final DyedItemColor DEFAULT_EYEPIECE_COLOR = new DyedItemColor(0xFF00AA00, false);

    public PneumaticArmorItem(ArmorItem.Type equipmentSlotIn) {
        super(ModArmorMaterials.PNEUMATIC.getDelegate(), equipmentSlotIn, ModItems.pressurizableToolProps());
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
        return stack.getOrDefault(ModDataComponents.AIR, 0);
    }

    @Override
    public float getMaxPressure() {
        return 10F;
    }

    @Override
    @Nullable
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        String s = slot == EquipmentSlot.LEGS ? Textures.ARMOR_PNEUMATIC + "_2.png" : Textures.ARMOR_PNEUMATIC + "_1.png";
        return ResourceLocation.parse(s);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        if (type == Type.HELMET && context.registries() != null) {
            ClientUtils.getOptionalClientLevel().ifPresent(level -> addHelmetInformation(stack, level, tooltip));
        }
    }

    private void addHelmetInformation(ItemStack stack, Level level, List<Component> tooltip) {
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

        BlockPos pos = getCoordTrackerPos(stack, level);
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
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        ItemAttributeModifiers modifiers = super.getDefaultAttributeModifiers(stack);

        if (stack.getEquipmentSlot() == type.getSlot()) {
            int upgrades = UpgradableItemUtils.getUpgradeCount(stack, ModUpgrades.ARMOR.get());
            if (upgrades > 0) {
                EquipmentSlotGroup group = EquipmentSlotGroup.bySlot(type.getSlot());
                if (MODIFIER_IDS.containsKey(group)) {
                    AttributeModifier armor = new AttributeModifier(MODIFIER_IDS.get(group), upgrades / 2d, AttributeModifier.Operation.ADD_VALUE);
                    AttributeModifier armorToughness = new AttributeModifier(MODIFIER_IDS.get(group), upgrades, AttributeModifier.Operation.ADD_VALUE);
                    return modifiers
                            .withModifierAdded(Attributes.ARMOR, armor, group)
                            .withModifierAdded(Attributes.ARMOR_TOUGHNESS, armorToughness, group);
                }
            }
        }

        return modifiers;
    }

//    @Nullable
//    @Override
//    public CompoundTag getShareTag(ItemStack stack) {
//        return ConfigHelper.common().advanced.nbtToClientModification.get() ? PressurizableItem.roundedPressure(stack) : super.getShareTag(stack);
//    }

    /* ----------- Pneumatic Helmet helpers ---------- */

    public static int getIntData(ItemStack stack, DataComponentType<Integer> componentType, int def) {
        return stack.getOrDefault(componentType, def);
    }

    public static int getIntData(ItemStack stack, DataComponentType<Integer> componentType, int def, int min, int max) {
        return Mth.clamp(stack.getOrDefault(componentType, def), min, max);
    }

    public static boolean getBooleanData(ItemStack stack, DataComponentType<Boolean> componentType, boolean def) {
        return stack.getOrDefault(componentType, def);
    }

    public static Item getSearchedItem(ItemStack helmetStack) {
        return helmetStack.has(ModDataComponents.HELMET_SEARCH_ITEM) ?
                helmetStack.get(ModDataComponents.HELMET_SEARCH_ITEM) :
                null;
    }

    public static void setSearchedItem(ItemStack helmetStack, Item searchedItem) {
        if (helmetStack.getItem() instanceof PneumaticArmorItem) {
            helmetStack.set(ModDataComponents.HELMET_SEARCH_ITEM, searchedItem);
        }
    }

    public static BlockPos getCoordTrackerPos(ItemStack helmetStack, Level world) {
        if (helmetStack.has(ModDataComponents.COORD_TRACKER)) {
            GlobalPos gPos = helmetStack.get(ModDataComponents.COORD_TRACKER);
            if (!world.isOutsideBuildHeight(gPos.pos().getY()) && GlobalPosHelper.isSameWorld(gPos, world)) {
                return helmetStack.get(ModDataComponents.COORD_TRACKER).pos();
            }
        }
        return null;
    }

    public static void setCoordTrackerPos(ItemStack helmetStack, GlobalPos gPos) {
        helmetStack.set(ModDataComponents.COORD_TRACKER, gPos);
    }

    public static String getEntityFilter(ItemStack helmetStack) {
        return helmetStack.getOrDefault(ModDataComponents.ENTITY_FILTER, "");
    }

    public static boolean isPlayerDebuggingDrone(Player player, AbstractDroneEntity drone) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.getItem() != ModItems.PNEUMATIC_HELMET.get()) return false;
        if (drone instanceof DroneEntity) {
            return helmet.getOrDefault(ModDataComponents.DRONE_DEBUG_TARGET, DroneTarget.NONE).is(drone);
        } else if (drone instanceof ProgrammableControllerEntity pc) {
            return helmet.getOrDefault(ModDataComponents.DRONE_DEBUG_TARGET, DroneTarget.NONE).is(pc.getControllerPos());
        } else {
            return false;
        }
    }

    public static boolean isPlayerDebuggingDrone(Player player, IDroneBase drone) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.getItem() != ModItems.PNEUMATIC_HELMET.get()) return false;
        if (drone instanceof DroneEntity e) {
            return helmet.getOrDefault(ModDataComponents.DRONE_DEBUG_TARGET, DroneTarget.NONE).is(e);
        } else if (drone instanceof ProgrammableControllerBlockEntity pc) {
            return helmet.getOrDefault(ModDataComponents.DRONE_DEBUG_TARGET, DroneTarget.NONE).is(pc.getControllerPos());
        } else {
            return false;
        }
    }

    /**
     * Get the debugged drone or programmable controller for the given player, who should be wearing a Pneumatic Helmet
     * @return the debugged whatever
     */
    public static IDroneBase getDebuggedDrone(Player player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.getItem() == ModItems.PNEUMATIC_HELMET.get() && helmet.has(ModDataComponents.DRONE_DEBUG_TARGET)) {
            return helmet.getOrDefault(ModDataComponents.DRONE_DEBUG_TARGET, DroneTarget.NONE).getDrone(player.level());
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
            case 0 -> getPrimaryColor(stack);
            case 1 -> getSecondaryColor(stack);
            case 2 -> stack.getItem() == ModItems.PNEUMATIC_HELMET.get() ? getEyepieceColor(stack) : 0xFFFFFFFF;
            default -> 0xFFFFFFFF;
        };
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

    public int getPrimaryColor(ItemStack stack) {
        return stack.getOrDefault(DataComponents.DYED_COLOR, DEFAULT_PRIMARY_COLOR).rgb();
    }

    public void setPrimaryColor(ItemStack stack, int color) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color, false));
    }

    public int getSecondaryColor(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.ARMOR_SECONDARY_COLOR, DEFAULT_SECONDARY_COLOR).rgb();
    }

    public void setSecondaryColor(ItemStack stack, int color) {
        stack.set(ModDataComponents.ARMOR_SECONDARY_COLOR, new DyedItemColor(color, false));
    }

    public int getEyepieceColor(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.ARMOR_EYEPIECE_COLOR, DEFAULT_EYEPIECE_COLOR).rgb();
    }

    public void setEyepieceColor(ItemStack stack, int color) {
        stack.set(ModDataComponents.ARMOR_EYEPIECE_COLOR, new DyedItemColor(color, false));
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
