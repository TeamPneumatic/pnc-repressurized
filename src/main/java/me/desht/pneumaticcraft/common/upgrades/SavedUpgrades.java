package me.desht.pneumaticcraft.common.upgrades;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.item.UpgradeItem;
import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.*;
import java.util.function.ToIntFunction;

public class SavedUpgrades {
    private static final Codec<PNCUpgrade> UPGRADE_CODEC
            = Identifier.CODEC.xmap(id -> ApplicableUpgradesDB.getInstance().getUpgradeById(id), PNCUpgrade::getId);
    private static final StreamCodec<ByteBuf,PNCUpgrade> UPGRADE_STREAM_CODEC
            = Identifier.STREAM_CODEC.map(id -> ApplicableUpgradesDB.getInstance().getUpgradeById(id), PNCUpgrade::getId);

    public static final Codec<SavedUpgrades> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ItemContainerContents.CODEC.fieldOf("upgrades").forGetter(s -> s.contents),
            Codec.unboundedMap(UPGRADE_CODEC, ExtraCodecs.POSITIVE_INT).fieldOf("map").forGetter(s -> s.map)
    ).apply(builder, SavedUpgrades::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, SavedUpgrades> STREAM_CODEC = StreamCodec.composite(
            ItemContainerContents.STREAM_CODEC, s -> s.contents,
            ByteBufCodecs.map(Maps::newHashMapWithExpectedSize, UPGRADE_STREAM_CODEC, ByteBufCodecs.VAR_INT), s -> s.map,
            SavedUpgrades::new
    );

    public static final SavedUpgrades EMPTY = new SavedUpgrades(ItemContainerContents.EMPTY, Map.of());

    private final ItemContainerContents contents;
    private final Map<PNCUpgrade,Integer> map;

    private SavedUpgrades(ItemContainerContents contents, Map<PNCUpgrade,Integer> map) {
        this.contents = contents;
        this.map = map;
    }

    public static SavedUpgrades fromItemHandler(ResourceHandler<ItemResource> upgradeHandler, ToIntFunction<PNCUpgrade> maxCalculator) {
        Map<PNCUpgrade,Integer> map = new HashMap<>();

        List<ItemStack> items = new ArrayList<>(upgradeHandler.size());
        for (int i = 0; i < upgradeHandler.size(); i++) {
            ItemStack stack = IOHelper.getStack(upgradeHandler, i);
            items.add(stack);
            if (stack.getItem() instanceof UpgradeItem upgradeItem) {
                PNCUpgrade type = upgradeItem.getUpgradeType();
                int maxAllowed = maxCalculator.applyAsInt(type);
                map.put(type, Math.min(maxAllowed, map.getOrDefault(type, 0) + stack.getCount()) * upgradeItem.getUpgradeTier());
            }
        }
        return new SavedUpgrades(ItemContainerContents.fromItems(items), Collections.unmodifiableMap(map));
    }

    public int getUpgradeCount(PNCUpgrade upgrade) {
        return map.getOrDefault(upgrade, 0);
    }

    public Map<PNCUpgrade, Integer> getUpgradeMap() {
        return Collections.unmodifiableMap(map);
    }

    public ResourceHandler<ItemResource> getUpgradeHandler() {
        BaseItemStackHandler res = new BaseItemStackHandler(contents.getSlots());
        for (int i = 0; i < contents.getSlots(); i++) {
            res.setStackInSlot(i, contents.getStackInSlot(i));
        }
        return res;
    }

    public void fillItemHandler(BaseItemStackHandler handler) {
        // handler is expected to be large enough to hold the upgrades
        for (int i = 0; i < contents.getSlots() && i < handler.size(); i++) {
            handler.setStackInSlot(i, contents.getStackInSlot(i));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SavedUpgrades that = (SavedUpgrades) o;
        return Objects.equals(contents, that.contents) && Objects.equals(map, that.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contents, map);
    }
}
