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
import me.desht.pneumaticcraft.api.data.PneumaticCraftTags;
import me.desht.pneumaticcraft.api.drone.DroneConstructingEvent;
import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.advancements.AdvancementTriggers;
import me.desht.pneumaticcraft.common.block.entity.ProgrammerBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.VacuumTrapBlockEntity;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.drone.DroneClaimManager;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import me.desht.pneumaticcraft.common.entity.semiblock.SpawnerAgitatorEntity;
import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import me.desht.pneumaticcraft.common.item.minigun.MinigunItem;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketModWrenchBlock;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.network.PacketServerTickTime;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorRegistry;
import me.desht.pneumaticcraft.common.recipes.machine.ExplosionCraftingRecipeImpl;
import me.desht.pneumaticcraft.common.semiblock.SemiblockTracker;
import me.desht.pneumaticcraft.common.thirdparty.ModdedWrenchUtils;
import me.desht.pneumaticcraft.common.tubemodules.ModuleNetworkManager;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Iterator;

public class MiscEventHandler {
    @SubscribeEvent
    public void onWorldTickEnd(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.level.isClientSide) {
            DroneClaimManager.getInstance(event.level).tick();

            if (event.level.getGameTime() % 100 == 0) {
                double tickTime = PneumaticCraftUtils.average(ServerLifecycleHooks.getCurrentServer().tickTimes) * 1.0E-6D;
                // In case world are going to get their own thread: MinecraftServer.getServer().worldTickTimes.get(event.world.provider.getDimension())
                NetworkHandler.sendToDimension(new PacketServerTickTime(tickTime), event.level.dimension());
            }
        }
    }
    @SubscribeEvent
    public void handleFuelEvent(FurnaceFuelBurnTimeEvent event) {
        // allow burning of PNC fuel fluids in any container item, iff item.hasContainer() is true
        ItemStack containerStack = event.getItemStack();
        if (containerStack.hasCraftingRemainingItem()) {
            FluidUtil.getFluidContained(containerStack).ifPresent(fluidStack -> {
                PneumaticCraftUtils.getRegistryName(fluidStack.getFluid()).ifPresent(regName -> {
                    if (Names.MOD_ID.equals(regName.getNamespace())) {
                        int value = PneumaticRegistry.getInstance().getFuelRegistry().getFuelValue(null, fluidStack.getFluid());
                        if (value > 0) {
                            int amountTaken = amountTaken(fluidStack.getAmount(), containerStack);
                            double mult = Math.min(amountTaken, fluidStack.getAmount()) / 1000d;
                            event.setBurnTime((int) (value * mult * ConfigHelper.common().general.fuelBucketEfficiency.get()));
                        } else {
                            event.setBurnTime(-1);
                        }
                    }
                });
            });
        }
    }

    private int amountTaken(int origAmount, ItemStack stack) {
        int newAmount = stack.getCraftingRemainingItem().getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM)
                .map(handler -> handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE).getAmount())
                .orElse(0);
        return origAmount - newAmount;
    }

    @SubscribeEvent
    public void explosionCraftingEvent(ExplosionEvent.Detonate event) {
        if (event.getLevel().isClientSide) {
            return;
        }

        Iterator<Entity> iterator = event.getAffectedEntities().iterator();
        while (iterator.hasNext()) {
            Entity entity = iterator.next();
            if (entity instanceof ItemEntity itemEntity && entity.isAlive()) {
                ItemStack stack = itemEntity.getItem();
                if (!stack.isEmpty()) {
                    boolean firstItem = true;
                    for (ItemStack result : ExplosionCraftingRecipeImpl.tryToCraft(event.getLevel(), stack)) {
                        if (firstItem) {
                            // first item in result: just replace the existing entity
                            itemEntity.setItem(result);
                            iterator.remove();
                            firstItem = false;
                        } else {
                            // subsequent items: add a new item entity
                            PneumaticCraftUtils.dropItemOnGround(result, event.getLevel(), entity.blockPosition());
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityConstruction(EntityEvent.EntityConstructing event) {
        if (event.getEntity() instanceof IDroneBase d) {
            MinecraftForge.EVENT_BUS.post(new DroneConstructingEvent(d));
        }
    }

    @SubscribeEvent
    public void onFillBucket(FillBucketEvent event) {
        if (event.getTarget() instanceof BlockHitResult brtr
                && event.getLevel().getBlockState(brtr.getBlockPos()).getBlock() instanceof LiquidBlock l
                && l.getFluid().is(PneumaticCraftTags.Fluids.CRUDE_OIL)
                && event.getEntity() instanceof ServerPlayer sp)
        {
            AdvancementTriggers.OIL_BUCKET.trigger(sp);
        }
    }

    @SubscribeEvent
    public void onModdedWrenchBlock(PlayerInteractEvent.RightClickBlock event) {
        BlockState state = event.getLevel().getBlockState(event.getPos());
        if (!event.isCanceled() && state.getBlock() instanceof IPneumaticWrenchable) {
            if (event.getHand() == InteractionHand.OFF_HAND && ModdedWrenchUtils.getInstance().isModdedWrench(event.getEntity().getItemInHand(InteractionHand.MAIN_HAND))) {
                event.setCanceled(true);
            } else if (ModdedWrenchUtils.getInstance().isModdedWrench(event.getEntity().getItemInHand(event.getHand()))) {
                if (event.getLevel().isClientSide) {
                    NetworkHandler.sendToServer(new PacketModWrenchBlock(event.getPos(), event.getFace(), event.getHand()));
                }
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onModdedWrenchEntity(PlayerInteractEvent.EntityInteract event) {
        if (!event.isCanceled() && event.getTarget() instanceof IPneumaticWrenchable) {
            if (event.getHand() == InteractionHand.OFF_HAND && ModdedWrenchUtils.getInstance().isModdedWrench(event.getEntity().getItemInHand(InteractionHand.MAIN_HAND))) {
                event.setCanceled(true);
            } else if (ModdedWrenchUtils.getInstance().isModdedWrench(event.getEntity().getItemInHand(event.getHand()))) {
                if (event.getLevel().isClientSide) {
                    NetworkHandler.sendToServer(new PacketModWrenchBlock(event.getPos(), event.getHand(), event.getTarget().getId()));
                }
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void quetziMoo(ServerChatEvent event) {
        String username = event.getUsername();
        ServerPlayer player = event.getPlayer();
        if (player != null && username != null && username.equals("Quetzz") && event.getMessage().equals("m00")) {
            for (int i = 0; i < 4; i++) {
                NetworkHandler.sendToPlayer(new PacketPlaySound(SoundEvents.COW_AMBIENT, SoundSource.NEUTRAL, player.getX(), player.getY(), player.getZ(), 1, 1, true), player);
            }
        }
    }

    @SubscribeEvent
    public void onWorldLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof Level world && !world.isClientSide) {
            ModuleNetworkManager.getInstance(world).invalidateCache();
        }
    }

    @SubscribeEvent
    public void onInventoryTracking(InventoryTrackEvent event) {
        if (event.getTileEntity() instanceof ProgrammerBlockEntity) event.setCanceled(true);
    }

    @SubscribeEvent
    public void onEquipmentChanged(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (event.getSlot().getType() == EquipmentSlot.Type.HAND && event.getTo().getItem() instanceof IPositionProvider) {
                // sync any variable values in this position provider item to the client for rendering purposes
                ((IPositionProvider) event.getTo().getItem()).syncVariables(player, event.getTo());
            } else if (event.getSlot() == EquipmentSlot.MAINHAND) {
                if (event.getTo().getItem() instanceof MinigunItem) {
                    ((MinigunItem) event.getTo().getItem()).onEquipmentChange(player, event.getTo(), true);
                } else if (event.getFrom().getItem() instanceof MinigunItem) {
                    ((MinigunItem) event.getFrom().getItem()).onEquipmentChange(player, event.getFrom(), false);
                }
            } else if (event.getSlot().getType() == EquipmentSlot.Type.ARMOR) {
                // trigger the "compressed iron man" advancement if wearing a full suit
                for (ItemStack stack : player.getArmorSlots()) {
                    if (!(stack.getItem() instanceof PneumaticArmorItem)) {
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
                if (!event.getEntityBeingMounted().onGround()) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onTagsUpdated(TagsUpdatedEvent event) {
        CommonArmorRegistry.getInstance().resolveBlockTags();

        PneumaticRegistry.getInstance().getMiscHelpers().registerXPFluid(FluidIngredient.of(1, PneumaticCraftTags.Fluids.EXPERIENCE), 20);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onMobSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (event.getSpawner() != null) {
            // tag any mob spawned by a vanilla Spawner (rather than naturally) as a "defender"
            // such defenders are immune to being absorbed by a Vacuum Trap
            // note: mobs spawned by a Pressurized Spawner are not considered to be defenders
            event.getEntity().addTag(VacuumTrapBlockEntity.DEFENDER_TAG);

            // any mob spawned due to a spawner with Agitator attached should be persistent
            // i.e. not despawn if no players nearby
            if (event.getLevel() instanceof Level level) {
                BlockEntity be = event.getSpawner().getSpawnerBlockEntity();
                if (be != null) {
                    ISemiBlock semi = SemiblockTracker.getInstance().getSemiblock(level, be.getBlockPos());
                    if (semi instanceof SpawnerAgitatorEntity) {
                        event.getEntity().setPersistenceRequired();
                    }
                }
            }
        }
    }
}
