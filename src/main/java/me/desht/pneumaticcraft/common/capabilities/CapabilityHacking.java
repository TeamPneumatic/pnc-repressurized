package me.desht.pneumaticcraft.common.capabilities;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import me.desht.pneumaticcraft.api.hacking.IHacking;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.PneumaticHelmetRegistry;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.HACKING_CAPABILITY;

public class CapabilityHacking {
    public static void register() {
        CapabilityManager.INSTANCE.register(IHacking.class, new Capability.IStorage<IHacking>() {
            @Nullable
            @Override
            public CompoundNBT writeNBT(Capability<IHacking> capability, IHacking instance, Direction side) {
                return instance.serializeNBT();
            }

            @Override
            public void readNBT(Capability<IHacking> capability, IHacking instance, Direction side, INBT nbt) {
                if (nbt instanceof CompoundNBT) instance.deserializeNBT((CompoundNBT) nbt);
            }
        }, DefaultImpl::new);
    }

    private static class DefaultImpl implements IHacking {
        private final List<IHackableEntity> hackables = new ArrayList<>();

        @Override
        public void update(Entity entity) {
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

        @Override
        public CompoundNBT serializeNBT() {
            CompoundNBT compound = new CompoundNBT();
            if (!getCurrentHacks().isEmpty()) {
                ListNBT tagList = new ListNBT();
                for (IHackableEntity hackableEntity : getCurrentHacks()) {
                    if (hackableEntity.getId() != null) {
                        CompoundNBT tag = new CompoundNBT();
                        tag.putString("id", hackableEntity.getId());
                        tagList.add(tagList.size(), tag);
                    }
                }
                compound.put("hackables", tagList);
            }
            return compound;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            getCurrentHacks().clear();
            ListNBT tagList = nbt.getList("hackables", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++) {
                String hackableId = tagList.getCompound(i).getString("id");
                Class<? extends IHackableEntity> hackableClass = PneumaticHelmetRegistry.getInstance().stringToEntityHackables.get(hackableId);
                if (hackableClass != null) {
                    try {
                        getCurrentHacks().add(hackableClass.newInstance());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.warning("hackable \"" + hackableId + "\" not found when constructing from NBT. Was it deleted?");
                }
            }
        }
    }

    public static class Provider implements ICapabilitySerializable<INBT> {
        private final IHacking impl = new DefaultImpl();
        private final LazyOptional<IHacking> l = LazyOptional.of(() -> impl);

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction facing) {
            return cap == HACKING_CAPABILITY ? l.cast() : LazyOptional.empty();
        }

        @Override
        public INBT serializeNBT() {
            return HACKING_CAPABILITY.getStorage().writeNBT(HACKING_CAPABILITY, this.impl, null);
        }

        @Override
        public void deserializeNBT(INBT nbt) {
            HACKING_CAPABILITY.getStorage().readNBT(HACKING_CAPABILITY, this.impl, null, nbt);
        }
    }
}
