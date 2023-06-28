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

package me.desht.pneumaticcraft.common.hacking;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorRegistry;
import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.IHackableBlock;
import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.IHackableEntity;
import me.desht.pneumaticcraft.client.pneumatic_armor.ClientArmorRegistry;
import me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler.BlockTrackerClientHandler;
import me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler.EntityTrackerClientHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderBlockTarget;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderEntityTarget;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.hacking.block.*;
import me.desht.pneumaticcraft.common.hacking.entity.*;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

public class HackManager {
    private static HackManager clientInstance, serverInstance;

    private final Map<Entity, IHackableEntity<?>> hackableEntities = new HashMap<>();
    private final Map<WorldAndCoord, Pair<Block,IHackableBlock>> hackableBlocks = new HashMap<>();
    private long lastEntityPrune = 0L;
    private long lastBlockPrune = 0L;

    private static HackManager getInstance(Level world) {
        if (world.isClientSide) {
            if (clientInstance == null) clientInstance = new HackManager();
            return clientInstance;
        } else {
            if (serverInstance == null) serverInstance = new HackManager();
            return serverInstance;
        }
    }

    public static void addDefaultEntries() {
        ICommonArmorRegistry registry = PneumaticRegistry.getInstance().getCommonArmorRegistry();

        // blocks
        registry.addHackable(Blocks.TNT, HackableTNT::new);
        registry.addHackable(Blocks.SPAWNER, HackableMobSpawner::new);
        registry.addHackable(Blocks.LEVER, HackableLever::new);
        registry.addHackable(Blocks.TRIPWIRE_HOOK, HackableTripwire::new);
        registry.addHackable(Blocks.DISPENSER, HackableDispenser::new);
        registry.addHackable(Blocks.DROPPER, HackableDispenser::new);
        registry.addHackable(ModBlocks.SECURITY_STATION.get(), HackableSecurityStation::new);
        registry.addHackable(Blocks.INFESTED_COBBLESTONE, HackableSilverfish::new);
        registry.addHackable(Blocks.INFESTED_CHISELED_STONE_BRICKS, HackableSilverfish::new);
        registry.addHackable(Blocks.INFESTED_CRACKED_STONE_BRICKS, HackableSilverfish::new);
        registry.addHackable(Blocks.INFESTED_MOSSY_STONE_BRICKS, HackableSilverfish::new);
        registry.addHackable(Blocks.INFESTED_STONE, HackableSilverfish::new);
        registry.addHackable(Blocks.INFESTED_STONE_BRICKS, HackableSilverfish::new);
        registry.addHackable(Blocks.NOTE_BLOCK, HackableNoteblock::new);
        registry.addHackable(Blocks.JUKEBOX, HackableJukebox::new);
        // block tags
        registry.addHackable(BlockTags.BUTTONS, HackableButton::new);
        registry.addHackable(BlockTags.DOORS, HackableDoor::new);
        registry.addHackable(BlockTags.TRAPDOORS, HackableTrapDoor::new);
        // entities
        registry.addHackable(Mob.class, HackableMobDisarm::new);
        registry.addHackable(Creeper.class, HackableCreeper::new);
        registry.addHackable(TamableAnimal.class, HackableTameable::new);
        registry.addHackable(Cow.class, HackableCow::new);
        registry.addHackable(Sheep.class, HackableSheep::new);
        registry.addHackable(CaveSpider.class, HackableCaveSpider::new);
        registry.addHackable(Blaze.class, HackableBlaze::new);
        registry.addHackable(Ghast.class, HackableGhast::new);
        registry.addHackable(Witch.class, HackableWitch::new);
        registry.addHackable(EnderMan.class, HackableEnderman::new);
        registry.addHackable(Bat.class, HackableBat::new);
        registry.addHackable(Horse.class, HackableHorse::new);
        registry.addHackable(Shulker.class, HackableShulker::new);
        registry.addHackable(Guardian.class, HackableGuardian::new);
        registry.addHackable(Pufferfish.class, HackablePufferfish::new);
        registry.addHackable(Squid.class, HackableSquid::new);
        registry.addHackable(Villager.class, HackableVillager::new);
        registry.addHackable(Painting.class, HackablePainting::new);
        registry.addHackable(ItemFrame.class, HackableItemFrame::new);
    }

    public static IHackableEntity<?> getHackableForEntity(Entity entity, Player player) {
        HackManager manager = getInstance(player.level());

        // prune the tracked entities map every 60 ticks, removing dead or no-longer-hackable entities
        if (player.level().getGameTime() - 60 > manager.lastEntityPrune) {
            manager.hackableEntities.entrySet().removeIf(
                    entry -> !entry.getKey().isAlive()
                            || !entry.getValue().canHack(entry.getKey(), player) && !isInDisplayCooldown(entry.getValue(), entry.getKey())
            );
            manager.lastEntityPrune = player.level().getGameTime();
        }

        // entities which implement IHackableEntity directly
        if (entity instanceof IHackableEntity h && h.canHack(entity, player))
            return h;

        // entities which have been registered with the hack manager
        IHackableEntity<?> hackable = manager.hackableEntities.get(entity);
        if (hackable == null) {
            hackable = CommonArmorRegistry.getInstance().getHackable(entity, player);
            if (hackable != null) {
                manager.hackableEntities.put(entity, hackable);
            }
        }
        return hackable;
    }

    public static IHackableBlock getHackableForBlock(BlockGetter blockGetter, BlockPos pos, Player player) {
        BlockState state = blockGetter.getBlockState(pos);
        Block block = state.getBlock();
        HackManager manager = getInstance(player.level());

        if (player.level().getGameTime() - 60 > manager.lastBlockPrune) {
            // clean up the tracked blocks map
            manager.hackableBlocks.entrySet().removeIf(
                    entry -> {
                        Block trackedBlock = entry.getValue().getLeft();
                        IHackableBlock hackableBlock = entry.getValue().getRight();
                        return block != trackedBlock ||
                                !hackableBlock.canHack(entry.getKey().world, entry.getKey().pos, state, player)
                                        && !isInDisplayCooldown(hackableBlock, entry.getKey().world, entry.getKey().pos, player);
                    }
            );
            manager.lastBlockPrune = player.level().getGameTime();
        }

        if (block instanceof IHackableBlock h && h.canHack(blockGetter, pos, state, player))
            return h;

        WorldAndCoord loc = new WorldAndCoord(blockGetter, pos);
        Pair<Block,IHackableBlock> pair = manager.hackableBlocks.get(loc);
        if (pair == null) {
            IHackableBlock hackable = CommonArmorRegistry.getInstance().getHackable(block);
            if (hackable != null && hackable.canHack(blockGetter, pos, state, player)) {
                pair = Pair.of(block, hackable);
                manager.hackableBlocks.put(loc, pair);
            } else {
                return null;
            }
        }
        return pair.getRight();
    }

    private static boolean isInDisplayCooldown(IHackableBlock hackableBlock, BlockGetter world, BlockPos pos, Player player) {
        if (player.level().isClientSide) {
            RenderBlockTarget target = ClientArmorRegistry.getInstance()
                    .getClientHandler(CommonUpgradeHandlers.blockTrackerHandler, BlockTrackerClientHandler.class)
                    .getTargetForCoord(pos);
            int requiredHackTime = hackableBlock.getHackTime(world, pos, player);
            return target != null && target.getHackTime() >= requiredHackTime && target.getHackTime() <= requiredHackTime + 20;
        } else {
            return false;
        }
    }

    private static boolean isInDisplayCooldown(IHackableEntity<?> hackableEntity, Entity entity) {
        if (entity.level().isClientSide) {
            RenderEntityTarget target = ClientArmorRegistry.getInstance()
                    .getClientHandler(CommonUpgradeHandlers.entityTrackerHandler, EntityTrackerClientHandler.class)
                    .getTargetForEntity(entity);
            int requiredHackTime = hackableEntity._getHackTime(entity, ClientUtils.getClientPlayer());
            return target != null && target.getHackTime() >= requiredHackTime && target.getHackTime() <= requiredHackTime + 20;
        } else {
            return false;
        }
    }
}
