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
import me.desht.pneumaticcraft.common.block.tubes.ModuleNetworkManager;
import me.desht.pneumaticcraft.common.capabilities.CapabilityHacking;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.item.ItemMinigun;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketModWrenchBlock;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.network.PacketServerTickTime;
import me.desht.pneumaticcraft.common.recipes.machine.ExplosionCraftingRecipeImpl;
import me.desht.pneumaticcraft.common.thirdparty.ModdedWrenchUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import me.desht.pneumaticcraft.common.tileentity.TileEntityRefineryController;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.TableLootEntry;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
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
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.Iterator;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class MiscEventHandler {
    @SubscribeEvent
    public void onWorldTickEnd(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.world.isClientSide) {

            DroneClaimManager.getInstance(event.world).update();

            if (event.world.getGameTime() % 100 == 0) {
                double tickTime = MathHelper.average(ServerLifecycleHooks.getCurrentServer().tickTimes) * 1.0E-6D;
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
        if (!ConfigHelper.common().recipes.explosionCrafting.get() || event.getWorld().isClientSide) {
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
        if (!event.getWorld().isClientSide && event.getEntity() instanceof MobEntity) {
            MobEntity mob = (MobEntity) event.getEntity();
            mob.goalSelector.addGoal(Integer.MIN_VALUE, new EntityAINoAIWhenRidingDrone(mob));
        }
    }

    @SubscribeEvent
    public void onEntityConstruction(AttachCapabilitiesEvent<Entity> event) {
        event.addCapability(RL("hacking"), new CapabilityHacking.Provider());
    }

    @SubscribeEvent
    public void onFillBucket(FillBucketEvent event) {
        RayTraceResult rtr = event.getTarget();
        if (rtr != null && rtr.getType() == RayTraceResult.Type.BLOCK) {
            BlockRayTraceResult brtr = (BlockRayTraceResult) rtr;
            Block b = event.getWorld().getBlockState(brtr.getBlockPos()).getBlock();
            if (b instanceof FlowingFluidBlock) {
                Fluid fluid = ((FlowingFluidBlock) b).getFluid();
                if (TileEntityRefineryController.isInputFluidValid(event.getWorld(), fluid, 4) && event.getPlayer() instanceof ServerPlayerEntity) {
                    AdvancementTriggers.OIL_BUCKET.trigger((ServerPlayerEntity) event.getPlayer());
                }
            }
        }
    }

    @SubscribeEvent
    public void onModdedWrenchBlock(PlayerInteractEvent.RightClickBlock event) {
        BlockState state = event.getWorld().getBlockState(event.getPos());
        if (!event.isCanceled() && state.getBlock() instanceof IPneumaticWrenchable) {
            if (event.getHand() == Hand.OFF_HAND && ModdedWrenchUtils.getInstance().isModdedWrench(event.getPlayer().getItemInHand(Hand.MAIN_HAND))) {
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
            if (event.getHand() == Hand.OFF_HAND && ModdedWrenchUtils.getInstance().isModdedWrench(event.getPlayer().getItemInHand(Hand.MAIN_HAND))) {
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
                NetworkHandler.sendToPlayer(new PacketPlaySound(SoundEvents.COW_AMBIENT, SoundCategory.NEUTRAL, event.getPlayer().getX(), event.getPlayer().getY(), event.getPlayer().getZ(), 1, 1, true), event.getPlayer());
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (event.getWorld() instanceof World) {
            World world = (World) event.getWorld();
            if (!world.isClientSide) {
                ModuleNetworkManager.getInstance(world).invalidateCache();
            }
        }
    }

    @SubscribeEvent
    public void onInventoryTracking(InventoryTrackEvent event) {
        if (event.getTileEntity() instanceof TileEntityProgrammer) event.setCanceled(true);
    }

    @SubscribeEvent
    public void onLootTableLoad(LootTableLoadEvent event) {
        if (ConfigHelper.common().general.enableDungeonLoot.get()) {
            String prefix = "minecraft:chests/";
            String name = event.getName().toString();
            if (name.startsWith(prefix)) {
                String file = name.substring(name.indexOf(prefix) + prefix.length());
                switch (file) {
                    case "abandoned_mineshaft":
                    case "desert_pyramid":
                    case "jungle_temple":
                    case "simple_dungeon":
                    case "stronghold_corridor":
                    case "village_blacksmith":
                        event.getTable().addPool(buildLootPool("simple_dungeon_loot"));
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private LootPool buildLootPool(String name) {
        return LootPool.lootPool()
                .add(TableLootEntry.lootTableReference(RL("inject/" + name)).setWeight(1))
                .bonusRolls(0, 1)
                .name("pneumaticcraft_inject")
                .build();
    }

    @SubscribeEvent
    public void onEquipmentChanged(LivingEquipmentChangeEvent event) {
        if (event.getEntityLiving() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getEntityLiving();
            if (event.getSlot().getType() == EquipmentSlotType.Group.HAND && event.getTo().getItem() instanceof IPositionProvider) {
                // sync any variable values in this position provider item to the client for rendering purposes
                ((IPositionProvider) event.getTo().getItem()).syncVariables(player, event.getTo());
            } else if (event.getSlot() == EquipmentSlotType.MAINHAND) {
                if (event.getTo().getItem() instanceof ItemMinigun) {
                    ((ItemMinigun) event.getTo().getItem()).onEquipmentChange(player, event.getTo(), true);
                } else if (event.getFrom().getItem() instanceof ItemMinigun) {
                    ((ItemMinigun) event.getFrom().getItem()).onEquipmentChange(player, event.getFrom(), false);
                }
            } else if (event.getSlot().getType() == EquipmentSlotType.Group.ARMOR) {
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
            if (event.getEntityMounting() instanceof EntityDrone
                    && (event.getEntityBeingMounted() instanceof AbstractMinecartEntity || event.getEntityBeingMounted() instanceof BoatEntity)) {
                if (!event.getEntityBeingMounted().isOnGround()) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onTagsUpdated(TagsUpdatedEvent event) {
        PneumaticHelmetRegistry.getInstance().resolveBlockTags(event.getTagManager().getBlocks());

        PneumaticRegistry.getInstance().registerXPFluid(FluidIngredient.of(1, PneumaticCraftTags.Fluids.EXPERIENCE), 20);
    }
}
