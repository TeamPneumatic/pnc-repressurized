package me.desht.pneumaticcraft.common.ai;

import com.google.common.base.Predicate;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.entity.Entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StringFilterEntitySelector implements Predicate<Entity> {

    private List<String> filter = new ArrayList<>();

    @Override
    public boolean apply(Entity entity) {
        for (String f : getFilter()) {
            if (PneumaticCraftUtils.isEntityValidForFilter(f, entity)) return true;
        }
        return false;
    }

    protected List<String> getFilter() {
        return filter;
    }

    public StringFilterEntitySelector setFilter(String filter) {
        this.filter = Collections.singletonList(filter);
        return this;
    }

    public StringFilterEntitySelector setFilter(List<String> filter) {
        this.filter = filter;
        return this;
    }

    public StringFilterEntitySelector addEntry(String filterEntry) {
        filter.add(filterEntry);
        return this;
    }
}
