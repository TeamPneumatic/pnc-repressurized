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
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableBlock;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IPneumaticHelmetRegistry;
import me.desht.pneumaticcraft.client.pneumatic_armor.ArmorUpgradeClientRegistry;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.PneumaticHelmetRegistry;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderBlockTarget;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderEntityTarget;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.BlockTrackerClientHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.EntityTrackerClientHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.hacking.block.*;
import me.desht.pneumaticcraft.common.hacking.entity.*;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.item.PaintingEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.horse.HorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

public class HackManager {
    private final Map<Entity, IHackableEntity> trackedHackableEntities = new HashMap<>();
    private final Map<WorldAndCoord, Pair<Block,IHackableBlock>> trackedHackableBlocks = new HashMap<>();
    private static HackManager clientInstance, serverInstance;

    private static HackManager getInstance(World world) {
        if (world.isClientSide) {
            if (clientInstance == null) clientInstance = new HackManager();
            return clientInstance;
        } else {
            if (serverInstance == null) serverInstance = new HackManager();
            return serverInstance;
        }
    }

    public static void addDefaultEntries() {
        IPneumaticHelmetRegistry registry = PneumaticRegistry.getInstance().getHelmetRegistry();

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
        registry.addHackable(LivingEntity.class, HackableMobDisarm::new);
        registry.addHackable(CreeperEntity.class, HackableCreeper::new);
        registry.addHackable(TameableEntity.class, HackableTameable::new);
        registry.addHackable(CowEntity.class, HackableCow::new);
        registry.addHackable(SheepEntity.class, HackableSheep::new);
        registry.addHackable(CaveSpiderEntity.class, HackableCaveSpider::new);
        registry.addHackable(BlazeEntity.class, HackableBlaze::new);
        registry.addHackable(GhastEntity.class, HackableGhast::new);
        registry.addHackable(WitchEntity.class, HackableWitch::new);
        registry.addHackable(EndermanEntity.class, HackableEnderman::new);
        registry.addHackable(BatEntity.class, HackableBat::new);
        registry.addHackable(HorseEntity.class, HackableHorse::new);
        registry.addHackable(ShulkerEntity.class, HackableShulker::new);
        registry.addHackable(GuardianEntity.class, HackableGuardian::new);
        registry.addHackable(VillagerEntity.class, HackableVillager::new);
        registry.addHackable(PaintingEntity.class, HackablePainting::new);
        registry.addHackable(ItemFrameEntity.class, HackableItemFrame::new);
    }

    public static IHackableEntity getHackableForEntity(Entity entity, PlayerEntity player) {
        // clean up the tracked entities map
        getInstance(player.getCommandSenderWorld()).trackedHackableEntities.entrySet().removeIf(
                entry -> !entry.getKey().isAlive()
                        || !entry.getValue().canHack(entry.getKey(), player) && !isInDisplayCooldown(entry.getValue(), entry.getKey())
        );

        if (entity instanceof IHackableEntity && ((IHackableEntity) entity).canHack(entity, player))
            return (IHackableEntity) entity;

        IHackableEntity hackable = getInstance(player.getCommandSenderWorld()).trackedHackableEntities.get(entity);
        if (hackable == null) {
            hackable = PneumaticHelmetRegistry.getInstance().getHackable(entity, player);
            if (hackable != null) {
                getInstance(player.getCommandSenderWorld()).trackedHackableEntities.put(entity, hackable);
            }
        }
        return hackable;
    }

    public static IHackableBlock getHackableForBlock(IBlockReader world, BlockPos pos, PlayerEntity player) {
        Block block = world.getBlockState(pos).getBlock();

        // clean up the tracked blocks map
        getInstance(player.getCommandSenderWorld()).trackedHackableBlocks.entrySet().removeIf(
                entry -> {
                    Block trackedBlock = entry.getValue().getLeft();
                    IHackableBlock hackableBlock = entry.getValue().getRight();
                    return block != trackedBlock ||
                            !hackableBlock.canHack(entry.getKey().world, entry.getKey().pos, player)
                                    && !isInDisplayCooldown(hackableBlock, entry.getKey().world, entry.getKey().pos, player);
                }
        );

        if (block instanceof IHackableBlock && ((IHackableBlock) block).canHack(world, pos, player))
            return (IHackableBlock) block;

        WorldAndCoord loc = new WorldAndCoord(world, pos);
        Pair<Block,IHackableBlock> pair = getInstance(player.getCommandSenderWorld()).trackedHackableBlocks.get(loc);
        if (pair == null) {
            IHackableBlock hackable = PneumaticHelmetRegistry.getInstance().getHackable(block);
            if (hackable != null && hackable.canHack(world, pos, player)) {
                pair = Pair.of(block, hackable);
                getInstance(player.getCommandSenderWorld()).trackedHackableBlocks.put(loc, pair);
            } else {
                return null;
            }
        }
        return pair.getRight();
    }

    private static boolean isInDisplayCooldown(IHackableBlock hackableBlock, IBlockReader world, BlockPos pos, PlayerEntity player) {
        if (player.level.isClientSide) {
            RenderBlockTarget target = ArmorUpgradeClientRegistry.getInstance()
                    .getClientHandler(ArmorUpgradeRegistry.getInstance().blockTrackerHandler, BlockTrackerClientHandler.class)
                    .getTargetForCoord(pos);
            int requiredHackTime = hackableBlock.getHackTime(world, pos, player);
            return target != null && target.getHackTime() >= requiredHackTime && target.getHackTime() <= requiredHackTime + 20;
        } else {
            return false;
        }
    }

    private static boolean isInDisplayCooldown(IHackableEntity hackableEntity, Entity entity) {
        if (entity.level.isClientSide) {
            RenderEntityTarget target = ArmorUpgradeClientRegistry.getInstance()
                    .getClientHandler(ArmorUpgradeRegistry.getInstance().entityTrackerHandler, EntityTrackerClientHandler.class)
                    .getTargetForEntity(entity);
            int requiredHackTime = hackableEntity.getHackTime(entity, ClientUtils.getClientPlayer());
            return target != null && target.getHackTime() >= requiredHackTime && target.getHackTime() <= requiredHackTime + 20;
        } else {
            return false;
        }
    }
}
