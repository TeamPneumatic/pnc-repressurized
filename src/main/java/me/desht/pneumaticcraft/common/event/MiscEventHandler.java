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

package me.desht.pneumaticcraft.common.event;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.block.IPneumaticWrenchable;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.InventoryTrackEvent;
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import me.desht.pneumaticcraft.api.drone.DroneConstructingEvent;
import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.PneumaticHelmetRegistry;
import me.desht.pneumaticcraft.common.PneumaticCraftTags;
import me.desht.pneumaticcraft.common.advancements.AdvancementTriggers;
import me.desht.pneumaticcraft.common.ai.DroneClaimManager;
import me.desht.pneumaticcraft.common.ai.EntityAINoAIWhenRidingDrone;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.block.entity.ProgrammerBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.RefineryControllerBlockEntity;
import me.desht.pneumaticcraft.common.capabilities.CapabilityHacking;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import me.desht.pneumaticcraft.common.item.ItemMinigun;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketModWrenchBlock;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.network.PacketServerTickTime;
import me.desht.pneumaticcraft.common.recipes.machine.ExplosionCraftingRecipeImpl;
import me.desht.pneumaticcraft.common.thirdparty.ModdedWrenchUtils;
import me.desht.pneumaticcraft.common.tubemodules.ModuleNetworkManager;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.*;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Iterator;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class MiscEventHandler {
    @SubscribeEvent
    public void onWorldTickEnd(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.world.isClientSide) {

            DroneClaimManager.getInstance(event.world).tick();

            if (event.world.getGameTime() % 100 == 0) {
                double tickTime = Mth.average(ServerLifecycleHooks.getCurrentServer().tickTimes) * 1.0E-6D;
                // In case world are going to get their own thread: MinecraftServer.getServer().worldTickTimes.get(event.world.provider.getDimension())
                NetworkHandler.sendToDimension(new PacketServerTickTime(tickTime), event.world.dimension());
            }
        }
    }
    @SubscribeEvent
    public void handleFuelEvent(FurnaceFuelBurnTimeEvent event) {
        FluidUtil.getFluidContained(event.getItemStack()).ifPresent(fluidStack -> {
            ResourceLocation name = fluidStack.getFluid().getRegistryName();
            if (Names.MOD_ID.equals(name.getNamespace())) {
                int value = PneumaticRegistry.getInstance().getFuelRegistry().getFuelValue(null, fluidStack.getFluid());
                event.setBurnTime(value > 0 ? (int)(value * ConfigHelper.common().general.fuelBucketEfficiency.get()) : -1);
            }
        });
    }

    @SubscribeEvent
    public void explosionCraftingEvent(ExplosionEvent.Detonate event) {
        if (event.getWorld().isClientSide) {
            return;
        }

        Iterator<Entity> iterator = event.getAffectedEntities().iterator();
        while (iterator.hasNext()) {
            Entity entity = iterator.next();
            if (entity instanceof ItemEntity && entity.isAlive()) {
                ItemStack stack = ((ItemEntity) entity).getItem();
                if (!stack.isEmpty()) {
                    boolean firstItem = true;
                    for (ItemStack result : ExplosionCraftingRecipeImpl.tryToCraft(event.getWorld(), stack)) {
                        if (firstItem) {
                            // first item in result: just replace the existing entity
                            ((ItemEntity) entity).setItem(result);
                            iterator.remove();
                            firstItem = false;
                        } else {
                            // subsequent items: add a new item entity
                            PneumaticCraftUtils.dropItemOnGround(result, event.getWorld(), entity.blockPosition());
                        }
                    }
                }
            }
        }
    }

//    private void checkForAdvancement(ExplosionEvent.Detonate event, ItemStack result) {
//        if ((result.getItem() == ModItems.INGOT_IRON_COMPRESSED.get() || result.getItem() == ModBlocks.COMPRESSED_IRON_BLOCK.get().asItem())) {
//            Vec3d exp = event.getExplosion().getPosition();
//            for (PlayerEntity player : event.getWorld().getEntitiesWithinAABB(PlayerEntity.class, new AxisAlignedBB(exp.x - 32, exp.y - 32, exp.z - 32, exp.x + 32, exp.y + 32, exp.z + 32))) {
//                AdvancementTriggers.EXPLODE_IRON.trigger((ServerPlayerEntity) player);
//            }
//        }
//    }

    @SubscribeEvent
    public void onEntityConstruction(EntityConstructing event) {
        if (event.getEntity() instanceof IDroneBase) {
            MinecraftForge.EVENT_BUS.post(new DroneConstructingEvent((IDroneBase) event.getEntity()));
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (!event.getWorld().isClientSide && event.getEntity() instanceof Mob mob) {
            mob.goalSelector.addGoal(Integer.MIN_VALUE, new EntityAINoAIWhenRidingDrone(mob));
        }
    }

    @SubscribeEvent
    public void onEntityConstruction(AttachCapabilitiesEvent<Entity> event) {
        event.addCapability(RL("hacking"), new CapabilityHacking.Provider());
    }

    @SubscribeEvent
    public void onFillBucket(FillBucketEvent event) {
        HitResult rtr = event.getTarget();
        if (rtr != null && rtr.getType() == HitResult.Type.BLOCK) {
            BlockHitResult brtr = (BlockHitResult) rtr;
            Block b = event.getWorld().getBlockState(brtr.getBlockPos()).getBlock();
            if (b instanceof LiquidBlock) {
                Fluid fluid = ((LiquidBlock) b).getFluid();
                if (RefineryControllerBlockEntity.isInputFluidValid(event.getWorld(), fluid, 4) && event.getPlayer() instanceof ServerPlayer) {
                    AdvancementTriggers.OIL_BUCKET.trigger((ServerPlayer) event.getPlayer());
                }
            }
        }
    }

    @SubscribeEvent
    public void onModdedWrenchBlock(PlayerInteractEvent.RightClickBlock event) {
        BlockState state = event.getWorld().getBlockState(event.getPos());
        if (!event.isCanceled() && state.getBlock() instanceof IPneumaticWrenchable) {
            if (event.getHand() == InteractionHand.OFF_HAND && ModdedWrenchUtils.getInstance().isModdedWrench(event.getPlayer().getItemInHand(InteractionHand.MAIN_HAND))) {
                event.setCanceled(true);
            } else if (ModdedWrenchUtils.getInstance().isModdedWrench(event.getPlayer().getItemInHand(event.getHand()))) {
                if (event.getWorld().isClientSide) {
                    NetworkHandler.sendToServer(new PacketModWrenchBlock(event.getPos(), event.getFace(), event.getHand()));
                }
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onModdedWrenchEntity(PlayerInteractEvent.EntityInteract event) {
        if (!event.isCanceled() && event.getTarget() instanceof IPneumaticWrenchable) {
            if (event.getHand() == InteractionHand.OFF_HAND && ModdedWrenchUtils.getInstance().isModdedWrench(event.getPlayer().getItemInHand(InteractionHand.MAIN_HAND))) {
                event.setCanceled(true);
            } else if (ModdedWrenchUtils.getInstance().isModdedWrench(event.getPlayer().getItemInHand(event.getHand()))) {
                if (event.getWorld().isClientSide) {
                    NetworkHandler.sendToServer(new PacketModWrenchBlock(event.getPos(), event.getHand(), event.getTarget().getId()));
                }
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void quetziMoo(ServerChatEvent event) {
        if (event.getUsername().equals("Quetzz") && event.getMessage().equals("m00")) {
            for (int i = 0; i < 4; i++)
                NetworkHandler.sendToPlayer(new PacketPlaySound(SoundEvents.COW_AMBIENT, SoundSource.NEUTRAL, event.getPlayer().getX(), event.getPlayer().getY(), event.getPlayer().getZ(), 1, 1, true), event.getPlayer());
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (event.getWorld() instanceof Level world) {
            if (!world.isClientSide) {
                ModuleNetworkManager.getInstance(world).invalidateCache();
            }
        }
    }

    @SubscribeEvent
    public void onInventoryTracking(InventoryTrackEvent event) {
        if (event.getTileEntity() instanceof ProgrammerBlockEntity) event.setCanceled(true);
    }

    @SubscribeEvent
    public void onLootTableLoad(LootTableLoadEvent event) {
        if (ConfigHelper.common().general.enableDungeonLoot.get()) {
            String prefix = "minecraft:chests/";
            String name = event.getName().toString();
            if (name.startsWith(prefix)) {
                String file = name.substring(name.indexOf(prefix) + prefix.length());
                switch (file) {
                    case "abandoned_mineshaft", "desert_pyramid", "jungle_temple", "simple_dungeon", "stronghold_corridor", "village_blacksmith" -> event.getTable().addPool(buildLootPool("simple_dungeon_loot"));
                    default -> {
                    }
                }
            }
        }
    }

    private LootPool buildLootPool(String name) {
        return LootPool.lootPool()
                .add(LootTableReference.lootTableReference(RL("inject/" + name)).setWeight(1))
                .setBonusRolls(UniformGenerator.between(0, 1))
                .name("pneumaticcraft_inject")
                .build();
    }

    @SubscribeEvent
    public void onEquipmentChanged(LivingEquipmentChangeEvent event) {
        if (event.getEntityLiving() instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) event.getEntityLiving();
            if (event.getSlot().getType() == EquipmentSlot.Type.HAND && event.getTo().getItem() instanceof IPositionProvider) {
                // sync any variable values in this position provider item to the client for rendering purposes
                ((IPositionProvider) event.getTo().getItem()).syncVariables(player, event.getTo());
            } else if (event.getSlot() == EquipmentSlot.MAINHAND) {
                if (event.getTo().getItem() instanceof ItemMinigun) {
                    ((ItemMinigun) event.getTo().getItem()).onEquipmentChange(player, event.getTo(), true);
                } else if (event.getFrom().getItem() instanceof ItemMinigun) {
                    ((ItemMinigun) event.getFrom().getItem()).onEquipmentChange(player, event.getFrom(), false);
                }
            } else if (event.getSlot().getType() == EquipmentSlot.Type.ARMOR) {
                // trigger the "compressed iron man" advancement if wearing a full suit
                for (ItemStack stack : player.getArmorSlots()) {
                    if (!(stack.getItem() instanceof ItemPneumaticArmor)) {
                        return;
                    }
                }
                AdvancementTriggers.PNEUMATIC_ARMOR.trigger(player);
            }
        }
    }

    @SubscribeEvent
    public void entityMounting(EntityMountEvent event) {
        if (event.isMounting()) {
            // prevent minecarts/boats which have just been dropped by drones from immediately picking up the drone
            if (event.getEntityMounting() instanceof DroneEntity
                    && (event.getEntityBeingMounted() instanceof AbstractMinecart || event.getEntityBeingMounted() instanceof Boat)) {
                if (!event.getEntityBeingMounted().isOnGround()) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onTagsUpdated(TagsUpdatedEvent event) {
        PneumaticHelmetRegistry.getInstance().resolveBlockTags(event.getTagManager().getOrEmpty(Registry.BLOCK_REGISTRY));

        PneumaticRegistry.getInstance().registerXPFluid(FluidIngredient.of(1, PneumaticCraftTags.Fluids.EXPERIENCE), 20);
    }
}
