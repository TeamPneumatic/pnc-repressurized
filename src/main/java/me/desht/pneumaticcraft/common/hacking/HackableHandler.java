package me.desht.pneumaticcraft.common.hacking;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableBlock;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IPneumaticHelmetRegistry;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.PneumaticHelmetRegistry;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderBlockTarget;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderEntityTarget;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.BlockTrackerClientHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.EntityTrackerClientHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.hacking.block.*;
import me.desht.pneumaticcraft.common.hacking.entity.*;
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
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

public class HackableHandler {
    private final Map<Entity, IHackableEntity> trackedHackableEntities = new HashMap<>();
    private final Map<WorldAndCoord, Pair<Block,IHackableBlock>> trackedHackableBlocks = new HashMap<>();
    private static HackableHandler clientInstance, serverInstance;

    private static HackableHandler getInstance() {
        if (EffectiveSide.get() == LogicalSide.CLIENT) {
            if (clientInstance == null) clientInstance = new HackableHandler();
            return clientInstance;
        } else {
            if (serverInstance == null) serverInstance = new HackableHandler();
            return serverInstance;
        }
    }

    public static void addDefaultEntries() {
        IPneumaticHelmetRegistry manager = PneumaticRegistry.getInstance().getHelmetRegistry();

        // blocks
        manager.addHackable(Blocks.TNT, HackableTNT::new);
        manager.addHackable(Blocks.SPAWNER, HackableMobSpawner::new);
        manager.addHackable(Blocks.LEVER, HackableLever::new);
        manager.addHackable(Blocks.TRIPWIRE_HOOK, HackableTripwire::new);
        manager.addHackable(Blocks.DISPENSER, HackableDispenser::new);
        manager.addHackable(Blocks.DROPPER, HackableDispenser::new);
        manager.addHackable(ModBlocks.SECURITY_STATION.get(), HackableSecurityStation::new);
        manager.addHackable(Blocks.INFESTED_COBBLESTONE, HackableSilverfish::new);
        manager.addHackable(Blocks.INFESTED_CHISELED_STONE_BRICKS, HackableSilverfish::new);
        manager.addHackable(Blocks.INFESTED_CRACKED_STONE_BRICKS, HackableSilverfish::new);
        manager.addHackable(Blocks.INFESTED_MOSSY_STONE_BRICKS, HackableSilverfish::new);
        manager.addHackable(Blocks.INFESTED_STONE, HackableSilverfish::new);
        manager.addHackable(Blocks.INFESTED_STONE_BRICKS, HackableSilverfish::new);
        manager.addHackable(Blocks.NOTE_BLOCK, HackableNoteblock::new);
        manager.addHackable(Blocks.JUKEBOX, HackableJukebox::new);
        // block tags
        manager.addHackable(BlockTags.BUTTONS, HackableButton::new);
        manager.addHackable(BlockTags.DOORS, HackableDoor::new);
        manager.addHackable(BlockTags.TRAPDOORS, HackableTrapDoor::new);
        // entities
        manager.addHackable(LivingEntity.class, HackableMobDisarm::new);
        manager.addHackable(CreeperEntity.class, HackableCreeper::new);
        manager.addHackable(TameableEntity.class, HackableTameable::new);
        manager.addHackable(CowEntity.class, HackableCow::new);
        manager.addHackable(SheepEntity.class, HackableSheep::new);
        manager.addHackable(CaveSpiderEntity.class, HackableCaveSpider::new);
        manager.addHackable(BlazeEntity.class, HackableBlaze::new);
        manager.addHackable(GhastEntity.class, HackableGhast::new);
        manager.addHackable(WitchEntity.class, HackableWitch::new);
        manager.addHackable(EndermanEntity.class, HackableEnderman::new);
        manager.addHackable(BatEntity.class, HackableBat::new);
        manager.addHackable(HorseEntity.class, HackableHorse::new);
        manager.addHackable(ShulkerEntity.class, HackableShulker::new);
        manager.addHackable(GuardianEntity.class, HackableGuardian::new);
        manager.addHackable(VillagerEntity.class, HackableVillager::new);
        manager.addHackable(PaintingEntity.class, HackablePainting::new);
        manager.addHackable(ItemFrameEntity.class, HackableItemFrame::new);
    }

    public static IHackableEntity getHackableForEntity(Entity entity, PlayerEntity player) {
        // clean up the tracked entities map
        getInstance().trackedHackableEntities.entrySet().removeIf(
                entry -> !entry.getKey().isAlive()
                        || !entry.getValue().canHack(entry.getKey(), player) && !isInDisplayCooldown(entry.getValue(), entry.getKey())
        );

        if (entity instanceof IHackableEntity && ((IHackableEntity) entity).canHack(entity, player))
            return (IHackableEntity) entity;

        IHackableEntity hackable = getInstance().trackedHackableEntities.get(entity);
        if (hackable == null) {
            hackable = PneumaticHelmetRegistry.getInstance().getHackable(entity, player);
            if (hackable != null) {
                getInstance().trackedHackableEntities.put(entity, hackable);
            }
        }
        return hackable;
    }

    public static IHackableBlock getHackableForBlock(IBlockReader world, BlockPos pos, PlayerEntity player) {
        Block block = world.getBlockState(pos).getBlock();

        // clean up the tracked blocks map
        getInstance().trackedHackableBlocks.entrySet().removeIf(
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
        Pair<Block,IHackableBlock> pair = getInstance().trackedHackableBlocks.get(loc);
        if (pair == null) {
            IHackableBlock hackable = PneumaticHelmetRegistry.getInstance().getHackable(block);
            if (hackable != null && hackable.canHack(world, pos, player)) {
                pair = Pair.of(block, hackable);
                getInstance().trackedHackableBlocks.put(loc, pair);
            } else {
                return null;
            }
        }
        return pair.getRight();
    }

    private static boolean isInDisplayCooldown(IHackableBlock hackableBlock, IBlockReader world, BlockPos pos, PlayerEntity player) {
        if (player.world.isRemote) {
            RenderBlockTarget target = HUDHandler.getInstance().getSpecificRenderer(BlockTrackerClientHandler.class).getTargetForCoord(pos);
            int requiredHackTime = hackableBlock.getHackTime(world, pos, player);
            return target != null && target.getHackTime() >= requiredHackTime && target.getHackTime() <= requiredHackTime + 20;
        } else {
            return false;
        }
    }

    private static boolean isInDisplayCooldown(IHackableEntity hackableEntity, Entity entity) {
        if (entity.world.isRemote) {
            RenderEntityTarget target = HUDHandler.getInstance().getSpecificRenderer(EntityTrackerClientHandler.class).getTargetForEntity(entity);
            int requiredHackTime = hackableEntity.getHackTime(entity, ClientUtils.getClientPlayer());
            return target != null && target.getHackTime() >= requiredHackTime && target.getHackTime() <= requiredHackTime + 20;
        } else {
            return false;
        }
    }
}
