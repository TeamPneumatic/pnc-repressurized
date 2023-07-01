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

package me.desht.pneumaticcraft.common.item;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.data.PneumaticCraftTags;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.client.sound.MovingSounds;
import me.desht.pneumaticcraft.common.block.entity.ChargingStationBlockEntity;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModMenuTypes;
import me.desht.pneumaticcraft.common.inventory.AbstractPneumaticCraftMenu;
import me.desht.pneumaticcraft.common.inventory.JackhammerSetupMenu;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.item.DrillBitItem.DrillBitType;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlayMovingSound;
import me.desht.pneumaticcraft.common.network.PacketPlayMovingSound.MovingSoundFocus;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.*;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkHooks;
import org.apache.commons.lang3.mutable.MutableBoolean;

import javax.annotation.Nonnull;
import java.util.*;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class JackHammerItem extends PressurizableItem
        implements IChargeableContainerProvider, ColorHandlers.ITintableItem, IShiftScrollable {
    private static final float[] SPEED_MULT = new float[] {
            1f,
            2f,
            2.41421356237309f,
            2.73205080756888f,
            3f,
            3.23606797749979f,
            3.44948974278318f,
            3.64575131106459f,
            3.82842712474619f,
            4f,
            4.16227766016838f
    };
    private static final String NBT_DIG_MODE = "DigMode";

    private static long lastModeSwitchTime; // client-side: when player last scrolled to change mode

    public JackHammerItem() {
        super(ModItems.toolProps(), PneumaticValues.VOLUME_JACKHAMMER * 10, PneumaticValues.VOLUME_JACKHAMMER);
    }

    public static long getLastModeSwitchTime() {
        return lastModeSwitchTime;
    }

    public static DrillBitHandler getDrillBitHandler(ItemStack stack) {
        if (stack.getItem() instanceof JackHammerItem) {
            return new DrillBitHandler(stack);
        }
        return null;
    }

    public static EnchantmentHandler getEnchantmentHandler(ItemStack stack) {
        if (stack.getItem() instanceof JackHammerItem) {
            return new EnchantmentHandler(stack);
        }
        return null;
    }

    public DrillBitType getDrillBit(ItemStack stack) {
        DrillBitHandler handler = new DrillBitHandler(stack);
        return handler.getStackInSlot(0).getItem() instanceof DrillBitItem bit ? bit.getType() : DrillBitType.NONE;
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return TierSortingRegistry.isCorrectTierForDrops(getDrillBit(stack).getTier(), state);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        DrillBitType bitType = getDrillBit(stack);
        int speed = bitType == DrillBitType.NONE ? 0 : UpgradableItemUtils.getUpgradeCount(stack, ModUpgrades.SPEED.get());
        return getAir(stack) > 0f ? bitType.getBaseEfficiency() * SPEED_MULT[speed] : 1;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        if (!playerIn.isCrouching() || stack.getCount() != 1) return InteractionResultHolder.pass(stack);
        if (playerIn instanceof ServerPlayer sp) {
            NetworkHooks.openScreen(sp, new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return stack.getHoverName();
                }

                @Override
                public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
                    return new JackhammerSetupMenu(windowId, inv, handIn);
                }
            }, buf -> AbstractPneumaticCraftMenu.putHand(buf, handIn));
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        return getDrillBit(stack) != DrillBitType.NONE && getAir(stack) > 0f;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) {
        MutableBoolean didWork = new MutableBoolean(false);

        if (player instanceof ServerPlayer serverPlayer && !player.isShiftKeyDown()) {
            Level level = serverPlayer.getCommandSenderWorld();

            HitResult hitResult = RayTraceUtils.getEntityLookedObject(player, PneumaticCraftUtils.getPlayerReachDistance(player));
            if (hitResult instanceof BlockHitResult blockHitResult) {
                itemstack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).ifPresent(airHandler -> {
                    DigMode digMode = JackHammerItem.getDigMode(itemstack);

                    IntList upgrades = UpgradableItemUtils.getUpgradeList(itemstack, ModUpgrades.SPEED.get(), ModUpgrades.MAGNET.get());
                    int speed = upgrades.getInt(0);
                    boolean magnet = upgrades.getInt(1) > 0 && digMode.isVeinMining();

                    DrillBitType bitType = getDrillBit(itemstack);
                    if (TierSortingRegistry.getTiersLowerThan(digMode.getBitType().getTier()).contains(bitType.getTier())) {
                        // sanity check
                        digMode = DigMode.MODE_1X1;
                    }
                    Set<BlockPos> brokenPos = getBreakPositions(level, pos, blockHitResult.getDirection(), player.getDirection(), digMode);
                    brokenPos.remove(pos); // start pos already broken

                    float air = airHandler.getAir();
                    float air0 = air;
                    float usage = player.isCreative() ? 0f : PneumaticValues.USAGE_JACKHAMMER * SPEED_MULT[speed];
                    if (magnet) usage *= 1.1f;

                    if (air >= usage) {
                        didWork.setTrue();

                        // the first block (always mined)
                        air -= usage;

                        // any extra blocks, based on the dig mode
                        for (BlockPos pos1 : brokenPos) {
                            if (air < usage) break;

                            BlockState state1 = level.getBlockState(pos1);
                            if (state1.getDestroySpeed(level, pos1) < 0) continue;

                            int exp = ForgeHooks.onBlockBreakEvent(serverPlayer.level(), serverPlayer.gameMode.getGameModeForPlayer(), serverPlayer, pos1);
                            if (exp == -1) {
                                continue;
                            }
                            if (level.getBlockEntity(pos1) != null) {
                                continue;
                            }
                            Block block = state1.getBlock();
                            boolean removed = state1.onDestroyedByPlayer(level, pos1, player, true, level.getFluidState(pos1));
                            if (removed) {
                                block.destroy(level, pos1, state1);
                                if (magnet) {
                                    playerDestroyWithMagnet(block, level, player, pos, pos1, state1, itemstack);
                                } else {
                                    block.playerDestroy(level, player, pos1, state1, null, itemstack);
                                }
                                if (exp > 0 && level instanceof ServerLevel) {
                                    block.popExperience((ServerLevel) level, magnet ? pos : pos1, exp);
                                }
                                if (!player.isCreative()) {
                                    air -= usage;
                                }
                                player.awardStat(Stats.ITEM_USED.get(this));
                            }
                        }
                        if (air != air0 && !player.isCreative()) {
                            airHandler.addAir((int) (air - air0));
                        }
                    }
                });
            }
        }
        return !didWork.booleanValue();
    }

    // just like Block#playerDestroy, except all items are dropped in the same place (the block that was mined)
    private static void playerDestroyWithMagnet(Block block, Level level, Player player, BlockPos pos0, BlockPos pos, BlockState state, ItemStack stack) {
        player.awardStat(Stats.BLOCK_MINED.get(block));
        player.causeFoodExhaustion(0.005F);
        if (level instanceof ServerLevel serverLevel) {
            Block.getDrops(state, serverLevel, pos, null, player, stack)
                    .forEach((stackToSpawn) -> Block.popResource(level, pos0, stackToSpawn));
            state.spawnAfterBreak(serverLevel, pos, stack, true);
        }
    }

    public static Set<BlockPos> getBreakPositions(Level world, BlockPos pos, Direction dir, Direction playerHoriz, DigMode digMode) {
        if (digMode.isVeinMining()) {
            return new HashSet<>(getVeinPositions(world, pos, digMode));
        }

        Set<BlockPos> res = new HashSet<>();
        if (digMode.atLeast(DigMode.MODE_1X2)) {
            res.add(dir.getAxis() == Direction.Axis.Y ? pos.relative(playerHoriz) : pos.below());
        }
        if (digMode.atLeast(DigMode.MODE_1X3)) {
            res.add(dir.getAxis() == Direction.Axis.Y ? pos.relative(playerHoriz.getOpposite()) : pos.above());
        }
        if (digMode.atLeast(DigMode.MODE_3X3_CROSS)) {
            switch (dir.getAxis()) {
                case X -> {
                    res.add(pos.north());
                    res.add(pos.south());
                    res.add(pos.above());
                    res.add(pos.below());
                }
                case Y -> {
                    res.add(pos.north());
                    res.add(pos.south());
                    res.add(pos.west());
                    res.add(pos.east());
                }
                case Z -> {
                    res.add(pos.above());
                    res.add(pos.below());
                    res.add(pos.west());
                    res.add(pos.east());
                }
            }
        }
        if (digMode.atLeast(DigMode.MODE_3X3_FULL)) {
            switch (dir.getAxis()) {
                case X -> {
                    res.add(pos.above().north());
                    res.add(pos.above().south());
                    res.add(pos.below().north());
                    res.add(pos.below().south());
                }
                case Y -> {
                    res.add(pos.north().east());
                    res.add(pos.north().west());
                    res.add(pos.south().east());
                    res.add(pos.south().west());
                }
                case Z -> {
                    res.add(pos.above().east());
                    res.add(pos.above().west());
                    res.add(pos.below().east());
                    res.add(pos.below().west());
                }
            }
        }
        return res;
    }

    private static List<BlockPos> getVeinPositions(Level world, BlockPos startPos, DigMode mode) {
        BlockState state = world.getBlockState(startPos);

        if (!mode.okToVeinMine(state)) {
            return Collections.emptyList();
        }

        int maxRange = ConfigHelper.common().jackhammer.maxVeinMinerRange.get();
        int maxRangeSq = maxRange * maxRange;

        List<BlockPos> found = new ArrayList<>();
        found.add(startPos);
        Set<BlockPos> checked = new ObjectOpenHashSet<>();

        Block startBlock = state.getBlock();
        int maxBlocks = mode.getBlocksDug();

        // thanks due to Mekanism for this useful & efficient blockfinder algorithm which I shamelessly cribbed ;)
        for (int i = 0; i < found.size(); i++) {
            BlockPos blockPos = found.get(i);
            for (BlockPos pos : BlockPos.betweenClosed(blockPos.offset(-1, -1, -1), blockPos.offset(1, 1, 1))) {
                if (!checked.contains(pos)) {
                    if (mode == DigMode.MODE_VEIN_PLUS && startPos.distSqr(pos) > maxRangeSq) {
                        continue;
                    }
                    if (world.isLoaded(pos) && startBlock == world.getBlockState(pos).getBlock()) {
                        BlockPos pos1 = pos.immutable();
                        found.add(pos1);
                        checked.add(pos1);
                        if (found.size() > maxBlocks) {
                            return found;
                        }
                    }
                }
            }
        }
        return found;
    }

    @Override
    public MenuProvider getContainerProvider(ChargingStationBlockEntity te) {
        return new IChargeableContainerProvider.Provider(te, ModMenuTypes.CHARGING_JACKHAMMER.get());
    }

    @Override
    public int getTintColor(ItemStack stack, int tintIndex) {
        if (tintIndex == 0) {
            return getDrillBit(stack).getTint();
        }
        return 0xFFFFFFFF;
    }

    public static DigMode getDigMode(ItemStack stack) {
        if (stack.getItem() instanceof JackHammerItem && stack.hasTag()) {
            try {
                return Objects.requireNonNull(stack.getTag()).contains(NBT_DIG_MODE) ?
                        DigMode.valueOf(stack.getTag().getString(NBT_DIG_MODE)) :
                        DigMode.MODE_1X1;
            } catch (IllegalArgumentException ignored) {
            }
        }
        return DigMode.MODE_1X1;
    }

    public static void setDigMode(ItemStack stack, DigMode mode) {
        stack.getOrCreateTag().putString(NBT_DIG_MODE, mode.toString());
    }

    public static DigMode cycleDigMode(ItemStack stack, boolean forward) {
        if (stack.getItem() instanceof JackHammerItem) {
            DrillBitType ourBit = ((JackHammerItem) stack.getItem()).getDrillBit(stack);
            DigMode currentMode = getDigMode(stack);
            DigMode newMode = currentMode;
            if (forward) {
                if (currentMode == DigMode.MODE_VEIN_PLUS) {
                    newMode = DigMode.MODE_1X1;
                } else {
                    newMode = DigMode.values()[currentMode.ordinal() + 1];
                    newMode = newMode.getBitType().getBitQuality() <= ourBit.getBitQuality() ? newMode : DigMode.MODE_1X1;
                }
            } else {
                if (currentMode == DigMode.MODE_1X1) {
                    // search backward to find the highest dig mode our bit supports
                    for (int i = DigMode.values().length - 1; i >= 0; i--) {
                        if (DigMode.values()[i].getBitType().getBitQuality() <= ourBit.getBitQuality()) {
                            newMode = DigMode.values()[i];
                            break;
                        }
                    }
                } else {
                    newMode = DigMode.values()[currentMode.ordinal() - 1];
                }
            }

            setDigMode(stack, newMode);
            return newMode;
        }
        return null;
    }

    @Override
    public void onShiftScrolled(Player player, boolean forward, InteractionHand hand) {
        if (!player.level().isClientSide) {
            DigMode newMode = cycleDigMode(player.getItemInHand(hand), forward);
            if (newMode != null) {
                player.displayClientMessage(xlate("pneumaticcraft.message.jackhammer.mode")
                        .append(xlate(newMode.getTranslationKey()).withStyle(ChatFormatting.YELLOW)), true);
            }
        } else {
            lastModeSwitchTime = player.level().getGameTime();
        }
    }

    public enum DigMode implements ITranslatableEnum {
        MODE_1X1("1x1", 1, DrillBitType.IRON),
        MODE_1X2("1x2", 2, DrillBitType.COMPRESSED_IRON),
        MODE_1X3("1x3", 3, DrillBitType.COMPRESSED_IRON),
        MODE_3X3_CROSS("3x3_cross", 5, DrillBitType.DIAMOND),
        MODE_VEIN("vein", 128, DrillBitType.DIAMOND),
        MODE_3X3_FULL("3x3_full", 9, DrillBitType.DIAMOND),
        MODE_VEIN_PLUS("vein_plus", 128, DrillBitType.NETHERITE);

        private final String name;
        private final int blocksDug;
        private final DrillBitType bitType;

        DigMode(String name, int blocksDug, DrillBitType bitType) {
            this.name = name;
            this.blocksDug = blocksDug;
            this.bitType = bitType;
        }

        public String getName() {
            return name;
        }

        public DrillBitType getBitType() {
            return bitType;
        }

        public int getBlocksDug() {
            return blocksDug;
        }

        public ResourceLocation getGuiIcon() {
            return Textures.guiIconTexture("gui_" + name + ".png");
        }

        public boolean atLeast(DigMode type) {
            return type.ordinal() <= this.ordinal();
        }

        public boolean isVeinMining() { return this == DigMode.MODE_VEIN || this == DigMode.MODE_VEIN_PLUS; }

        public boolean okToVeinMine(BlockState state) {
            return switch (this) {
                case MODE_VEIN -> state.is(PneumaticCraftTags.Blocks.JACKHAMMER_ORES);
                case MODE_VEIN_PLUS -> true;
                default -> false;
            };
        }

        @Override
        public String getTranslationKey() {
            return "pneumaticcraft.message.jackhammer.mode." + name;
        }
    }

    public static class DrillBitHandler extends BaseItemStackHandler {
        private static final String NBT_DRILL_BIT = "DrillBit";

        private final ItemStack jackhammerStack;

        public DrillBitHandler(ItemStack jackhammerStack) {
            super(1);

            this.jackhammerStack = jackhammerStack;
            if (jackhammerStack.hasTag() && Objects.requireNonNull(jackhammerStack.getTag()).contains(NBT_DRILL_BIT, Tag.TAG_STRING)) {
                String name = jackhammerStack.getTag().getString(NBT_DRILL_BIT);
                try {
                    DrillBitType type = DrillBitType.valueOf(name);
                    setStackInSlot(0, type.asItemStack());
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        @Override
        public boolean isItemValid(int slot, ItemStack itemStack) {
            return itemStack.isEmpty() || itemStack.getItem() instanceof DrillBitItem;
        }

        public void save() {
            ItemStack bitStack = getStackInSlot(0);
            if (bitStack.getItem() instanceof DrillBitItem bit) {
                NBTUtils.setString(jackhammerStack, NBT_DRILL_BIT, bit.getType().toString());
            } else {
                NBTUtils.setString(jackhammerStack, NBT_DRILL_BIT, DrillBitType.NONE.toString());
            }
        }
    }

    public static class EnchantmentHandler extends BaseItemStackHandler {
        private final ItemStack jackhammerStack;

        public EnchantmentHandler(ItemStack jackhammerStack) {
            super(1);

            this.jackhammerStack = jackhammerStack;

            Map<Enchantment, Integer> ench = EnchantmentHelper.getEnchantments(jackhammerStack);
            for (Map.Entry<Enchantment, Integer> map : ench.entrySet()) {
                if (map.getKey() == Enchantments.SILK_TOUCH || map.getKey() == Enchantments.BLOCK_FORTUNE) {
                    ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
                    EnchantmentHelper.setEnchantments(Collections.singletonMap(map.getKey(), map.getValue()), book);
                    setStackInSlot(0, book);
                    break;
                }
            }
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return stack.isEmpty() || validateBook(stack);
        }

        public void save() {
            // replace any silk touch or fortune enchant, but leave any other enchants untouched
            ItemStack bookStack = getStackInSlot(0);
            Map<Enchantment, Integer> currentEnchants = EnchantmentHelper.getEnchantments(jackhammerStack);
            currentEnchants.remove(Enchantments.SILK_TOUCH);
            currentEnchants.remove(Enchantments.BLOCK_FORTUNE);
            if (validateBook(bookStack)) {
                currentEnchants.putAll(EnchantmentHelper.getEnchantments(bookStack));
            }
            EnchantmentHelper.setEnchantments(currentEnchants, jackhammerStack);
        }

        public static boolean validateBook(ItemStack bookStack) {
            // must be an enchanted book with Silk Touch or Fortune and nothing else
            if (bookStack.getItem() == Items.ENCHANTED_BOOK) {
                Map<Enchantment, Integer> ench = EnchantmentHelper.getEnchantments(bookStack);
                if (ench.size() != 1) return false;
                return ench.containsKey(Enchantments.BLOCK_FORTUNE) || ench.containsKey(Enchantments.SILK_TOUCH);
            }
            return false;
        }
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID)
    public static class Listener {
        @SubscribeEvent
        public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
            Player player = event.getEntity();
            ItemStack stack = player.getItemInHand(event.getHand());
            if (stack.getItem() instanceof JackHammerItem jackHammer && jackHammer.getAir(stack) > 0f) {
                if (event.getLevel().isClientSide) {
                    // play the sound to this player
                    MovingSounds.playMovingSound(MovingSounds.Sound.JACKHAMMER, event.getEntity());
                } else {
                    // play the sound to any other players tracking this player
                    NetworkHandler.sendToAllTracking(new PacketPlayMovingSound(MovingSounds.Sound.JACKHAMMER, MovingSoundFocus.of(player)), player);
                }
            }
        }
    }
}
