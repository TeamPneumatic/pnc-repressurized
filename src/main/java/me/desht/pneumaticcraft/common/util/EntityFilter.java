package me.desht.pneumaticcraft.common.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import joptsimple.internal.Strings;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.progwidgets.IEntityProvider;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetText;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.PaintingEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntityFilter implements Predicate<Entity> {
    private static final Pattern ELEMENT_DIVIDER = Pattern.compile(";");
    private static final Pattern ELEMENT_SUBDIVIDER = Pattern.compile("[(),]");
    private static final Map<String,Predicate<Entity>> ENTITY_PREDICATES = ImmutableMap.<String,Predicate<Entity>>builder()
            .put("mob", e -> e instanceof IMob && !(e instanceof TameableEntity && ((TameableEntity) e).isTamed()))
            .put("animal", e -> e instanceof AnimalEntity)
            .put("living", e -> e instanceof LivingEntity)
            .put("player", e -> e instanceof PlayerEntity)
            .put("item", e -> e instanceof ItemEntity)
            .put("drone", e -> e instanceof EntityDrone)
            .put("boat", e -> e instanceof BoatEntity)
            .put("minecart", e -> e instanceof AbstractMinecartEntity)
            .put("painting", e -> e instanceof PaintingEntity)
            .put("orb", e -> e instanceof ExperienceOrbEntity)
            .build();

    private final List<EntityMatcher> matchers = new ArrayList<>();
    private final boolean sense;
    private final String rawFilter;

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

    public static <T extends IProgWidget & IEntityProvider> EntityFilter fromProgWidget(T widget, boolean whitelist) {
        if (widget.getParameters().size() > 1) {
            int pos = widget.getEntityFilterPosition();
            IProgWidget w = widget.getConnectedParameters()[whitelist ? pos : widget.getParameters().size() + pos];
            if (w instanceof ProgWidgetText) {
                List<String> l = new ArrayList<>();
                while (w instanceof ProgWidgetText) {
                    String str = ((ProgWidgetText) w).string;
                    Validate.isTrue(!str.startsWith("!"), "'!' negation can't be used here (put blacklist filters on left of widget)");
                    l.add(str);
                    w = w.getConnectedParameters()[0];
                }
                return new EntityFilter(Strings.join(l, ";"));
            }
        }
        return whitelist ? ConstantEntityFilter.ALLOW : ConstantEntityFilter.DENY;
    }

    public static EntityFilter fromString(String s) {
        try {
            return new EntityFilter(s);
        } catch (Exception e) {
            Log.warning("ignoring invalid filter: " + s);
            return null;
        }
    }

    public static EntityFilter allow() {
        return ConstantEntityFilter.ALLOW;
    }

    public static EntityFilter deny() {
        return ConstantEntityFilter.DENY;
    }

    @Override
    public String toString() {
        return sense ? rawFilter : "!" + rawFilter;
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
            DYE_COLORS.add(d.getTranslationKey());
        }
    }

    private enum Modifier implements BiPredicate<Entity,String> {
        AGE(ImmutableSet.of("adult", "baby"),
                Modifier::testAge
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
        HOLDING((item) -> ForgeRegistries.ITEMS.containsKey(new ResourceLocation(item)),
                "any valid item registry name, e.g. 'minecraft:cobblestone'",
                (entity, val) -> isHeldItem(entity, val, true)
        ),
        HOLDING_OFFHAND((item) -> ForgeRegistries.ITEMS.containsKey(new ResourceLocation(item)),
                "any valid item registry name, e.g. 'minecraft:cobblestone'",
                (entity, val) -> isHeldItem(entity, val, false)
        );

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
            return entity instanceof IForgeShearable
                    && ((IForgeShearable) entity).isShearable(new ItemStack(Items.SHEARS), entity.getEntityWorld(), entity.getPosition()) ?
                    val.equalsIgnoreCase("yes") : val.equalsIgnoreCase("no");
        }

        private static boolean testBreedable(Entity entity, String val) {
            return entity instanceof AnimalEntity && (((AnimalEntity) entity).getGrowingAge() == 0 ?
                    val.equalsIgnoreCase("yes") : val.equalsIgnoreCase("no")
            );
        }

        private static boolean testAge(Entity entity, String val) {
            return entity instanceof AgeableEntity && (((AgeableEntity) entity).getGrowingAge() >= 0 ?
                    val.equalsIgnoreCase("adult") : val.equalsIgnoreCase("baby"));
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
            if (entity instanceof SheepEntity) {
                return ((SheepEntity) entity).getFleeceColor().getTranslationKey().equalsIgnoreCase(val);
            } else if (entity instanceof WolfEntity) {
                return ((WolfEntity) entity).getCollarColor().getTranslationKey().equalsIgnoreCase(val);
            } else if (entity instanceof CatEntity) {
                return ((CatEntity) entity).getCollarColor().getTranslationKey().equalsIgnoreCase(val);
            } else {
                return false;
            }
        }

        private static boolean isHeldItem(Entity entity, String name, boolean mainHand) {
            if (entity instanceof LivingEntity) {
                if (!name.contains(":")) {
                    name = "minecraft:" + name;
                }
                ItemStack stack = mainHand ? ((LivingEntity) entity).getHeldItemMainhand() : ((LivingEntity) entity).getHeldItemOffhand();
                return stack.getItem().getRegistryName() != null && stack.getItem().getRegistryName().toString().equals(name);
            }
            return false;
        }
    }

    private static class EntityMatcher implements Predicate<Entity> {
        private final Pattern regex;
        private final Predicate<Entity> entityPredicate;
        private final List<ModifierEntry> modifiers = new ArrayList<>();

        private EntityMatcher(String element) {
            String[] splits = ELEMENT_SUBDIVIDER.split(element);
            for (int i = 0; i < splits.length; i++) {
                splits[i] = splits[i].trim();
            }

            if (splits[0].startsWith("@")) {
                // match by entity predicate
                String sub = splits[0].substring(1);
                if (StringUtils.countMatches(element, "(") != StringUtils.countMatches(element, ")")) {
                    throw new IllegalArgumentException("Mismatched opening/closing braces");
                }
                entityPredicate = ENTITY_PREDICATES.get(sub);
                Validate.isTrue(entityPredicate != null, "Unknown entity type specifier: @" + sub);
                regex = null;
            } else {
                // wildcard match on entity name
                entityPredicate = null;
                regex = Pattern.compile(wildcardToRegex(splits[0]), Pattern.CASE_INSENSITIVE);
            }

            for (int i = 1; i < splits.length; i++) {
                String[] parts = splits[i].split("=");
                Validate.isTrue(parts.length == 2, "Invalid modifier syntax: " + splits[i]);
                boolean sense = true;
                if (parts[0].endsWith("!")) {
                    parts[0] = parts[0].substring(0, parts[0].length() - 1);
                    sense = false;
                }
                Modifier modifier;
                try {
                    modifier = Modifier.valueOf(parts[0].toUpperCase(Locale.ROOT));
                } catch (Exception e) {
                    throw new IllegalArgumentException("Unknown modifier: " + parts[0]);
                }
                if (!modifier.isValid(parts[1])) {
                    throw new IllegalArgumentException(String.format("Invalid value '%s' for modifier '%s'. Valid values: %s",
                            parts[1], parts[0], modifier.displayValidOptions()));
                }
                modifiers.add(new ModifierEntry(modifier, parts[1], sense));
            }
        }

        @Override
        public boolean test(Entity entity) {
            boolean ok = false;
            if (entityPredicate != null) {
                ok = entityPredicate.test(entity);
            } else if (regex != null) {
                Matcher m = regex.matcher(entity.getName().getString());
                ok = m.matches();
            }
            // modifiers test is a match-all (e.g. "sheep(sheared=false,color=black)" matches sheep which are unsheared AND black)
            return ok && modifiers.stream().allMatch(modifierEntry -> modifierEntry.test(entity));
        }
    }

    private static class ModifierEntry implements Predicate<Entity> {
        final Modifier modifier;
        final String value;
        final boolean sense;

        private ModifierEntry(Modifier modifier, String value, boolean sense) {
            this.modifier = modifier;
            this.value = value;
            this.sense = sense;
        }

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
                case '*':
                    s.append(".*");
                    break;
                case '?':
                    s.append(".");
                    break;
                case '(':
                case ')':
                case '[':
                case ']':
                case '$':
                case '^':
                case '.':
                case '{':
                case '}':
                case '|':
                case '\\':
                    s.append("\\").append(c);
                    break;
                default:
                    s.append(c);
                    break;
            }
        }
        s.append('$');
        return (s.toString());
    }
}
