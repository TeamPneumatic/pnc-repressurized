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

import com.google.common.base.Predicate;
import me.desht.pneumaticcraft.common.util.entityfilter.EntityFilter;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class StringFilterEntitySelector implements Predicate<Entity> {
    private List<EntityFilter> filters = new ArrayList<>();

    @Override
    public boolean apply(Entity entity) {
        return getFilter().stream().anyMatch(f -> f.test(entity));
    }

    protected List<EntityFilter> getFilter() {
        return filters;
    }

    public StringFilterEntitySelector setFilter(String filterStr) {
        EntityFilter filter = EntityFilter.fromString(filterStr);
        if (filter != null) {
            this.filters = new ArrayList<>();
            this.filters.add(filter);
        } else {
            this.filters.clear();
        }
        return this;
    }

    public StringFilterEntitySelector setFilter(List<EntityFilter> filters) {
        this.filters = new ArrayList<>(filters);
        return this;
    }

    public StringFilterEntitySelector addEntry(String filterStr) {
        EntityFilter filter = EntityFilter.fromString(filterStr);
        if (filter != null) {
            filters.add(filter);
        }
        return this;
    }
}
