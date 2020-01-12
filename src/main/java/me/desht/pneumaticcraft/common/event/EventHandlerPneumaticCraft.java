package me.desht.pneumaticcraft.common.event;

import me.desht.pneumaticcraft.api.block.IPneumaticWrenchable;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.EntityTrackEvent;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.InventoryTrackEvent;
import me.desht.pneumaticcraft.api.drone.AmadronRetrievalEvent;
import me.desht.pneumaticcraft.api.drone.DroneConstructingEvent;
import me.desht.pneumaticcraft.api.drone.DroneSuicideEvent;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.DroneRegistry;
import me.desht.pneumaticcraft.common.PneumaticCraftAPIHandler;
import me.desht.pneumaticcraft.common.advancements.AdvancementTriggers;
import me.desht.pneumaticcraft.common.ai.EntityAINoAIWhenRidingDrone;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.block.tubes.ModuleNetworkManager;
import me.desht.pneumaticcraft.common.capabilities.CapabilityHacking;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.config.aux.AmadronOfferStaticConfig;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.entity.EntityProgrammableController;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.hacking.entity.HackableEnderman;
import me.desht.pneumaticcraft.common.item.ItemAmadronTablet;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketModWrenchBlock;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOfferCustom;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOfferManager;
import me.desht.pneumaticcraft.common.recipes.machine.ExplosionCraftingRecipe;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import me.desht.pneumaticcraft.common.thirdparty.ModdedWrenchUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import me.desht.pneumaticcraft.common.tileentity.TileEntityRefineryController;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class EventHandlerPneumaticCraft {

    @SubscribeEvent
    public void handleFuelEvent(FurnaceFuelBurnTimeEvent event) {
        FluidUtil.getFluidContained(event.getItemStack()).ifPresent(fluidStack -> {
            ResourceLocation name = fluidStack.getFluid().getRegistryName();
            if (Names.MOD_ID.equals(name.getNamespace())) {
                int value = PneumaticCraftAPIHandler.getInstance().liquidFuels.getOrDefault(name, -1);
                event.setBurnTime(value > 0 ? (int)(value * PNCConfig.Common.General.fuelBucketEfficiency) : -1);
            }
        });
    }

    @SubscribeEvent
    public void explosionCraftingEvent(ExplosionEvent.Detonate event) {
        if (!PNCConfig.Common.Recipes.explosionCrafting || event.getWorld().isRemote) {
            return;
        }

        Iterator<Entity> iterator = event.getAffectedEntities().iterator();
        while (iterator.hasNext()) {
            Entity entity = iterator.next();
            if (entity instanceof ItemEntity && entity.isAlive()) {
                ItemStack stack = ((ItemEntity) entity).getItem();
                if (!stack.isEmpty()) {
                    boolean firstItem = true;
                    for (ItemStack result : ExplosionCraftingRecipe.tryToCraft(stack)) {
                        if (firstItem) {
                            // first item in result: just replace the existing entity
                            ((ItemEntity) entity).setItem(result);
                            iterator.remove();
                            firstItem = false;
                        } else {
                            // subsequent items: add a new item entity
                            PneumaticCraftUtils.dropItemOnGround(result, event.getWorld(), entity.getPosition());
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
        if (!event.getWorld().isRemote) {
            if (event.getEntity() instanceof MobEntity) {
                ((MobEntity) event.getEntity()).goalSelector.addGoal(Integer.MIN_VALUE, new EntityAINoAIWhenRidingDrone((MobEntity) event.getEntity()));
            }
        } else {
            if (event.getEntity() instanceof PlayerEntity && event.getEntity().getEntityId() == ClientUtils.getClientPlayer().getEntityId()) {
                SemiBlockManager.getInstance(event.getWorld()).clearAll();
            }
        }
    }

    @SubscribeEvent
    public void onEntityConstruction(AttachCapabilitiesEvent<Entity> event) {
        event.addCapability(RL("hacking"), new CapabilityHacking.Provider());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEnderTeleport(EnderTeleportEvent event) {
        Entity e = event.getEntity();
        if (!HackableEnderman.onEndermanTeleport(e)) {
            event.setCanceled(true);
        }
        if (e.getEntityWorld().getBlockState(e.getPosition()).getBlock() == ModBlocks.FAKE_ICE.get()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onFillBucket(FillBucketEvent event) {
        RayTraceResult rtr = event.getTarget();
        if (rtr != null && rtr.getType() == RayTraceResult.Type.BLOCK) {
            BlockRayTraceResult brtr = (BlockRayTraceResult) rtr;
            Block b = event.getWorld().getBlockState(brtr.getPos()).getBlock();
            if (b instanceof FlowingFluidBlock) {
                Fluid fluid = ((FlowingFluidBlock) b).getFluid();
                if (TileEntityRefineryController.isInputFluidValid(fluid, 4) && event.getPlayer() instanceof ServerPlayerEntity) {
                    AdvancementTriggers.OIL_BUCKET.trigger((ServerPlayerEntity) event.getPlayer());
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerClick(PlayerInteractEvent event) {
        if (event instanceof PlayerInteractEvent.RightClickEmpty) return;

        ItemStack heldItem = event.getPlayer().getHeldItem(event.getHand());
        BlockState interactedBlockState = event.getWorld().getBlockState(event.getPos());
        Block interactedBlock = interactedBlockState.getBlock();

        if (!event.getPlayer().isCreative() || !event.getPlayer().getCommandSource().hasPermissionLevel(2)) {
            if (event.getWorld() != null && !event.getWorld().isRemote) {
                if (interactedBlock != ModBlocks.SECURITY_STATION.get() || event instanceof PlayerInteractEvent.LeftClickBlock) {
                    boolean tryingToPlaceSecurityStation = heldItem.getItem() instanceof BlockItem && ((BlockItem) heldItem.getItem()).getBlock() == ModBlocks.SECURITY_STATION.get();
                    int blockingStations = PneumaticCraftUtils.getProtectingSecurityStations(event.getWorld(), event.getPos(), event.getPlayer(), true, tryingToPlaceSecurityStation);
                    if (blockingStations > 0) {
                        event.setCanceled(true);
                        event.getPlayer().sendStatusMessage(
                                new TranslationTextComponent(
                                        tryingToPlaceSecurityStation ? "message.securityStation.stationPlacementPrevented" : "message.securityStation.accessPrevented",
                                        blockingStations), false);
                    }
                }
            }
        }

//        if (!event.isCanceled() && interactedBlock == Blocks.COBBLESTONE) {
//            AdvancementUtils.checkFor9x9(event.getEntityPlayer(), event.getPos());
//        }
    }

    @SubscribeEvent
    public void onModdedWrenchBlock(PlayerInteractEvent.RightClickBlock event) {
        BlockState state = event.getWorld().getBlockState(event.getPos());
        if (!event.isCanceled() && state.getBlock() instanceof IPneumaticWrenchable) {
            if (event.getHand() == Hand.OFF_HAND && ModdedWrenchUtils.getInstance().isModdedWrench(event.getPlayer().getHeldItem(Hand.MAIN_HAND))) {
                event.setCanceled(true);
            } else if (ModdedWrenchUtils.getInstance().isModdedWrench(event.getPlayer().getHeldItem(event.getHand()))) {
                if (event.getWorld().isRemote) {
                    NetworkHandler.sendToServer(new PacketModWrenchBlock(event.getPos(), event.getFace(), event.getHand()));
                }
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onModdedWrenchEntity(PlayerInteractEvent.EntityInteract event) {
        if (!event.isCanceled() && event.getTarget() instanceof IPneumaticWrenchable) {
            if (event.getHand() == Hand.OFF_HAND && ModdedWrenchUtils.getInstance().isModdedWrench(event.getPlayer().getHeldItem(Hand.MAIN_HAND))) {
                event.setCanceled(true);
            } else if (ModdedWrenchUtils.getInstance().isModdedWrench(event.getPlayer().getHeldItem(event.getHand()))) {
                if (event.getWorld().isRemote) {
                    NetworkHandler.sendToServer(new PacketModWrenchBlock(event.getPos(), event.getHand(), event.getTarget().getEntityId()));
                }
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void quetziMoo(ServerChatEvent event) {
        if (event.getUsername().equals("Quetzz") && event.getMessage().equals("m00")) {
            for (int i = 0; i < 4; i++)
                NetworkHandler.sendToPlayer(new PacketPlaySound(SoundEvents.ENTITY_COW_AMBIENT, SoundCategory.NEUTRAL, event.getPlayer().posX, event.getPlayer().posY, event.getPlayer().posZ, 1, 1, true), event.getPlayer());
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        World world = event.getWorld().getWorld();
        if (!world.isRemote) {
            ModuleNetworkManager.getInstance(world).invalidateCache();
        }
    }

    @SubscribeEvent
    public void onEntityTracking(EntityTrackEvent event) {
        if (event.trackingEntity instanceof EntityProgrammableController) event.setCanceled(true);
    }

    @SubscribeEvent
    public void onInventoryTracking(InventoryTrackEvent event) {
        if (event.getTileEntity() instanceof TileEntityProgrammer) event.setCanceled(true);
    }

    @SubscribeEvent
    public void onDroneSuicide(DroneSuicideEvent event) {
        if (event.drone instanceof EntityDrone) {
            EntityDrone drone = (EntityDrone) event.drone;
            AmadronOffer offer = drone.getHandlingOffer();
            if (offer != null) {
                int requiredCount = offer.getInput().getAmount() * drone.getOfferTimes();
                switch (offer.getInput().getType()) {
                    case ITEM:
                        for (int i = 0; i < drone.getInv().getSlots(); i++) {
                            requiredCount -= drone.getInv().getStackInSlot(i).getCount();
                        }
                        if (requiredCount <= 0) {
                            for (int i = 0; i < drone.getInv().getSlots(); i++) {
                                drone.getInv().setStackInSlot(i, ItemStack.EMPTY);
                            }
                            MinecraftForge.EVENT_BUS.post(new AmadronRetrievalEvent(event.drone));
                        }
                        break;
                    case FLUID:
                        if (drone.getFluidTank().getFluidAmount() >= requiredCount) {
                            MinecraftForge.EVENT_BUS.post(new AmadronRetrievalEvent(event.drone));
                        }
                        break;
                }
            }
        }
    }

    @SubscribeEvent
    public void onAmadronSuccess(AmadronRetrievalEvent event) {
        EntityDrone drone = (EntityDrone) event.drone;
        AmadronOffer offer = drone.getHandlingOffer();

        boolean shouldDeliver = false;
        if (offer instanceof AmadronOfferCustom) {
            boolean shouldSave = false;
            AmadronOffer realOffer = AmadronOfferManager.getInstance().get(offer);
            if (realOffer != null) {//If we find the non-inverted offer, that means the Drone just has completed trading with a different player.
                ((AmadronOfferCustom) realOffer).addPayment(drone.getOfferTimes());
                ((AmadronOfferCustom) realOffer).addStock(-drone.getOfferTimes());
                realOffer.onTrade(drone.getOfferTimes(), drone.getBuyingPlayer());
                shouldDeliver = true;
                shouldSave = true;
            }
            realOffer = AmadronOfferManager.getInstance().get(((AmadronOfferCustom) offer).copy().invert());
            if (realOffer != null) {//If we find the inverted offer, that means the Drone has just restocked.
                ((AmadronOfferCustom) realOffer).addStock(drone.getOfferTimes());
                shouldSave = true;
            }
            if (shouldSave) {
                try {
                    AmadronOfferStaticConfig.INSTANCE.writeToFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            shouldDeliver = true;
        }
        if (shouldDeliver) {
            ItemStack usedTablet = drone.getUsedTablet();
            switch (offer.getOutput().getType()) {
                case ITEM:
                    ItemStack offeringItems = offer.getOutput().getItem();
                    int producedItems = offeringItems.getCount() * drone.getOfferTimes();
                    List<ItemStack> stacks = new ArrayList<>();
                    while (producedItems > 0) {
                        ItemStack stack = offeringItems.copy();
                        stack.setCount(Math.min(producedItems, stack.getMaxStackSize()));
                        stacks.add(stack);
                        producedItems -= stack.getCount();
                    }
                    GlobalPos pos = ItemAmadronTablet.getItemProvidingLocation(usedTablet);
                    if (pos != null) {
                        DroneRegistry.getInstance().deliverItemsAmazonStyle(pos, stacks.toArray(new ItemStack[0]));
                    }
                    break;
                case FLUID:
                    FluidStack offeringFluid = offer.getOutput().getFluid().copy();
                    offeringFluid.setAmount(offeringFluid.getAmount() * drone.getOfferTimes());
                    GlobalPos fpos = ItemAmadronTablet.getFluidProvidingLocation(usedTablet);
                    if (fpos != null) {
                        DroneRegistry.getInstance().deliverFluidAmazonStyle(fpos, offeringFluid);
                    }
                    break;
            }
        }
    }

    @SubscribeEvent
    public void onLootTableLoad(LootTableLoadEvent event) {
        if (PNCConfig.Common.General.enableDungeonLoot) {
            String prefix = "minecraft:chests/";
            String name = event.getName().toString();
            if (name.startsWith(prefix)) {
                String file = name.substring(name.indexOf(prefix) + prefix.length());
                switch (file) {
                    case "abandoned_mineshaft":
                    case "desert_pyramid":
                    case "jungle_temple":
                    case "simple_dungeon":
                    case "spawn_bonus_chest":
                    case "stronghold_corridor":
                    case "village_blacksmith":
                        // todo 1.14 loot tables are in datapack
//                        ILootGenerator entry = new TableLootEntry(RL("inject/simple_dungeon_loot"), 1, 0,  new ILootCondition[0], "pneumaticcraft_inject_entry");
//                        LootPool pool = new LootPool(new ILootGenerator[]{entry}, new ILootCondition[0], new RandomValueRange(1), new RandomValueRange(0, 1), "pneumaticcraft_inject_pool");
//                        event.getTable().addPool(pool);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @SubscribeEvent
    public void onEquipmentChanged(LivingEquipmentChangeEvent event) {
        if (event.getEntityLiving() instanceof ServerPlayerEntity) {
            if (event.getSlot() == EquipmentSlotType.MAINHAND) {
                // tag the minigun with the player's entity ID - it's sync'd to clients
                // so other clients will know who's wielding it, and render appropriately
                // See RenderItemMinigun#renderByItem()
                if (event.getTo().getItem() == ModItems.MINIGUN.get()) {
                    NBTUtil.initNBTTagCompound(event.getTo());
                    event.getTo().getTag().putInt("owningPlayerId", event.getEntityLiving().getEntityId());
                } else if (event.getFrom().getItem() == ModItems.MINIGUN.get()) {
                    NBTUtil.initNBTTagCompound(event.getFrom());
                    event.getFrom().getTag().remove("owningPlayerId");
                }
            } else if (event.getSlot().getSlotType() == EquipmentSlotType.Group.ARMOR) {
                // trigger the "compressed iron man" advancement if wearing a full suit
                ServerPlayerEntity player = (ServerPlayerEntity) event.getEntityLiving();
                for (ItemStack stack : player.getArmorInventoryList()) {
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
            // prevent minecarts which have just been dropped by drones from immediately picking up the drone
            if (event.getEntityMounting() instanceof EntityDrone
                    && (event.getEntityBeingMounted() instanceof AbstractMinecartEntity || event.getEntityBeingMounted() instanceof BoatEntity)) {
                if (!event.getEntityBeingMounted().onGround) {
                    event.setCanceled(true);
                }
            }
        }
    }
}
