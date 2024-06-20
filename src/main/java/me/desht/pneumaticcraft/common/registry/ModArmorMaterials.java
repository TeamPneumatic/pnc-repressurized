package me.desht.pneumaticcraft.common.registry;

import me.desht.pneumaticcraft.api.lib.Names;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class ModArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS
            = DeferredRegister.create(Registries.ARMOR_MATERIAL, Names.MOD_ID);

    public static final DeferredHolder<ArmorMaterial,ArmorMaterial> COMPRESSED_IRON
        = ARMOR_MATERIALS.register("compressed_iron", () -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 2);
                map.put(ArmorItem.Type.LEGGINGS, 5);
                map.put(ArmorItem.Type.CHESTPLATE, 6);
                map.put(ArmorItem.Type.HELMET, 2);
                map.put(ArmorItem.Type.BODY, 5);
            }),
            9,
            SoundEvents.ARMOR_EQUIP_IRON,
            () -> Ingredient.of(ModItems.COMPRESSED_IRON_INGOT.get()),
            List.of(
                    new ArmorMaterial.Layer(RL("compressed_iron"))
            ),
            1.0f,
            0.075f
    ));

    public static final DeferredHolder<ArmorMaterial,ArmorMaterial> PNEUMATIC
            = ARMOR_MATERIALS.register("pneumatic", () -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 2);
                map.put(ArmorItem.Type.LEGGINGS, 5);
                map.put(ArmorItem.Type.CHESTPLATE, 6);
                map.put(ArmorItem.Type.HELMET, 2);
                map.put(ArmorItem.Type.BODY, 5);
            }),
            9,
            SoundEvents.ARMOR_EQUIP_IRON,
            () -> Ingredient.of(ModItems.COMPRESSED_IRON_INGOT.get()),
            List.of(
                    new ArmorMaterial.Layer(RL("pneumatic")),
                    new ArmorMaterial.Layer(RL("pneumatic"), "_overlay", true),
                    new ArmorMaterial.Layer(RL("pneumatic"), "_eyepiece", true),
                    new ArmorMaterial.Layer(RL("pneumatic"), "_translucent", false)
            ),
            1.0f,
            0.2f
    ));
}
