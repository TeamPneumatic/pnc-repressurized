package me.desht.pneumaticcraft.common.hacking;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableBlock;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IPneumaticHelmetRegistry;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.PneumaticHelmetRegistry;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderBlockTarget;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderEntityTarget;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.BlockTrackUpgradeHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.EntityTrackUpgradeHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.hacking.block.*;
import me.desht.pneumaticcraft.common.hacking.entity.*;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.item.PaintingEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.horse.HorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.thread.EffectiveSide;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HackableHandler {
    private final Map<Entity, IHackableEntity> trackedHackableEntities = new HashMap<>();
    private final Map<WorldAndCoord, IHackableBlock> trackedHackableBlocks = new HashMap<>();
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
        manager.addHackable(Blocks.TNT, HackableTNT.class);
        manager.addHackable(Blocks.SPAWNER, HackableMobSpawner.class);
        manager.addHackable(Blocks.LEVER, HackableLever.class);
        manager.addHackable(BlockTags.BUTTONS, HackableButton.class);
        manager.addHackable(BlockTags.DOORS, HackableDoor.class);
        manager.addHackable(Blocks.TRIPWIRE_HOOK, HackableTripwire.class);
        manager.addHackable(Blocks.DISPENSER, HackableDispenser.class);
        manager.addHackable(Blocks.DROPPER, HackableDispenser.class);
        manager.addHackable(ModBlocks.SECURITY_STATION.get(), HackableSecurityStation.class);
        manager.addHackable(Blocks.INFESTED_COBBLESTONE, HackableSilverfish.class);
        manager.addHackable(Blocks.INFESTED_CHISELED_STONE_BRICKS, HackableSilverfish.class);
        manager.addHackable(Blocks.INFESTED_CRACKED_STONE_BRICKS, HackableSilverfish.class);
        manager.addHackable(Blocks.INFESTED_MOSSY_STONE_BRICKS, HackableSilverfish.class);
        manager.addHackable(Blocks.INFESTED_STONE, HackableSilverfish.class);
        manager.addHackable(Blocks.INFESTED_STONE_BRICKS, HackableSilverfish.class);
        manager.addHackable(Blocks.NOTE_BLOCK, HackableNoteblock.class);
        manager.addHackable(Blocks.JUKEBOX, HackableJukebox.class);

        manager.addHackable(CreeperEntity.class, HackableCreeper.class);
        manager.addHackable(TameableEntity.class, HackableTameable.class);
        manager.addHackable(CowEntity.class, HackableCow.class);
        manager.addHackable(CaveSpiderEntity.class, HackableCaveSpider.class);
        manager.addHackable(BlazeEntity.class, HackableBlaze.class);
        manager.addHackable(GhastEntity.class, HackableGhast.class);
        manager.addHackable(WitchEntity.class, HackableWitch.class);
        manager.addHackable(MobEntity.class, HackableLivingDisarm.class);
        manager.addHackable(EndermanEntity.class, HackableEnderman.class);
        manager.addHackable(BatEntity.class, HackableBat.class);
        manager.addHackable(HorseEntity.class, HackableHorse.class);
        manager.addHackable(ShulkerEntity.class, HackableShulker.class);
        manager.addHackable(GuardianEntity.class, HackableGuardian.class);
        manager.addHackable(VillagerEntity.class, HackableVillager.class);
        manager.addHackable(PaintingEntity.class, HackablePainting.class);
        manager.addHackable(ItemFrameEntity.class, HackableItemFrame.class);
    }

    public static IHackableEntity getHackableForEntity(Entity entity, PlayerEntity player) {

        //clean up the map
        getInstance().trackedHackableEntities.entrySet().removeIf(entry -> !entry.getKey().isAlive() || !entry.getValue().canHack(entry.getKey(), player) && !isInDisplayCooldown(entry.getValue(), entry.getKey()));

        if (entity instanceof IHackableEntity && ((IHackableEntity) entity).canHack(entity, player))
            return (IHackableEntity) entity;
        IHackableEntity hackable = getInstance().trackedHackableEntities.get(entity);
        if (hackable == null) {
            for (Map.Entry<Class<? extends Entity>, Class<? extends IHackableEntity>> entry : PneumaticHelmetRegistry.getInstance().hackableEntities.entrySet()) {
                if (entry.getKey().isAssignableFrom(entity.getClass())) {
                    try {
                        hackable = entry.getValue().newInstance();
                        if (hackable.canHack(entity, player)) {
                            getInstance().trackedHackableEntities.put(entity, hackable);
                            break;
                        } else {
                            hackable = null;
                        }
                    } catch (Exception e) {
                        //shouldn't happen, checked earlier.
                        e.printStackTrace();
                    }
                }
            }
        }
        return hackable;
    }

    public static IHackableBlock getHackableForCoord(WorldAndCoord coord, PlayerEntity player) {
        return getHackableForCoord(coord.world, coord.pos, player);
    }

    public static IHackableBlock getHackableForCoord(IBlockReader world, BlockPos pos, PlayerEntity player) {
        //clean up the map
        Iterator<Map.Entry<WorldAndCoord, IHackableBlock>> iterator = getInstance().trackedHackableBlocks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<WorldAndCoord, IHackableBlock> entry = iterator.next();
            Class<? extends IHackableBlock> hackableBlockClazz = PneumaticHelmetRegistry.getInstance().hackableBlocks.get(entry.getKey().getBlock());
            if (hackableBlockClazz != entry.getValue().getClass()
                    || !entry.getValue().canHack(entry.getKey().world, entry.getKey().pos, player)
                    && !isInDisplayCooldown(entry.getValue(), entry.getKey().world, entry.getKey().pos, player))
                iterator.remove();
        }

        Block block = world.getBlockState(pos).getBlock();
        if (block instanceof IHackableBlock && ((IHackableBlock) block).canHack(world, pos, player))
            return (IHackableBlock) block;
        IHackableBlock hackable = getInstance().trackedHackableBlocks.get(new WorldAndCoord(world, pos));
        if (hackable == null) {
            if (!PneumaticHelmetRegistry.getInstance().hackableBlocks.containsKey(block)) return null;
            try {
                hackable = PneumaticHelmetRegistry.getInstance().hackableBlocks.get(block).newInstance();
                if (hackable.canHack(world, pos, player)) {
                    getInstance().trackedHackableBlocks.put(new WorldAndCoord(world, pos), hackable);
                } else {
                    hackable = null;
                }
            } catch (Exception e) {
                //shouldn't happen, checked earlier.
                e.printStackTrace();
            }
        }
        return hackable;
    }

    private static boolean isInDisplayCooldown(IHackableBlock hackableBlock, IBlockReader world, BlockPos pos, PlayerEntity player) {
        if (player.world.isRemote) {
            RenderBlockTarget target = HUDHandler.instance().getSpecificRenderer(BlockTrackUpgradeHandler.class).getTargetForCoord(pos);
            int requiredHackTime = hackableBlock.getHackTime(world, pos, player);
            return target != null && target.getHackTime() >= requiredHackTime && target.getHackTime() <= requiredHackTime + 20;
        } else {
            return false;
        }
    }

    private static boolean isInDisplayCooldown(IHackableEntity hackableBlock, Entity entity) {
        if (entity.world.isRemote) {
            RenderEntityTarget target = HUDHandler.instance().getSpecificRenderer(EntityTrackUpgradeHandler.class).getTargetForEntity(entity);
            int requiredHackTime = hackableBlock.getHackTime(entity, ClientUtils.getClientPlayer());
            return target != null && target.getHackTime() >= requiredHackTime && target.getHackTime() <= requiredHackTime + 20;
        } else {
            return false;
        }
    }
}
