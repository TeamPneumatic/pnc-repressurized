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

import java.util.function.BiFunction;
import java.util.Objects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

/**
 * TODO 26.1 ValueInput/ValueOutput: temporary bridge between vanilla's ValueInput/ValueOutput and the existing
 *  CompoundTag-based serialization code. The saved data format is unchanged. To be removed when the
 *  serialization code is moved to ValueInput/ValueOutput.
 */
public class ValueIOBridge {
    /**
     * {@return the whole contents of the given input, as a compound tag}
     */
    @SuppressWarnings("deprecation")
    public static CompoundTag readAll(ValueInput input) {
        return input.read(MapCodec.assumeMapUnsafe(CompoundTag.CODEC)).orElseGet(CompoundTag::new);
    }

    public static CompoundTag serialize(ValueIOSerializable obj, HolderLookup.Provider provider) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider);
        obj.serialize(output);
        return output.buildResult();
    }

    public static void deserialize(ValueIOSerializable obj, HolderLookup.Provider provider, CompoundTag tag) {
        obj.deserialize(TagValueInput.create(ProblemReporter.DISCARDING, provider, tag));
    }

    /**
     * Saved data is now (de)serialized with a codec. This codec goes through the saved data's existing CompoundTag
     * methods, so the contents of the saved data file are unchanged. Only for saved data stored in a level's data
     * storage (the level is needed for the registry lookup).
     *
     * @param loader creates the saved data from its tag
     * @param saver writes the saved data to a new tag
     * @return a level-aware codec factory for a {@link SavedDataType}
     */
    public static <T extends SavedData> SavedDataType.Factory<Codec<T>> savedDataCodec(BiFunction<CompoundTag, HolderLookup.Provider, T> loader,
                                                                                       BiFunction<T, HolderLookup.Provider, CompoundTag> saver) {
        return level -> {
            HolderLookup.Provider provider = Objects.requireNonNull(level, "level data storage required").registryAccess();
            return CompoundTag.CODEC.xmap(tag -> loader.apply(tag, provider), data -> saver.apply(data, provider));
        };
    }
}
