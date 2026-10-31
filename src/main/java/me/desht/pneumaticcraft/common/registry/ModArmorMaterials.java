package me.desht.pneumaticcraft.common.registry;

import me.desht.pneumaticcraft.api.data.PneumaticCraftTags;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.EnumMap;
import java.util.Map;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Armor materials are no longer registry objects in 26.1; the armor layers are now defined by the equipment
 * assets (assets/pneumaticcraft/equipment/*.json) which the materials refer to.
 */
public class ModArmorMaterials {
    public static final Identifier COMPRESSED_IRON_ARMOR = RL("compressed_iron");
    public static final Identifier PNEUMATIC_ARMOR = RL("pneumatic");

    public static final ResourceKey<EquipmentAsset> COMPRESSED_IRON_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, COMPRESSED_IRON_ARMOR);
    public static final ResourceKey<EquipmentAsset> PNEUMATIC_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, PNEUMATIC_ARMOR);

    public static final ArmorMaterial COMPRESSED_IRON = new ArmorMaterial(
            24,
            defense(),
            9,
            SoundEvents.ARMOR_EQUIP_IRON,
            1.0f,
            0.075f,
            PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
            COMPRESSED_IRON_ASSET
    );

    public static final ArmorMaterial PNEUMATIC = new ArmorMaterial(
            24,
            defense(),
            9,
            SoundEvents.ARMOR_EQUIP_IRON,
            1.0f,
            0.2f,
            PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
            PNEUMATIC_ASSET
    );

    private static Map<ArmorType, Integer> defense() {
        Map<ArmorType, Integer> map = new EnumMap<>(ArmorType.class);
        map.put(ArmorType.BOOTS, 2);
        map.put(ArmorType.LEGGINGS, 5);
        map.put(ArmorType.CHESTPLATE, 6);
        map.put(ArmorType.HELMET, 2);
        map.put(ArmorType.BODY, 5);
        return map;
    }
}
