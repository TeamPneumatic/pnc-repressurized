package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.common.XPFluidManager;
import me.desht.pneumaticcraft.common.capabilities.FluidItemWrapper;
import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketLeftClickEmpty;
import me.desht.pneumaticcraft.common.thirdparty.curios.Curios;
import me.desht.pneumaticcraft.common.util.EnchantmentUtils;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.util.ITooltipFlag;
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
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        super(ModItems.defaultProps());
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        if (!worldIn.isRemote) {
            stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).ifPresent(handler -> {
                int ratio = XPFluidManager.getInstance().getXPRatio(ModFluids.MEMORY_ESSENCE.get());
                int playerXp = EnchantmentUtils.getPlayerXP(playerIn);
                if (playerIn.isSneaking()) {
                    // take XP fluid from the stick and give to player
                    int xpToGive = EnchantmentUtils.getExperienceForLevel(playerIn.experienceLevel + 1) - playerXp;
                    int fluidAmount = xpToGive * ratio;
                    FluidStack toDrain = handler.drain(fluidAmount, IFluidHandler.FluidAction.SIMULATE);
                    if (!toDrain.isEmpty()) {
                        EnchantmentUtils.addPlayerXP(playerIn, toDrain.getAmount() / ratio);
                        handler.drain(toDrain.getAmount(), IFluidHandler.FluidAction.EXECUTE);
                        playerIn.setHeldItem(handIn, handler.getContainer());
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
                            playerIn.setHeldItem(handIn, handler.getContainer());
                        }
                    }
                }
            });
        } else {
            stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).ifPresent(handler -> {
                int amount = handler.getFluidInTank(0).getAmount();
                if (EnchantmentUtils.getPlayerXP(playerIn) > 0 && amount < handler.getTankCapacity(0) && !playerIn.isSneaking()
                        || handler.getFluidInTank(0).getAmount() > 0 && playerIn.isSneaking()) {
                    playerIn.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.1f,
                            (worldIn.rand.nextFloat() - worldIn.rand.nextFloat()) * 0.35F + 0.9F);
                }
            });
        }
        return ActionResult.resultSuccess(stack);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        if (worldIn != null) {
            stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).ifPresent(handler -> {
                int ratio = XPFluidManager.getInstance().getXPRatio(ModFluids.MEMORY_ESSENCE.get());
                FluidStack fluidStack = handler.getFluidInTank(0);
                int amount = fluidStack.getAmount();
                int levels = EnchantmentUtils.getLevelForExperience(amount/ ratio);
                tooltip.add(new TranslationTextComponent("pneumaticcraft.gui.tooltip.memory_stick.xp_stored", amount / ratio, levels).mergeStyle(TextFormatting.GREEN));
            });
            boolean absorb = shouldAbsorbXPOrbs(stack);
            tooltip.add(new TranslationTextComponent("pneumaticcraft.message.memory_stick.absorb." + absorb).mergeStyle(TextFormatting.YELLOW));
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
        if (this.getClass() == ItemMemoryStick.class) {
            return new FluidItemWrapper(stack, TANK_NAME, XP_FLUID_CAPACITY, fluid -> fluid == ModFluids.MEMORY_ESSENCE.get());
        } else {
            return super.initCapabilities(stack, nbt);
        }
    }

    public static boolean shouldAbsorbXPOrbs(ItemStack stack) {
        return stack.getItem() == ModItems.MEMORY_STICK.get() && stack.hasTag() && stack.getTag().getBoolean(NBT_ABSORB_ORBS);
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
        toggleXPAbsorption(sender, sender.getHeldItemMainhand());
    }

    private static void toggleXPAbsorption(PlayerEntity player, ItemStack stack) {
        if (stack.getItem() instanceof ItemMemoryStick) {
            boolean absorb = shouldAbsorbXPOrbs(stack);
            setAbsorbXPOrbs(stack, !absorb);
            player.sendStatusMessage(new TranslationTextComponent("pneumaticcraft.message.memory_stick.absorb." + !absorb).mergeStyle(TextFormatting.YELLOW), true);
            player.getEntityWorld().playSound(null, player.getPosition(), SoundEvents.BLOCK_NOTE_BLOCK_CHIME, SoundCategory.PLAYERS, 1f, absorb ? 1.5f : 2f);
        }
    }

    @Mod.EventBusSubscriber
    public static class Listener {
        private static final Map<PlayerEntity, Long> lastEvent = new HashMap<>();
        private static final Map<PlayerEntity, Pair<String,Integer>> memoryStickCache = new HashMap<>();

        @SubscribeEvent
        public static void onLeftClick(PlayerInteractEvent.LeftClickBlock event) {
            if (event.getItemStack().getItem() instanceof ItemMemoryStick) {
                if (!event.getWorld().isRemote) {
                    long now = event.getWorld().getGameTime();
                    long last = lastEvent.getOrDefault(event.getPlayer(), 0L);
                    if (now - last > 5) {
                        toggleXPAbsorption(event.getPlayer(), event.getItemStack());
                        lastEvent.put(event.getPlayer(), now);
                    }
                }
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
            // client only event, but let's be paranoid...
            if (event.getWorld().isRemote && event.getItemStack().getItem() instanceof ItemMemoryStick) {
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
            Pair<String,Integer> p = memoryStickCache.get(player);
            ItemStack stack = p == null ? ItemStack.EMPTY : getMemoryStick(player, p.getLeft(), p.getRight());
            if (!shouldAbsorbXPOrbs(stack)) {
                memoryStickCache.remove(player);
                for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                    ItemStack stack1 = player.inventory.getStackInSlot(i);
                    if (stack1.getItem() == ModItems.MEMORY_STICK.get() && shouldAbsorbXPOrbs(stack1)) {
                        stack = stack1;
                        memoryStickCache.put(player, Pair.of("", i));
                        break;
                    }
                }
                if (stack.isEmpty() && Curios.available) {
                    Pair<String,Integer> p1 = Curios.findStack(player, ItemMemoryStick::shouldAbsorbXPOrbs);
                    if (p1 == Curios.NONE) {
                        stack = ItemStack.EMPTY;
                    } else {
                        stack = Curios.getStack(player, p1.getKey(), p1.getValue());
                        memoryStickCache.put(player, Pair.of(p1.getKey(), p1.getValue()));
                    }
                }
            }
            return stack;
        }

        @Nonnull
        private static ItemStack getMemoryStick(PlayerEntity player, String inv, int slot) {
            if (inv.isEmpty()) {
                return player.inventory.getStackInSlot(slot);
            } else if (Curios.available) {
                return Curios.getStack(player, inv, slot);
            }
            return ItemStack.EMPTY;
        }
    }
}
