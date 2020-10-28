package me.desht.pneumaticcraft.common.util;

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
import net.minecraft.entity.item.minecart.MinecartEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.common.IForgeShearable;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntityFilter implements Predicate<Entity>, com.google.common.base.Predicate<Entity> {
    private static final Pattern ELEMENT_DIVIDER = Pattern.compile(";");
    private static final Pattern ELEMENT_SUBDIVIDER = Pattern.compile("[(),]");

    private static final EntityFilter ALLOW_FILTER = EntityFilter.allow();
    private static final EntityFilter DENY_FILTER = EntityFilter.deny();

    private static final Map<String,Class<?>> ENTITY_CLASSES = new HashMap<>();
    static {
        ENTITY_CLASSES.put("mob", IMob.class);
        ENTITY_CLASSES.put("animal", AnimalEntity.class);
        ENTITY_CLASSES.put("living", LivingEntity.class);
        ENTITY_CLASSES.put("player", PlayerEntity.class);
        ENTITY_CLASSES.put("item", ItemEntity.class);
        ENTITY_CLASSES.put("drone", EntityDrone.class);
        ENTITY_CLASSES.put("boat", BoatEntity.class);
        ENTITY_CLASSES.put("minecart", MinecartEntity.class);
        ENTITY_CLASSES.put("painting", PaintingEntity.class);
        ENTITY_CLASSES.put("orb", ExperienceOrbEntity.class);
    }

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
        return whitelist ? ALLOW_FILTER : DENY_FILTER;
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
        return new ConstantEntityFilter(true);
    }

    public static EntityFilter deny() {
        return new ConstantEntityFilter(false);
    }

    @Override
    public String toString() {
        return sense ? rawFilter : "!" + rawFilter;
    }

    @Override
    public boolean apply(@Nullable Entity input) {
        return test(input);
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

    private enum Modifier {
        AGE(ImmutableSet.of("adult", "baby")),
        BREEDABLE(ImmutableSet.of("yes", "no")),
        SHEARABLE(ImmutableSet.of("yes", "no")),
        COLOR(DYE_COLORS);

        private final Set<String> vals;

        Modifier(Set<String> v) {
            vals = v;
        }

        boolean isValid(String s) {
            return vals.contains(s);
        }

        boolean match(Entity entity, String val) {
            switch (this) {
                case AGE:
                    if (entity instanceof AgeableEntity) {
                        return ((AgeableEntity) entity).getGrowingAge() >= 0 ?
                                val.equalsIgnoreCase("adult") : val.equalsIgnoreCase("baby");
                    }
                    break;
                case BREEDABLE:
                    if (entity instanceof AnimalEntity) {
                        return ((AnimalEntity) entity).getGrowingAge() == 0 ?
                                val.equalsIgnoreCase("yes") : val.equalsIgnoreCase("no");
                    }
                    break;
                case SHEARABLE:
                    if (entity instanceof IForgeShearable) {
                        return ((IForgeShearable) entity).isShearable(new ItemStack(Items.SHEARS), entity.getEntityWorld(), entity.getPosition()) ?
                                val.equalsIgnoreCase("yes") : val.equalsIgnoreCase("no");
                    }
                    break;
                case COLOR:
                    if (entity instanceof SheepEntity) {
                        return ((SheepEntity) entity).getFleeceColor().getTranslationKey().equalsIgnoreCase(val);
                    } else if (entity instanceof WolfEntity) {
                        return ((WolfEntity) entity).getCollarColor().getTranslationKey().equalsIgnoreCase(val);
                    } else if (entity instanceof CatEntity) {
                        return ((CatEntity) entity).getCollarColor().getTranslationKey().equalsIgnoreCase(val);
                    }
                    break;
            }
            return false;
        }
    }

    private static class EntityMatcher implements Predicate<Entity> {
        private final Pattern regex;
        private final Class<?> typeClass;
        private final List<Pair<Modifier,String>> modifiers = new ArrayList<>();

        private EntityMatcher(String element) {
            String[] splits = ELEMENT_SUBDIVIDER.split(element);
            for (int i = 0; i < splits.length; i++) {
                splits[i] = splits[i].trim();
            }

            if (splits[0].startsWith("@")) {
                String sub = splits[0].substring(1);
                if (StringUtils.countMatches(element, "(") != StringUtils.countMatches(element, ")")) {
                    throw new IllegalArgumentException("Mismatched opening/closing braces");
                }
                typeClass = ENTITY_CLASSES.get(sub);
                Validate.isTrue(typeClass != null, "Unknown entity type specifier: @" + sub);
                regex = null;
            } else {
                typeClass = null;
                regex = Pattern.compile(wildcardToRegex(splits[0]), Pattern.CASE_INSENSITIVE);
            }

            for (int i = 1; i < splits.length; i++) {
                String[] modifier = splits[i].split("=");
                Validate.isTrue(modifier.length == 2, "Invalid modifier syntax: " + splits[i]);
                Modifier m;
                try {
                    m = Modifier.valueOf(modifier[0].toUpperCase(Locale.ROOT));
                } catch (Exception e) {
                    throw new IllegalArgumentException("Unknown modifier: " + modifier[0]);
                }
                Validate.isTrue(m.isValid(modifier[1]), "'" + modifier[1] + "' is not a valid value for modifier '" + modifier[0] + "'.  Valid values are: " + Strings.join(m.vals, ","));
                modifiers.add(Pair.of(m, modifier[1]));
            }
        }

        @Override
        public boolean test(Entity entity) {
            boolean ok = false;
            if (typeClass != null) {
                ok = typeClass.isAssignableFrom(entity.getClass());
            } else if (regex != null) {
                Matcher m = regex.matcher(entity.getName().getString());
                ok = m.matches();
            }
            return ok && matchModifiers(entity);
        }

        private boolean matchModifiers(Entity entity) {
            // this is a match-all (e.g. "sheep(sheared=false,color=black)" matches sheep which are unsheared AND black
            for (Pair<Modifier,String> pair : modifiers) {
                Modifier modifier = pair.getLeft();
                String val = pair.getRight();
                if (!modifier.match(entity, val)) {
                    return false;
                }
            }
            return true;
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

    public static class ConstantEntityFilter extends EntityFilter {
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
}
