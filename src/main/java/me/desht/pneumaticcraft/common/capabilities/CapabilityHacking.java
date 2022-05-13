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

package me.desht.pneumaticcraft.common.capabilities;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import me.desht.pneumaticcraft.api.hacking.IHacking;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorRegistry;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.api.PNCCapabilities.HACKING_CAPABILITY;

public class CapabilityHacking {
    private static class DefaultImpl implements IHacking {
        private final List<IHackableEntity> hackables = new ArrayList<>();

        @Override
        public void tick(Entity entity) {
            hackables.removeIf(hackable -> !hackable.afterHackTick(entity));
        }

        @Override
        public void addHackable(IHackableEntity hackable) {
            hackables.add(hackable);
        }

        @Override
        public List<IHackableEntity> getCurrentHacks() {
            return hackables;
        }
    }

    public static class Provider implements ICapabilitySerializable<CompoundTag> {
        private final IHacking impl = new DefaultImpl();
        private final LazyOptional<IHacking> lazy = LazyOptional.of(() -> impl);

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction facing) {
            return HACKING_CAPABILITY.orEmpty(cap, lazy);
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag compound = new CompoundTag();
            if (!impl.getCurrentHacks().isEmpty()) {
                ListTag tagList = new ListTag();
                for (IHackableEntity hackableEntity : impl.getCurrentHacks()) {
                    if (hackableEntity.getHackableId() != null) {
                        CompoundTag tag = new CompoundTag();
                        tag.putString("id", hackableEntity.getHackableId().toString());
                        tagList.add(tagList.size(), tag);
                    }
                }
                compound.put("hackables", tagList);
            }
            return compound;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            impl.getCurrentHacks().clear();
            ListTag tagList = nbt.getList("hackables", Tag.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++) {
                ResourceLocation hackableId = new ResourceLocation(tagList.getCompound(i).getString("id"));
                IHackableEntity hackable = CommonArmorRegistry.getInstance().getEntityById(hackableId);
                if (hackable != null) {
                    impl.getCurrentHacks().add(hackable);
                } else {
                    Log.warning("hackable \"" + hackableId + "\" not found when constructing from NBT. Was it deleted?");
                }
            }
        }
    }
}
