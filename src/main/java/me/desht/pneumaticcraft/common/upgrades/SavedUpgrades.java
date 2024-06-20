package me.desht.pneumaticcraft.common.upgrades;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.common.item.UpgradeItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SavedUpgrades {
    private static final Codec<PNCUpgrade> UPGRADE_CODEC
            = ResourceLocation.CODEC.xmap(id -> ApplicableUpgradesDB.getInstance().getUpgradeById(id), PNCUpgrade::getId);
    private static final StreamCodec<ByteBuf,PNCUpgrade> UPGRADE_STREAM_CODEC
            = ResourceLocation.STREAM_CODEC.map(id -> ApplicableUpgradesDB.getInstance().getUpgradeById(id), PNCUpgrade::getId);

    public static final Codec<SavedUpgrades> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ItemContainerContents.CODEC.fieldOf("upgrades").forGetter(s -> s.contents),
            Codec.unboundedMap(UPGRADE_CODEC, ExtraCodecs.POSITIVE_INT).fieldOf("map").forGetter(s -> s.map)
    ).apply(builder, SavedUpgrades::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, SavedUpgrades> STREAM_CODEC = StreamCodec.composite(
            ItemContainerContents.STREAM_CODEC, s -> s.contents,
            ByteBufCodecs.map(Maps::newHashMapWithExpectedSize, UPGRADE_STREAM_CODEC, ByteBufCodecs.VAR_INT), s -> s.map,
            SavedUpgrades::new
    );

    public static final SavedUpgrades EMPTY = new SavedUpgrades(ItemContainerContents.fromItems(List.of()), Map.of());

    private final ItemContainerContents contents;
    private final Map<PNCUpgrade,Integer> map;

    private SavedUpgrades(ItemContainerContents contents, Map<PNCUpgrade,Integer> map) {
        this.contents = contents;
        this.map = map;
    }

    public static SavedUpgrades fromItemHandler(IItemHandler upgradeHandler) {
        ImmutableMap.Builder<PNCUpgrade,Integer> builder = ImmutableMap.builder();
        List<ItemStack> items = new ArrayList<>(upgradeHandler.getSlots());
        for (int i = 0; i < upgradeHandler.getSlots(); i++) {
            ItemStack stack = upgradeHandler.getStackInSlot(i);
            items.add(stack);
            if (stack.getItem() instanceof UpgradeItem upgradeItem) {
                builder.put(upgradeItem.getUpgradeType(), stack.getCount() * upgradeItem.getUpgradeTier());
            }
        }
        return new SavedUpgrades(ItemContainerContents.fromItems(items), builder.build());
    }

    public int getUpgradeCount(PNCUpgrade upgrade) {
        return map.getOrDefault(upgrade, 0);
    }

    public Map<PNCUpgrade, Integer> getUpgradeMap() {
        return Collections.unmodifiableMap(map);
    }

    public IItemHandler getUpgradeHandler() {
        ItemStackHandler res = new ItemStackHandler(contents.getSlots());
        for (int i = 0; i < contents.getSlots(); i++) {
            res.setStackInSlot(i, contents.getStackInSlot(i));
        }
        return res;
    }

    public void fillItemHandler(ItemStackHandler handler) {
        handler.setSize(contents.getSlots());
        for (int i = 0; i < contents.getSlots(); i++) {
            handler.setStackInSlot(i, contents.getStackInSlot(i));
        }
    }
}
