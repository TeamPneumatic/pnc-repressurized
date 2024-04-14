package me.desht.pneumaticcraft.common.pneumatic_armor;

import me.desht.pneumaticcraft.common.block.entity.utility.ReinforcedChestBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

/**
 * Handles Block Tracker scanning of chests etc. with possible loot tables
 */
public enum BlockTrackLootable {
    INSTANCE;

    private static final List<BiConsumer<Player, BlockEntity>> PREDICATE_LIST = new CopyOnWriteArrayList<>();

    void addLootable(BiConsumer<Player, BlockEntity> predicate) {
        PREDICATE_LIST.add(predicate);
    }

    public void apply(Player player, BlockEntity blockEntity) {
        PREDICATE_LIST.forEach(c -> c.accept(player, blockEntity));
    }

    public void addDefaultEntries() {
        // vanilla chests and related blocks
        addLootable((player, blockEntity) -> {
            if (blockEntity instanceof RandomizableContainerBlockEntity r && r.canOpen(player)) {
                r.unpackLootTable(player);
            }
        });

        // pneumaticcraft reinforced and smart chests
        addLootable((player, blockEntity) -> {
            if (blockEntity instanceof ReinforcedChestBlockEntity r) {
                r.maybeFillWithLoot(player);
            }
        });
    }
}
