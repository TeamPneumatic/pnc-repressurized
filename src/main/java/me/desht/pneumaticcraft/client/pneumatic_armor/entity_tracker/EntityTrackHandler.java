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

package me.desht.pneumaticcraft.client.pneumatic_armor.entity_tracker;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IEntityTrackEntry;
import net.minecraft.world.entity.Entity;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public enum EntityTrackHandler {
    INSTANCE;

    private boolean frozen = false;
    private final List<Supplier<? extends IEntityTrackEntry>> pendingTrackers = new CopyOnWriteArrayList<>();
    private final List<Pair<Supplier<? extends IEntityTrackEntry>, IEntityTrackEntry>> trackers = new ArrayList<>();

    public static EntityTrackHandler getInstance() {
        return INSTANCE;
    }

    public void registerDefaultEntries() {
        register(EntityTrackEntryLivingBase::new);
        register(EntityTrackEntryHackable::new);
        register(EntityTrackEntryDrone::new);
        register(EntityTrackEntryPressurizable::new);
        register(EntityTrackEntryAgeable::new);
        register(EntityTrackEntryTameable::new);
        register(EntityTrackEntryCreeper::new);
        register(EntityTrackEntrySlime::new);
        register(EntityTrackEntryPlayer::new);
        register(EntityTrackEntryMob::new);
        register(EntityTrackEntryItemFrame::new);
        register(EntityTrackEntryPainting::new);
        register(EntityTrackEntryMinecart::new);
    }

    public void register(@Nonnull Supplier<? extends IEntityTrackEntry> entry) {
        if (frozen) throw new IllegalStateException("entity tracker registry is frozen!");
        pendingTrackers.add(entry);
    }

    public void freeze() {
        for (var sup : pendingTrackers) {
            trackers.add(Pair.of(sup, sup.get()));
        }
        frozen = true;
    }

    public List<IEntityTrackEntry> getTrackersForEntity(Entity entity) {
        return trackers.stream()
                .filter(pair -> pair.getRight().isApplicable(entity))
                .map(pair -> pair.getLeft().get())
                .collect(Collectors.toList());
    }
}