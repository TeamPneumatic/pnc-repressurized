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
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.data.PneumaticCraftTags;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.misc.ITranslatableEnum;
import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.client.sound.MovingSounds;
import me.desht.pneumaticcraft.common.block.entity.utility.ChargingStationBlockEntity;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.inventory.AbstractPneumaticCraftMenu;
import me.desht.pneumaticcraft.common.inventory.JackhammerSetupMenu;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.item.DrillBitItem.DrillBitType;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlayMovingSound;
import me.desht.pneumaticcraft.common.network.PacketPlayMovingSound.MovingSoundFocus;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.upgrades.UpgradableItemUtils;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.RayTraceUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

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

    private static final Set<ItemAbility> JACKHAMMER_ABILITIES = Set.of(
            ItemAbilities.AXE_DIG,
            ItemAbilities.PICKAXE_DIG,
            ItemAbilities.SHOVEL_DIG,
            ItemAbilities.SWORD_DIG
    );

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

    public static DrillBitType getDrillBit(ItemStack stack) {
        DrillBitHandler handler = new DrillBitHandler(stack);
        return handler.getStackInSlot(0).getItem() instanceof DrillBitItem bit ? bit.getType() : DrillBitType.NONE;
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return !state.is(getDrillBit(stack).getTier().getIncorrectBlocksForDrops());
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return JACKHAMMER_ABILITIES.contains(itemAbility)
                && PNCCapabilities.getAirHandler(stack).map(h -> h.getPressure() > 0.1f).orElse(false);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState pState, BlockPos pos, LivingEntity entity) {
        if (entity instanceof ServerPlayer serverPlayer && stack.getItem() instanceof JackHammerItem) {
            HitResult hitResult = RayTraceUtils.getEntityLookedObject(serverPlayer, PneumaticCraftUtils.getPlayerReachDistance(serverPlayer));
            if (hitResult instanceof BlockHitResult blockHitResult) {
                PNCCapabilities.getAirHandler(stack).ifPresent(airHandler -> {
                    DigMode digMode = JackHammerItem.getDigMode(stack);

                    IntList upgrades = UpgradableItemUtils.getUpgradeList(stack, ModUpgrades.SPEED.get(), ModUpgrades.MAGNET.get());
                    int speed = upgrades.getInt(0);
                    boolean magnet = upgrades.getInt(1) > 0 && digMode.isVeinMining();

                    if (digMode.getBitType().getBitQuality() > getDrillBit(stack).getBitQuality()) {
                        // shouldn't happen, but ensure jackhammer isn't in a dig mode that its installed bit can't do
                        digMode = DigMode.MODE_1X1;
                    }

                    Set<BlockPos> brokenPos = getBreakPositions(level, pos, blockHitResult.getDirection(), serverPlayer, digMode);

                    float air = airHandler.getAir();
                    float air0 = air;
                    float usage = serverPlayer.isCreative() ? 0f : PneumaticValues.USAGE_JACKHAMMER * SPEED_MULT[speed];
                    if (magnet) usage *= 1.1f;

                    if (air >= usage) {
                        // take air for the first block (always mined, has already been broken)
                        air -= usage;

                        // now handle vein-mining, based on the dig mode
                        for (BlockPos pos1 : brokenPos) {
                            if (air < usage) break;

                            BlockState state1 = level.getBlockState(pos1);
                            if (state1.getDestroySpeed(level, pos1) < 0) continue;

                            BlockEvent.BreakEvent breakEvent = CommonHooks.fireBlockBreak(serverPlayer.level(), serverPlayer.gameMode.getGameModeForPlayer(), serverPlayer, pos1, state1);
                            if (breakEvent.isCanceled()) {
                                continue;
                            }
                            if (level.getBlockEntity(pos1) != null) {
                                continue;
                            }
                            Block block = state1.getBlock();
                            state1 = block.playerWillDestroy(level, pos1, state1, serverPlayer);
                            boolean removed = state1.onDestroyedByPlayer(level, pos1, serverPlayer, true, level.getFluidState(pos1));
                            if (removed) {
                                block.destroy(level, pos1, state1);
                                if (magnet) {
                                    magnetHarvest(block, level, serverPlayer, pos, pos1, state1, stack);
                                } else {
                                    block.playerDestroy(level, serverPlayer, pos1, state1, null, stack);
                                }
//                                    if (exp > 0 && level instanceof ServerLevel) {
//                                        block.popExperience((ServerLevel) level, magnet ? pos : pos1, exp);
//                                    }
                                air -= usage;
                                serverPlayer.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                            }
                        }
                        if (air != air0 && !serverPlayer.isCreative()) {
                            airHandler.addAir((int) (air - air0));
                        }
                    }
                });
            }
        }
        return true;
    }

    // just like Block#playerDestroy, except all items are dropped in the same place (where the player actually mined)
    private static void magnetHarvest(Block block, Level level, Player player, BlockPos pos0, BlockPos pos, BlockState state, ItemStack stack) {
        player.awardStat(Stats.BLOCK_MINED.get(block));
        player.causeFoodExhaustion(0.005F);
        Block.dropResources(state, level, pos0, null, player, stack);
//        if (level instanceof ServerLevel serverLevel) {
//            Block.getDrops(state, serverLevel, pos, null, player, stack)
//                    .forEach((stackToSpawn) -> Block.popResource(level, pos0, stackToSpawn));
//            state.spawnAfterBreak(serverLevel, pos, stack, true);
//        }
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
            sp.openMenu(new MenuProvider() {
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

    public static Set<BlockPos> getBreakPositions(Level world, BlockPos pos, Direction dir, Player player, DigMode digMode) {
        if (player.isShiftKeyDown()) {
            return new HashSet<>();
        }
        if (digMode.isVeinMining()) {
            return new HashSet<>(getVeinPositions(world, pos, digMode));
        }

        Direction playerHoriz = player.getDirection();
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

        res.remove(pos);
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
        return stack.getOrDefault(ModDataComponents.JACKHAMMER_DIG_MODE, DigMode.MODE_1X1);
    }

    public static void setDigMode(ItemStack stack, DigMode mode) {
        stack.set(ModDataComponents.JACKHAMMER_DIG_MODE, mode);
    }

    public static DigMode cycleDigMode(ItemStack stack, boolean forward) {
        if (stack.getItem() instanceof JackHammerItem jackHammer) {
            DrillBitType ourBit = jackHammer.getDrillBit(stack);
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

    public enum DigMode implements ITranslatableEnum, StringRepresentable {
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

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public static class DrillBitHandler extends BaseItemStackHandler {
        private final ItemStack jackhammerStack;

        public DrillBitHandler(ItemStack jackhammerStack) {
            super(1);

            this.jackhammerStack = jackhammerStack;
            loadContainerContents(jackhammerStack.getOrDefault(ModDataComponents.JACKHAMMER_DRILL_BIT, ItemContainerContents.EMPTY));
        }

        @Override
        public boolean isItemValid(int slot, ItemStack itemStack) {
            return itemStack.isEmpty() || itemStack.getItem() instanceof DrillBitItem;
        }

        public void save() {
            jackhammerStack.set(ModDataComponents.JACKHAMMER_DRILL_BIT, toContainerContents());
        }
    }

    public static class EnchantmentHandler extends BaseItemStackHandler {
        private final ItemStack jackhammerStack;

        public EnchantmentHandler(ItemStack jackhammerStack) {
            super(1);

            this.jackhammerStack = jackhammerStack;

            ItemEnchantments ench = EnchantmentHelper.getEnchantmentsForCrafting(jackhammerStack);
            for (Object2IntMap.Entry<Holder<Enchantment>> entry : ench.entrySet()) {
                if (entry.getKey().is(Enchantments.SILK_TOUCH) || entry.getKey().is(Enchantments.FORTUNE)) {
                    setStackInSlot(0, Util.make(new ItemStack(Items.ENCHANTED_BOOK),
                            stack -> stack.enchant(entry.getKey(), entry.getIntValue())
                    ));
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
            ItemEnchantments.Mutable currentEnchants = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(jackhammerStack));
            currentEnchants.removeIf(h -> h.is(Enchantments.SILK_TOUCH) || h.is(Enchantments.FORTUNE));
            if (validateBook(bookStack)) {
                EnchantmentHelper.getEnchantmentsForCrafting(bookStack).entrySet()
                        .forEach(entry -> currentEnchants.set(entry.getKey(), entry.getIntValue()));
            }
            EnchantmentHelper.setEnchantments(jackhammerStack, currentEnchants.toImmutable());
        }

        public static boolean validateBook(ItemStack bookStack) {
            // must be an enchanted book with Silk Touch or Fortune and nothing else
            if (bookStack.getItem() == Items.ENCHANTED_BOOK) {
                var enchantments = EnchantmentHelper.getEnchantmentsForCrafting(bookStack);
                if (enchantments.size() == 1) {
                    var entry = enchantments.entrySet().stream().findFirst().orElseThrow();
                    return entry.getIntValue() > 0 && (entry.getKey().is(Enchantments.FORTUNE) || entry.getKey().is(Enchantments.SILK_TOUCH));
                }
            }
            return false;
        }
    }

    @EventBusSubscriber(modid = Names.MOD_ID)
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
