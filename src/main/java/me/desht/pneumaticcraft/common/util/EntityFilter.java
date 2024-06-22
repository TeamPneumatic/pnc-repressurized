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

package me.desht.pneumaticcraft.common.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import joptsimple.internal.Strings;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.common.drone.progwidgets.IEntityProvider;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetText;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.neoforge.common.IShearable;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class EntityFilter implements Predicate<Entity> {
    private static final Pattern ELEMENT_DIVIDER = Pattern.compile(";");
    private static final Pattern ELEMENT_SUBDIVIDER = Pattern.compile("[(),]");
    private static final Map<String,Predicate<Entity>> ENTITY_PREDICATES = ImmutableMap.<String,Predicate<Entity>>builder()
            .put("mob", e -> e instanceof Enemy && !(e instanceof TamableAnimal t && t.isTame()))
            .put("animal", e -> e instanceof Animal)
            .put("living", e -> e instanceof LivingEntity)
            .put("player", e -> e instanceof Player)
            .put("item", e -> e instanceof ItemEntity)
            .put("drone", e -> e instanceof DroneEntity)
            .put("boat", e -> e instanceof Boat)
            .put("minecart", e -> e instanceof AbstractMinecart)
            .put("painting", e -> e instanceof Painting)
            .put("orb", e -> e instanceof ExperienceOrb)
            .put("nothing", e -> false)
            .build();

    private final List<EntityMatcher> matchers = new ArrayList<>();
    private final boolean sense;
    private final String rawFilter;

    /**
     * Create a new entity filter
     * @param filter the filter specification
     * @throws IllegalArgumentException if the spec is not valid
     */
    public EntityFilter(String filter) {
        if (filter.startsWith("!")) {
            filter = filter.substring(1);
            sense = false;
        } else {
            sense = true;
        }

        rawFilter = filter;

        if (!filter.isEmpty()) {
            Arrays.stream(ELEMENT_DIVIDER.split(filter)).map(EntityMatcher::new).forEach(matchers::add);
        }
    }

    /**
     * Create a new entity filter from a progwidget, which has one or more text widgets attached
     * @param widget the progwidget
     * @param whitelist true if this should be a whitelist (look to the widget's right) or blacklist (look to the left)
     * @param <T> widget type
     * @return an entity filter
     * @throws IllegalArgumentException if any of the attached text widgets contain an invalid filter spec
     */
    public static <T extends IEntityProvider & IProgWidget> EntityFilter fromProgWidget(T widget, boolean whitelist) {
        if (widget.getParameters().size() > 1) {
            int pos = widget.getEntityFilterPosition();
            IProgWidget w = widget.getConnectedParameters()[whitelist ? pos : widget.getParameters().size() + pos];
            if (w instanceof ProgWidgetText) {
                List<String> l = new ArrayList<>();
                while (w instanceof ProgWidgetText txt) {
                    String str = txt.getString();
                    Validate.isTrue(!str.startsWith("!"), "'!' negation can't be used here (put blacklist filters on left of widget)");
                    l.add(str);
                    w = w.getConnectedParameters()[0];
                }
                return new EntityFilter(Strings.join(l, ";"));
            }
        }
        return whitelist ? ConstantEntityFilter.ALLOW : ConstantEntityFilter.DENY;
    }

    /**
     * Create an entity filter from string
     * @param s the filter spec
     * @return an entity filter, or null if the spec is not valid
     */
    public static EntityFilter fromString(String s) {
        return fromString(s, null);
    }

    /**
     * Create an entity filter from string
     * @param s the filter spec
     * @param fallback a fallback filter to use
     * @return an entity filter, or the fallback filter if the spec is not valid
     */
    public static EntityFilter fromString(String s, EntityFilter fallback) {
        try {
            return new EntityFilter(s);
        } catch (Exception e) {
            return fallback;
        }
    }

    /**
     * An entity filter which allows everything
     * @return an entity filter
     */
    public static EntityFilter allow() {
        return ConstantEntityFilter.ALLOW;
    }

    /**
     * An entity filter which allows nothing
     * @return an entity filter
     */
    public static EntityFilter deny() {
        return ConstantEntityFilter.DENY;
    }

    @Override
    public String toString() {
        if (this == ConstantEntityFilter.ALLOW) {
            return "";
        } else if (this == ConstantEntityFilter.DENY) {
            return "@nothing";
        } else {
            return sense ? rawFilter : "!" + rawFilter;
        }
    }

    @Override
    public boolean test(Entity entity) {
        if (matchers.isEmpty()) return true;

        for (EntityMatcher m : matchers) {
            if (m.test(entity)) return sense;
        }
        return !sense;
    }

    private static final Set<String> DYE_COLORS = new HashSet<>();
    static {
        for (DyeColor d : DyeColor.values()) {
            DYE_COLORS.add(d.getName());
        }
    }

    public boolean isNone() {
        return this == deny() || rawFilter.equals("@nothing");
    }

    private enum Modifier implements BiPredicate<Entity,String> {
        AGE(ImmutableSet.of("adult", "baby"),
                Modifier::testAge
        ),
        // the next four are for backwards compat, since MobType is no longer a thing
        AQUATIC(ImmutableSet.of("yes", "no"),
                (entity, val) -> testEntityTypeTag(entity, val, EntityTypeTags.AQUATIC)
        ),
        UNDEAD(ImmutableSet.of("yes", "no"),
                (entity, val) -> testEntityTypeTag(entity, val, EntityTypeTags.UNDEAD)
        ),
        ILLAGER(ImmutableSet.of("yes", "no"),
                (entity, val) -> testEntityTypeTag(entity, val, EntityTypeTags.ILLAGER)
        ),
        ARTHROPOD(ImmutableSet.of("yes", "no"),
                (entity, val) -> testEntityTypeTag(entity, val, EntityTypeTags.ARTHROPOD)
        ),
        BREEDABLE(ImmutableSet.of("yes", "no"),
                Modifier::testBreedable
        ),
        SHEARABLE(ImmutableSet.of("yes", "no"),
                Modifier::testShearable
        ),
        COLOR(DYE_COLORS,
                Modifier::hasColor
        ),
        HOLDING((item) -> BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(item)),
                "any valid item ID, e.g. 'minecraft:cobblestone'",
                (entity, val) -> isHeldItem(entity, val, true)
        ),
        HOLDING_OFFHAND((item) -> BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(item)),
                "any valid item ID, e.g. 'minecraft:cobblestone'",
                (entity, val) -> isHeldItem(entity, val, false)
        ),
        MOD((str) -> true,
                "any mod name, e.g. 'minecraft' or 'pneumaticcraft'",
                Modifier::testMod),
        ENTITY_TAG((str) -> true,
                "any string tag (added to entities with the /tag command)",
                Modifier::testEntityTag),
        TYPE_TAG(rl -> ResourceLocation.tryParse(rl) != null,
                "any known entity type tag, e.g 'minecraft:skeletons'",
                Modifier::testTypeTag),
        TEAM((str) -> true,
                "any valid Minecraft team name",
                Modifier::testTeamName);

        private final Set<String> validationSet;
        private final Predicate<String> validationPredicate;
        private final String valText;
        private final BiPredicate<Entity,String> testPredicate;

        Modifier(Predicate<String> validationPredicate, String valText, BiPredicate<Entity, String> testPredicate) {
            this.validationPredicate = validationPredicate;
            this.valText = valText;
            this.testPredicate = testPredicate;
            this.validationSet = Collections.emptySet();
        }

        Modifier(Set<String> validationSet, BiPredicate<Entity, String> testPredicate) {
            this.validationPredicate = null;
            this.valText = "";
            this.testPredicate = testPredicate;
            this.validationSet = validationSet;
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

        boolean isValid(String s) {
            return validationPredicate == null ? validationSet.contains(s) : validationPredicate.test(s);
        }

        @Override
        public boolean test(Entity entity, String val) {
            return testPredicate.test(entity, val);
        }

        public String displayValidOptions() {
            return validationSet.isEmpty() ? valText : Strings.join(validationSet, ",");
        }

        private static boolean hasColor(Entity entity, String val) {
            if (entity instanceof Sheep s) {
                return s.getColor().getName().equalsIgnoreCase(val);
            } else if (entity instanceof Wolf w) {
                return w.getCollarColor().getName().equalsIgnoreCase(val);
            } else if (entity instanceof Cat c) {
                return c.getCollarColor().getName().equalsIgnoreCase(val);
            } else {
                return false;
            }
        }

        private static boolean isHeldItem(Entity entity, String name, boolean mainHand) {
            if (entity instanceof LivingEntity l) {
                if (!name.contains(":")) {
                    name = "minecraft:" + name;
                }
                ItemStack stack = mainHand ? l.getMainHandItem() : l.getOffhandItem();
                return PneumaticCraftUtils.getRegistryName(stack.getItem()).orElseThrow().toString().equals(name);
            }
            return false;
        }
    }

    private static class EntityMatcher implements Predicate<Entity> {
        private final Predicate<Entity> matcher;
        private final List<ModifierEntry> modifiers = new ArrayList<>();

        private EntityMatcher(String element) {
            List<String> splits = Arrays.stream(ELEMENT_SUBDIVIDER.split(element)).map(String::trim).toList();

            String arg0 = splits.get(0);
            if (arg0.startsWith("@")) {
                // match by entity predicate
                String sub = arg0.substring(1);
                if (StringUtils.countMatches(element, "(") != StringUtils.countMatches(element, ")")) {
                    throw new IllegalArgumentException("Mismatched opening/closing braces");
                }
                matcher = ENTITY_PREDICATES.get(sub);
                Validate.isTrue(matcher != null, "Unknown entity type specifier: @" + sub);
            } else if (arg0.length() > 2 && (arg0.startsWith("\"") && arg0.endsWith("\"") || arg0.startsWith("'") && arg0.endsWith("'"))) {
                // match an entity with a custom name
                Pattern regex = Pattern.compile(wildcardToRegex(arg0.substring(1, arg0.length() - 1)));
                matcher = e -> matchByName(e, regex);
            } else {
                // wildcard match on entity type name
                Pattern regex = Pattern.compile(wildcardToRegex(arg0), Pattern.CASE_INSENSITIVE);
                matcher = e -> regex.matcher(PneumaticCraftUtils.getRegistryName(e).orElseThrow().getPath()).matches();
            }

            for (int i = 1; i < splits.size(); i++) {
                String[] parts = splits.get(i).split("=");
                Validate.isTrue(parts.length == 2, "Invalid modifier syntax: " + splits.get(i));
                String key = parts[0], arg = parts[1];
                boolean sense = true;
                if (key.endsWith("!")) {
                    key = key.substring(0, key.length() - 1);
                    sense = false;
                }
                try {
                    Modifier modifier = Modifier.valueOf(key.toUpperCase(Locale.ROOT));
                    if (!modifier.isValid(arg)) {
                        throw new IllegalArgumentException(String.format("Invalid value '%s' for modifier '%s'. Valid values: %s",
                                arg, key, modifier.displayValidOptions()));
                    }
                    modifiers.add(new ModifierEntry(modifier, arg, sense));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Unknown modifier: " + key);
                }
            }
        }

        @Override
        public boolean test(Entity entity) {
            // modifiers test is a match-all (e.g. "sheep(sheared=false,color=black)" matches sheep which are unsheared AND black)
            return matcher.test(entity) && modifiers.stream().allMatch(modifierEntry -> modifierEntry.test(entity));
        }

        private static boolean matchByName(Entity entity, Pattern regex) {
            return entity instanceof Player player ?
                    player.getGameProfile().getName() != null && regex.matcher(player.getGameProfile().getName()).matches() :
                    entity.getCustomName() != null && regex.matcher(entity.getCustomName().getString()).matches();
        }
    }

    private record ModifierEntry(Modifier modifier, String value, boolean sense) implements Predicate<Entity> {
        @Override
        public boolean test(Entity e) {
            return modifier.test(e, value) == sense;
        }
    }

    public static class ConstantEntityFilter extends EntityFilter {
        static final ConstantEntityFilter ALLOW = new ConstantEntityFilter(true);
        static final ConstantEntityFilter DENY = new ConstantEntityFilter(false);

        private final boolean allow;

        private ConstantEntityFilter(boolean allow) {
            super("");
            this.allow = allow;
        }

        @Override
        public boolean test(Entity entity) {
            return allow;
        }
    }

    private static String wildcardToRegex(String wildcard) {
        StringBuilder s = new StringBuilder(wildcard.length());
        s.append('^');
        for (int i = 0, is = wildcard.length(); i < is; i++) {
            char c = wildcard.charAt(i);
            switch (c) {
                case '*' -> s.append(".*");
                case '?' -> s.append(".");
                case '(', ')', '[', ']', '$', '^', '.', '{', '}', '|', '\\' -> s.append("\\").append(c);
                default -> s.append(c);
            }
        }
        s.append('$');
        return (s.toString());
    }
}
