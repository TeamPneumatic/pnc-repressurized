package me.desht.pneumaticcraft.common.event;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.block.IPneumaticWrenchable;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.EntityTrackEvent;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.InventoryTrackEvent;
import me.desht.pneumaticcraft.api.drone.AmadronRetrievalEvent;
import me.desht.pneumaticcraft.api.drone.DroneConstructingEvent;
import me.desht.pneumaticcraft.api.drone.DroneSuicideEvent;
import me.desht.pneumaticcraft.common.DroneRegistry;
import me.desht.pneumaticcraft.common.PneumaticCraftAPIHandler;
import me.desht.pneumaticcraft.common.advancements.AdvancementTriggers;
import me.desht.pneumaticcraft.common.ai.EntityAINoAIWhenRidingDrone;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.block.tubes.ModuleNetworkManager;
import me.desht.pneumaticcraft.common.capabilities.hacking.CapabilityHackingProvider;
import me.desht.pneumaticcraft.common.config.AmadronOfferStaticConfig;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.entity.EntityProgrammableController;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.hacking.entity.HackableEnderman;
import me.desht.pneumaticcraft.common.item.ItemAmadronTablet;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.network.PacketRotateBlock;
import me.desht.pneumaticcraft.common.recipes.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferCustom;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferManager;
import me.desht.pneumaticcraft.common.recipes.ExplosionCraftingRecipe;
import me.desht.pneumaticcraft.common.remote.GlobalVariableManager;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import me.desht.pneumaticcraft.common.thirdparty.ModdedWrenchUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import me.desht.pneumaticcraft.common.tileentity.TileEntityRefinery;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootEntryTable;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraftforge.common.DimensionManager;
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
import net.minecraftforge.fluids.*;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class EventHandlerPneumaticCraft {

    @SubscribeEvent
    public void handleFuelEvent(FurnaceFuelBurnTimeEvent event) {
        FluidStack fluidStack = FluidUtil.getFluidContained(event.getItemStack());
        if (fluidStack != null && Names.MOD_ID.equals(FluidRegistry.getModId(fluidStack))) {
            int value = PneumaticCraftAPIHandler.getInstance().liquidFuels.getOrDefault(fluidStack.getFluid().getName(), -1);
            event.setBurnTime(value > 0 ? (int)(value * ConfigHandler.general.fuelBucketEfficiencyMultiplier) : -1);
        }
    }

    @SubscribeEvent
    public void explosionCraftingEvent(ExplosionEvent.Detonate event) {
        if (!ConfigHandler.general.explosionCrafting) {
            return;
        }

        Iterator<Entity> iterator = event.getAffectedEntities().iterator();
        while (iterator.hasNext()) {
            Entity entity = iterator.next();
            if (entity instanceof EntityItem && !entity.isDead) {
                ItemStack stack = ((EntityItem) entity).getItem();
                if (!stack.isEmpty()) {
                    ItemStack result = ExplosionCraftingRecipe.tryToCraft(stack);
                    if (!result.isEmpty()) {
                        ((EntityItem) entity).setItem(result);
                        iterator.remove();
                        checkForAdvancement(event, result);
                    }
                }
            }
        }
    }

    private void checkForAdvancement(ExplosionEvent.Detonate event, ItemStack result) {
        if (!event.getWorld().isRemote
                && (result.getItem() == Itemss.INGOT_IRON_COMPRESSED || result.getItem() == Item.getItemFromBlock(Blockss.COMPRESSED_IRON))) {
            Vec3d exp = event.getExplosion().getPosition();
            for (EntityPlayer player : event.getWorld().getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(exp.x - 32, exp.y - 32, exp.z - 32, exp.x + 32, exp.y + 32, exp.z + 32))) {
                AdvancementTriggers.EXPLODE_IRON.trigger((EntityPlayerMP) player);
            }
        }
    }

    @SubscribeEvent
    public void onEntityConstruction(EntityConstructing event) {
        if (event.getEntity() instanceof IDroneBase) {
            MinecraftForge.EVENT_BUS.post(new DroneConstructingEvent((IDroneBase) event.getEntity()));
        }
    }
    
    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (!event.getWorld().isRemote) {
            if (event.getEntity() instanceof EntityLiving) {
                ((EntityLiving) event.getEntity()).tasks.addTask(Integer.MIN_VALUE, new EntityAINoAIWhenRidingDrone((EntityLiving) event.getEntity()));
            }
        } else {
            if (event.getEntity() instanceof EntityPlayer && event.getEntity().getEntityId() == PneumaticCraftRepressurized.proxy.getClientPlayer().getEntityId()) {
                SemiBlockManager.getInstance(event.getWorld()).clearAll();
            }
        }
    }

    @SubscribeEvent
    public void onEntityConstruction(AttachCapabilitiesEvent<Entity> event) {
        event.addCapability(RL("hacking"), new CapabilityHackingProvider());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEnderTeleport(EnderTeleportEvent event) {
        Entity e = event.getEntity();
        if (!HackableEnderman.onEndermanTeleport(e)) {
            event.setCanceled(true);
        }
        if (e.getEntityWorld().getBlockState(e.getPosition()).getBlock() == Blockss.FAKE_ICE) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onFillBucket(FillBucketEvent event) {
        RayTraceResult rtr = event.getTarget();
        if (rtr != null) {
            Block b = event.getWorld().getBlockState(rtr.getBlockPos()).getBlock();
            if (b instanceof IFluidBlock) {
                Fluid fluid = ((IFluidBlock) b).getFluid();
                if (TileEntityRefinery.isInputFluidValid(fluid, 4) && event.getEntityPlayer() instanceof EntityPlayerMP) {
                    AdvancementTriggers.OIL_BUCKET.trigger((EntityPlayerMP) event.getEntityPlayer());
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerClick(PlayerInteractEvent event) {
        if (event instanceof PlayerInteractEvent.RightClickEmpty) return;

        ItemStack heldItem = event.getEntityPlayer().getHeldItem(event.getHand());
        IBlockState interactedBlockState = event.getWorld().getBlockState(event.getPos());
        Block interactedBlock = interactedBlockState.getBlock();

        if (!event.getEntityPlayer().capabilities.isCreativeMode || !event.getEntityPlayer().canUseCommand(2, "securityStation")) {
            if (event.getWorld() != null && !event.getWorld().isRemote) {
                if (interactedBlock != Blockss.SECURITY_STATION || event instanceof PlayerInteractEvent.LeftClickBlock) {
                    boolean tryingToPlaceSecurityStation = heldItem.getItem() instanceof ItemBlock && ((ItemBlock) heldItem.getItem()).getBlock() == Blockss.SECURITY_STATION;
                    int blockingStations = TileEntitySecurityStation.getProtectingSecurityStations(event.getWorld(), event.getPos(), event.getEntityPlayer(), true, tryingToPlaceSecurityStation);
                    if (blockingStations > 0) {
                        event.setCanceled(true);
                        event.getEntityPlayer().sendStatusMessage(
                                new TextComponentTranslation(
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
        IBlockState state = event.getWorld().getBlockState(event.getPos());
        if (!event.isCanceled() && state.getBlock() instanceof IPneumaticWrenchable) {
            if (event.getHand() == EnumHand.OFF_HAND && ModdedWrenchUtils.getInstance().isModdedWrench(event.getEntityPlayer().getHeldItem(EnumHand.MAIN_HAND))) {
                event.setCanceled(true);
            } else if (ModdedWrenchUtils.getInstance().isModdedWrench(event.getEntityPlayer().getHeldItem(event.getHand()))) {
                if (event.getWorld().isRemote) {
                    NetworkHandler.sendToServer(new PacketRotateBlock(event.getPos(), event.getFace(), event.getHand()));
                }
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onModdedWrenchEntity(PlayerInteractEvent.EntityInteract event) {
        if (!event.isCanceled() && event.getTarget() instanceof IPneumaticWrenchable) {
            if (event.getHand() == EnumHand.OFF_HAND && ModdedWrenchUtils.getInstance().isModdedWrench(event.getEntityPlayer().getHeldItem(EnumHand.MAIN_HAND))) {
                event.setCanceled(true);
            } else if (ModdedWrenchUtils.getInstance().isModdedWrench(event.getEntityPlayer().getHeldItem(event.getHand()))) {
                if (event.getWorld().isRemote) {
                    NetworkHandler.sendToServer(new PacketRotateBlock(event.getPos(), event.getHand(), event.getTarget().getEntityId()));
                }
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void quetziMoo(ServerChatEvent event) {
        if (event.getUsername().equals("Quetzz") && event.getMessage().equals("m00")) {
            for (int i = 0; i < 4; i++)
                NetworkHandler.sendTo(new PacketPlaySound(SoundEvents.ENTITY_COW_AMBIENT, SoundCategory.NEUTRAL, event.getPlayer().posX, event.getPlayer().posY, event.getPlayer().posZ, 1, 1, true), event.getPlayer());
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (!event.getWorld().isRemote) {
            if (event.getWorld().provider.getDimension() == 0) {
                GlobalVariableManager.overworld = event.getWorld();
                event.getWorld().loadData(GlobalVariableManager.class, GlobalVariableManager.DATA_KEY);
            }
            ModuleNetworkManager.getInstance(event.getWorld()).invalidateCache();
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
                int times = drone.getOfferTimes();
                if (offer.getInput() instanceof ItemStack) {
                    int requiredCount = ((ItemStack) offer.getInput()).getCount() * times;
                    for (int i = 0; i < drone.getInv().getSlots(); i++) {
                        requiredCount -= drone.getInv().getStackInSlot(i).getCount();
                    }
                    if (requiredCount <= 0) {
                        for (int i = 0; i < drone.getInv().getSlots(); i++) {
                            drone.getInv().setStackInSlot(i, ItemStack.EMPTY);
                        }
                        MinecraftForge.EVENT_BUS.post(new AmadronRetrievalEvent(event.drone));
                    }
                } else {
                    int requiredCount = ((FluidStack) offer.getInput()).amount * times;
                    if (drone.getTank().getFluidAmount() >= requiredCount) {
                        MinecraftForge.EVENT_BUS.post(new AmadronRetrievalEvent(event.drone));
                    }
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
            if (offer.getOutput() instanceof ItemStack) {
                ItemStack offeringItems = (ItemStack) offer.getOutput();
                int producedItems = offeringItems.getCount() * drone.getOfferTimes();
                List<ItemStack> stacks = new ArrayList<>();
                while (producedItems > 0) {
                    ItemStack stack = offeringItems.copy();
                    stack.setCount(Math.min(producedItems, stack.getMaxStackSize()));
                    stacks.add(stack);
                    producedItems -= stack.getCount();
                }
                BlockPos pos = ItemAmadronTablet.getItemProvidingLocation(usedTablet);
                if (pos != null) {
                    World world = DimensionManager.getWorld(ItemAmadronTablet.getItemProvidingDimension(usedTablet));
                    DroneRegistry.getInstance().deliverItemsAmazonStyle(world, pos, stacks.toArray(new ItemStack[0]));
                }
            } else {
                FluidStack offeringFluid = ((FluidStack) offer.getOutput()).copy();
                offeringFluid.amount *= drone.getOfferTimes();
                BlockPos pos = ItemAmadronTablet.getLiquidProvidingLocation(usedTablet);
                if (pos != null) {
                    World world = DimensionManager.getWorld(ItemAmadronTablet.getLiquidProvidingDimension(usedTablet));
                    DroneRegistry.getInstance().deliverFluidAmazonStyle(world, pos, offeringFluid);
                }
            }
        }
    }

    @SubscribeEvent
    public void onLootTableLoad(LootTableLoadEvent event) {
        if (ConfigHandler.general.enableDungeonLoot) {
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
                        LootEntry entry = new LootEntryTable(RL("inject/simple_dungeon_loot"), 1, 0,  new LootCondition[0], "pneumaticcraft_inject_entry");
                        LootPool pool = new LootPool(new LootEntry[]{entry}, new LootCondition[0], new RandomValueRange(1), new RandomValueRange(0, 1), "pneumaticcraft_inject_pool");
                        event.getTable().addPool(pool);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @SubscribeEvent
    public void onEquipmentChanged(LivingEquipmentChangeEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayerMP) {
            if (event.getSlot() == EntityEquipmentSlot.MAINHAND) {
                // tag the minigun with the player's entity ID - it's sync'd to clients
                // so other clients will know who's wielding it, and render appropriately
                // See RenderItemMinigun#renderByItem()
                if (event.getTo().getItem() == Itemss.MINIGUN) {
                    NBTUtil.initNBTTagCompound(event.getTo());
                    event.getTo().getTagCompound().setInteger("owningPlayerId", event.getEntityLiving().getEntityId());
                } else if (event.getFrom().getItem() == Itemss.MINIGUN) {
                    NBTUtil.initNBTTagCompound(event.getFrom());
                    event.getFrom().getTagCompound().removeTag("owningPlayerId");
                }
            } else if (event.getSlot().getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
                // trigger the "compressed iron man" advancement if wearing a full suit
                EntityPlayerMP player = (EntityPlayerMP) event.getEntityLiving();
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
                    && (event.getEntityBeingMounted() instanceof EntityMinecart || event.getEntityBeingMounted() instanceof EntityBoat)) {
                if (!event.getEntityBeingMounted().onGround) {
                    event.setCanceled(true);
                }
            }
        }
    }
}
