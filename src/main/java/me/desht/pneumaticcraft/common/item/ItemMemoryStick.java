package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.common.XPFluidManager;
import me.desht.pneumaticcraft.common.capabilities.FluidItemWrapper;
import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketLeftClickEmpty;
import me.desht.pneumaticcraft.common.thirdparty.curios.Curios;
import me.desht.pneumaticcraft.common.thirdparty.curios.CuriosUtils;
import me.desht.pneumaticcraft.common.util.EnchantmentUtils;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class ItemMemoryStick extends Item implements ColorHandlers.ITintableItem, ILeftClickableItem {
    private static final String TANK_NAME = "Tank";
    private static final String NBT_ABSORB_ORBS = "AbsorbXPOrbs";
    private static final int XP_FLUID_CAPACITY = 512000;
    private static final int[] TINT_COLORS = new int[] {
                0xf7ffbf,
                0xf2ff99,
                0xedff73,
                0xe8ff4d,
                0xe3ff26,
                0xdeff00
    };

    public ItemMemoryStick() {
        super(ModItems.defaultProps().stacksTo(1));
    }

    @Override
    public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        if (stack.getCount() != 1) return ActionResult.pass(stack);

        if (!worldIn.isClientSide) {
            stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).ifPresent(handler -> {
                int ratio = XPFluidManager.getInstance().getXPRatio(ModFluids.MEMORY_ESSENCE.get());
                int playerXp = EnchantmentUtils.getPlayerXP(playerIn);
                if (playerIn.isShiftKeyDown()) {
                    // take XP fluid from the stick and give to player
                    int xpToGive = EnchantmentUtils.getExperienceForLevel(playerIn.experienceLevel + 1) - playerXp;
                    int fluidAmount = xpToGive * ratio;
                    FluidStack toDrain = handler.drain(fluidAmount, IFluidHandler.FluidAction.SIMULATE);
                    if (!toDrain.isEmpty()) {
                        EnchantmentUtils.addPlayerXP(playerIn, toDrain.getAmount() / ratio);
                        handler.drain(toDrain.getAmount(), IFluidHandler.FluidAction.EXECUTE);
                        playerIn.setItemInHand(handIn, handler.getContainer());
                    }
                } else {
                    if (playerXp > 0) {
                        // take XP from player and fill the stick
                        int xpToTake = playerXp - EnchantmentUtils.getExperienceForLevel(playerIn.experienceLevel);
                        if (xpToTake == 0) {
                            xpToTake = playerXp - EnchantmentUtils.getExperienceForLevel(playerIn.experienceLevel - 1);
                        }
                        int fluidAmount = xpToTake * ratio;
                        FluidStack toFill = new FluidStack(ModFluids.MEMORY_ESSENCE.get(), fluidAmount);
                        int filled = handler.fill(toFill, IFluidHandler.FluidAction.SIMULATE);
                        if (filled >= ratio) {
                            EnchantmentUtils.addPlayerXP(playerIn, -(filled / ratio));
                            handler.fill(new FluidStack(ModFluids.MEMORY_ESSENCE.get(), filled), IFluidHandler.FluidAction.EXECUTE);
                            playerIn.setItemInHand(handIn, handler.getContainer());
                        }
                    }
                }
            });
        } else {
            stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).ifPresent(handler -> {
                int amount = handler.getFluidInTank(0).getAmount();
                if (EnchantmentUtils.getPlayerXP(playerIn) > 0 && amount < handler.getTankCapacity(0) && !playerIn.isShiftKeyDown()
                        || handler.getFluidInTank(0).getAmount() > 0 && playerIn.isShiftKeyDown()) {
                    playerIn.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.1f,
                            (worldIn.random.nextFloat() - worldIn.random.nextFloat()) * 0.35F + 0.9F);
                }
            });
        }
        return ActionResult.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        if (worldIn != null) {
            stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).ifPresent(handler -> {
                int ratio = XPFluidManager.getInstance().getXPRatio(ModFluids.MEMORY_ESSENCE.get());
                FluidStack fluidStack = handler.getFluidInTank(0);
                int amount = fluidStack.getAmount();
                int levels = EnchantmentUtils.getLevelForExperience(amount/ ratio);
                tooltip.add(new TranslationTextComponent("pneumaticcraft.gui.tooltip.memory_stick.xp_stored", amount / ratio, levels).withStyle(TextFormatting.GREEN));
            });
            boolean absorb = shouldAbsorbXPOrbs(stack);
            tooltip.add(new TranslationTextComponent("pneumaticcraft.message.memory_stick.absorb." + absorb).withStyle(TextFormatting.YELLOW));
        }
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).map(handler -> {
            FluidStack fluidStack = handler.getFluidInTank(0);
            return 1d - ((double)fluidStack.getAmount() / (double) handler.getTankCapacity(0));
        }).orElse(1d);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return new FluidItemWrapper(stack, TANK_NAME, XP_FLUID_CAPACITY, fluid -> fluid == ModFluids.MEMORY_ESSENCE.get());
    }

    public static boolean shouldAbsorbXPOrbs(ItemStack stack) {
        return stack.getItem() == ModItems.MEMORY_STICK.get() && stack.getCount() == 1 && stack.hasTag() && stack.getTag().getBoolean(NBT_ABSORB_ORBS);
    }

    public static void setAbsorbXPOrbs(ItemStack stack, boolean absorb) {
        if (stack.getItem() == ModItems.MEMORY_STICK.get()) {
            stack.getOrCreateTag().putBoolean(NBT_ABSORB_ORBS, absorb);
        }
    }

    @Override
    public int getTintColor(ItemStack stack, int tintIndex) {
        switch (tintIndex) {
            case 1:
                return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).map(handler -> {
                    FluidStack fluidStack = handler.getFluidInTank(0);
                    if (fluidStack.isEmpty()) return 0xFFFFFF;
                    float f = (float) fluidStack.getAmount() / (float) handler.getTankCapacity(0);
                    return TINT_COLORS[(int)(f * 5)];
                }).orElse(0xFFFFFFFF);
            case 2:
                return shouldAbsorbXPOrbs(stack) ? 0xFF00FF00 : 0xFF808080;
            default:
                return 0xFFFFFFFF;
        }
    }

    @Override
    public void onLeftClickEmpty(ServerPlayerEntity sender) {
        toggleXPAbsorption(sender, sender.getMainHandItem());
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (shouldAbsorbXPOrbs(stack) && entityIn instanceof PlayerEntity && itemSlot >= 0) {
            cacheMemoryStickLocation((PlayerEntity) entityIn, MemoryStickLocator.playerInv(itemSlot));
        }
    }

    public static boolean isRoomInStick(ItemStack stick) {
        return stick.getItem() instanceof ItemMemoryStick && stick.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
                .map(h -> h.getFluidInTank(0).getAmount() < h.getTankCapacity(0))
                .orElseThrow(RuntimeException::new);
    }

    private static void toggleXPAbsorption(PlayerEntity player, ItemStack stack) {
        if (stack.getItem() instanceof ItemMemoryStick) {
            boolean absorb = shouldAbsorbXPOrbs(stack);
            setAbsorbXPOrbs(stack, !absorb);
            player.displayClientMessage(new TranslationTextComponent("pneumaticcraft.message.memory_stick.absorb." + !absorb).withStyle(TextFormatting.YELLOW), true);
            player.getCommandSenderWorld().playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_CHIME, SoundCategory.PLAYERS, 1f, absorb ? 1.5f : 2f);
        }
    }

    public static void cacheMemoryStickLocation(PlayerEntity entityIn, MemoryStickLocator locator) {
        Listener.memoryStickCache.computeIfAbsent(entityIn.getUUID(), k -> new HashSet<>()).add(locator);
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID)
    public static class Listener {
        private static final Map<UUID, Long> lastEvent = new HashMap<>();
        private static final Map<UUID, Set<MemoryStickLocator>> memoryStickCache = new HashMap<>();

        @SubscribeEvent
        public static void onLeftClick(PlayerInteractEvent.LeftClickBlock event) {
            if (event.getItemStack().getItem() instanceof ItemMemoryStick) {
                if (!event.getWorld().isClientSide) {
                    long now = event.getWorld().getGameTime();
                    long last = lastEvent.getOrDefault(event.getPlayer().getUUID(), 0L);
                    if (now - last > 5) {
                        toggleXPAbsorption(event.getPlayer(), event.getItemStack());
                        lastEvent.put(event.getPlayer().getUUID(), now);
                    }
                }
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
            // client only event, but let's be paranoid...
            if (event.getWorld().isClientSide && event.getItemStack().getItem() instanceof ItemMemoryStick) {
                NetworkHandler.sendToServer(new PacketLeftClickEmpty());
            }
        }

        @SubscribeEvent
        public static void onXpOrbPickup(PlayerXpEvent.PickupXp event) {
            ItemStack stack = findMemoryStick(event.getPlayer());
            if (!stack.isEmpty()) {
                stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).ifPresent(handler -> {
                    if (PneumaticCraftUtils.fillTankWithOrb(handler, event.getOrb(), IFluidHandler.FluidAction.EXECUTE)) {
                        // orb's xp can fit in the memory stick: remove the entity, cancel the event
                        stack.setTag(handler.getContainer().getTag());
                        event.getOrb().remove();
                        event.setCanceled(true);
                    }
                });
            }
        }

        private static ItemStack findMemoryStick(PlayerEntity player) {
            Set<MemoryStickLocator> locators = memoryStickCache.get(player.getUUID());
            if (locators == null || locators.isEmpty()) return ItemStack.EMPTY;

            locators.removeIf(loc -> !shouldAbsorbXPOrbs(loc.getMemoryStick(player))); // prune old entries

            // use first suitable memory stick in inventory (xp absorb switched on, not full)
            return locators.stream()
                    .map(loc -> loc.getMemoryStick(player))
                    .filter(ItemMemoryStick::isRoomInStick)
                    .findFirst()
                    .orElse(ItemStack.EMPTY);
        }
    }

    public static class MemoryStickLocator {
        final String invName; // empty string for player inv, curio inv identifier for curios inv
        final int slot;

        private MemoryStickLocator(@Nonnull String invName, int slot) {
            Validate.notNull(invName);
            Validate.isTrue(slot >= 0);
            this.invName = invName;
            this.slot = slot;
        }

        public static MemoryStickLocator playerInv(int slot) {
            return new MemoryStickLocator("", slot);
        }

        public static MemoryStickLocator namedInv(String name, int slot) {
            return new MemoryStickLocator(name, slot);
        }

        public ItemStack getMemoryStick(PlayerEntity player) {
            if (invName.isEmpty()) {
                return player.inventory.getItem(slot);
            } else if (Curios.available) {
                return CuriosUtils.getStack(player, invName, slot);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MemoryStickLocator)) return false;
            MemoryStickLocator that = (MemoryStickLocator) o;
            return slot == that.slot && invName.equals(that.invName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(invName, slot);
        }
    }
}
