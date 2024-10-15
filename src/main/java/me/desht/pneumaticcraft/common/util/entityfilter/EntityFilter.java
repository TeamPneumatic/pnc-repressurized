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

package me.desht.pneumaticcraft.common.util.entityfilter;

import com.google.common.collect.ImmutableMap;
import joptsimple.internal.Strings;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.common.drone.progwidgets.IEntityProvider;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetText;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.DyeColor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class EntityFilter implements Predicate<Entity> {
    private static final Pattern ELEMENT_DIVIDER = Pattern.compile(";");
    private static final Pattern ELEMENT_SUBDIVIDER = Pattern.compile("[(),]");
    private static final Map<String,Predicate<Entity>> ENTITY_PREDICATES = ImmutableMap.<String,Predicate<Entity>>builder()
            .put("mob", e -> e instanceof Enemy && !(e instanceof TamableAnimal t && t.isTame()))
            .put("animal", e -> e instanceof Animal)
            .put("living", e -> e instanceof LivingEntity)
            .put("player", e -> e instanceof Player p && !p.isSpectator())
            .put("item", e -> e instanceof ItemEntity)
            .put("drone", e -> e instanceof DroneEntity)
            .put("boat", e -> e instanceof Boat)
            .put("minecart", e -> e instanceof AbstractMinecart)
            .put("painting", e -> e instanceof Painting)
            .put("orb", e -> e instanceof ExperienceOrb)
            .put("nothing", e -> false)
            .build();

    static final Set<String> DYE_COLORS = Util.make(new HashSet<>(), set -> {
        for (DyeColor d : DyeColor.values()) {
            set.add(d.getName());
        }
    });

    static {
        FilterModifiers.INSTANCE.registerDefaults();
    }

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

    public boolean isNone() {
        return this == deny() || rawFilter.equals("@nothing");
    }

    private static class EntityMatcher implements Predicate<Entity> {
        private final Predicate<Entity> matcher;
        private final List<ModifierEntry> modifiers = new ArrayList<>();

        private EntityMatcher(String element) {
            List<String> splits = Arrays.stream(ELEMENT_SUBDIVIDER.split(element)).map(String::trim).toList();

            String arg0 = splits.getFirst();
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
                    FilterModifier modifier = FilterModifiers.INSTANCE.getModifier(key)
                            .orElseThrow(IllegalArgumentException::new);
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

    private record ModifierEntry(FilterModifier modifier, String value, boolean sense) implements Predicate<Entity> {
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
