package me.desht.pneumaticcraft.common.util.entityfilter;

import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.neoforge.common.IShearable;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public enum FilterModifiers {
    INSTANCE;

    private final Map<String,FilterModifier> modifierMap = new ConcurrentHashMap<>();

    public void registerModifier(String name, Set<String> options, BiPredicate<Entity,String> predicate) {
        modifierMap.put(name.toLowerCase(Locale.ROOT), new FilterModifier(options, predicate));
    }

    public void registerModifier(String name, Predicate<String> validator, String desc, BiPredicate<Entity,String> predicate) {
        modifierMap.put(name.toLowerCase(Locale.ROOT), new FilterModifier(validator, desc, predicate));
    }

    public Optional<FilterModifier> getModifier(String name) {
        return Optional.ofNullable(modifierMap.get(name.toLowerCase(Locale.ROOT)));
    }

    public void registerDefaults() {
        registerModifier("age", Set.of("adult", "baby"), FilterModifiers::testAge);

        // the next four are for backwards compat, since MobType is no longer a thing
        registerModifier("aquatic", Set.of("yes", "no"),
                (entity, val) -> testEntityTypeTag(entity, val, EntityTypeTags.AQUATIC)
        );
        registerModifier("undead", Set.of("yes", "no"),
                (entity, val) -> testEntityTypeTag(entity, val, EntityTypeTags.UNDEAD)
        );
        registerModifier("illager", Set.of("yes", "no"),
                (entity, val) -> testEntityTypeTag(entity, val, EntityTypeTags.ILLAGER)
        );
        registerModifier("arthropod", Set.of("yes", "no"),
                (entity, val) -> testEntityTypeTag(entity, val, EntityTypeTags.ARTHROPOD)
        );

        registerModifier("breedable", Set.of("yes", "no"), FilterModifiers::testBreedable);
        registerModifier("shearable", Set.of("yes", "no"), FilterModifiers::testShearable);
        registerModifier("color", EntityFilter.DYE_COLORS, FilterModifiers::hasColor);

        registerModifier("holding", (item) -> BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(item)),
                "any valid item ID, e.g. 'minecraft:cobblestone'",
                (entity, val) -> isHeldItem(entity, val, true)
        );
        registerModifier("holding_offhand", (item) -> BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(item)),
                "any valid item ID, e.g. 'minecraft:cobblestone'",
                (entity, val) -> isHeldItem(entity, val, false)
        );
        registerModifier("mod", (str) -> true,
                "any mod name, e.g. 'minecraft' or 'pneumaticcraft'",
                FilterModifiers::testMod);
        registerModifier("entity_tag", (str) -> true,
                "any string tag (added to entities with the /tag command)",
                FilterModifiers::testEntityTag);
        registerModifier("type_tag", rl -> ResourceLocation.tryParse(rl) != null,
                "any known entity type tag, e.g 'minecraft:skeletons'",
                FilterModifiers::testTypeTag);
        registerModifier("team", (str) -> true,
                "any valid Minecraft team name",
                FilterModifiers::testTeamName);
    }

    private static boolean testShearable(Entity entity, String val) {
        return entity instanceof IShearable s
                && s.isShearable(null, new ItemStack(Items.SHEARS), entity.getCommandSenderWorld(), entity.blockPosition()) ?
                val.equalsIgnoreCase("yes") : val.equalsIgnoreCase("no");
    }

    private static boolean testBreedable(Entity entity, String val) {
        return entity instanceof Animal a && val.equalsIgnoreCase(a.getAge() == 0 ? "yes" : "no");
    }

    private static boolean testEntityTypeTag(Entity entity, String val, TagKey<EntityType<?>> key) {
        return val.equalsIgnoreCase(entity.getType().is(key) ? "yes" : "no");
    }

    private static boolean testAge(Entity entity, String val) {
        return val.equalsIgnoreCase(entity instanceof AgeableMob a && a.getAge() >= 0 ? "adult" : "baby");
    }

    private static boolean testMod(Entity entity, String modName) {
        ResourceLocation rl = PneumaticCraftUtils.getRegistryName(entity).orElseThrow();
        return rl.getNamespace().toLowerCase(Locale.ROOT).equals(modName.toLowerCase(Locale.ROOT));
    }

    private static boolean testEntityTag(Entity entity, String val) {
        return entity.getTags().contains(val);
    }

    private static boolean testTypeTag(Entity entity, String val) {
        return ResourceLocation.read(val).result()
                .map(rl -> entity.getType().is(TagKey.create(Registries.ENTITY_TYPE, rl)))
                .orElse(false);
    }

    private static boolean testTeamName(Entity entity, String val) {
        PlayerTeam team = entity.getTeam();
        return team != null
                && (team.getName().equalsIgnoreCase(val) || team.getDisplayName().getString().equalsIgnoreCase(val));
    }


    private static boolean hasColor(Entity entity, String val) {
        return switch (entity) {
            case Sheep s -> s.getColor().getName().equalsIgnoreCase(val);
            case Wolf w -> w.getCollarColor().getName().equalsIgnoreCase(val);
            case Cat c -> c.getCollarColor().getName().equalsIgnoreCase(val);
            case null, default -> false;
        };
    }

    private static boolean isHeldItem(Entity entity, String name, boolean mainHand) {
        if (entity instanceof LivingEntity l) {
            if (name.startsWith("#")) {
                try {
                    TagKey<Item> tag = TagKey.create(Registries.ITEM, ResourceLocation.parse(name.substring(1)));
                    return mainHand ? l.getMainHandItem().is(tag) : l.getOffhandItem().is(tag);
                } catch (ResourceLocationException ignored) {
                    return false;
                }
            } else {
                if (!name.contains(":")) {
                    name = "minecraft:" + name;
                }
                ItemStack stack = mainHand ? l.getMainHandItem() : l.getOffhandItem();
                return PneumaticCraftUtils.getRegistryName(stack.getItem()).orElseThrow().toString().equals(name);
            }
        }
        return false;
    }
}
