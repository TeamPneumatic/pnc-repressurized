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

package me.desht.pneumaticcraft.common.util;

import com.google.common.base.Splitter;
import me.desht.pneumaticcraft.api.item.IFilteringItem;
import me.desht.pneumaticcraft.api.item.IInventoryItem;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.common.XPFluidManager;
import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.common.item.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.commons.lang3.math.NumberUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PneumaticCraftUtils {
    // an impossible blockpos to indicate invalid positions
    private static final BlockPos INVALID_POS = new BlockPos(0, Integer.MIN_VALUE, 0);

    private static final int MAX_CHAR_PER_LINE = 45;

    private static final int[] DYE_COLORS = new int[DyeColor.values().length];
    static {
        for (DyeColor color : DyeColor.values()) {
            float[] rgb = color.getTextureDiffuseColors();
            DYE_COLORS[color.getId()] = (int) (rgb[0] * 255) << 16 | ((int) (rgb[1] * 255) << 8) | (int) (rgb[2] * 255);
        }
    }

    public static List<? extends Component> splitStringComponent(String text) {
        return asStringComponent(splitString(text, MAX_CHAR_PER_LINE));
    }

    public static List<? extends Component> splitStringComponent(String text, int maxCharPerLine) {
        return asStringComponent(splitString(text, maxCharPerLine));
    }

    public static List<String> splitString(String text, int maxCharPerLine) {
        List<String> result = new ArrayList<>();

        StringBuilder builder = new StringBuilder(text.length());
        String format = "";
        for (String para : text.split(Pattern.quote("\n"))) {
            StringTokenizer tok = new StringTokenizer(para, " ");
            int lineLen = 0;
            while (tok.hasMoreTokens()) {
                String token = tok.nextToken();
                // the Splitter here ensures any very long words with no whitespace get split up
                // also important for some localizations, e.g. Chinese text does not use whitespace
                for (String word : Splitter.fixedLength(maxCharPerLine).split(token)) {
                    int idx = word.lastIndexOf("\u00a7");
                    if (idx >= 0 && idx < word.length() - 1) {
                        // note any formatting sequence, so it can also be applied to start of next line
                        format = word.substring(idx, idx + 2);
                        // formatting sequences do not contribute to line length
                        lineLen -= 2;
                    }
                    if (lineLen + word.length() > maxCharPerLine) {
                        result.add(builder.toString());
                        builder.delete(0, builder.length());
                        builder.append(format);
                        lineLen = 0;
                    } else if (lineLen > 0) {
                        builder.append(" ");
                        lineLen++;
                    }
                    builder.append(word);
                    lineLen += word.length();
                }
            }
            result.add(builder.toString());
            builder.delete(0, builder.length());
            builder.append(format);
        }
        return result;
    }

    public static List<String> splitString(String text) {
        return splitString(text, MAX_CHAR_PER_LINE);
    }

    public static List<? extends Component> asStringComponent(List<String> l) {
        return l.stream().map(Component::literal).toList();
    }

    /**
     * Takes in the amount of ticks, and converts it into a time notation. 40 ticks will become "2s", while 2400 will result in "2m".
     *
     * @param ticks number of ticks
     * @param fraction When true, 30 ticks will show as '1.5s' instead of '1s'.
     * @return a formatted time
     */
    public static String convertTicksToMinutesAndSeconds(long ticks, boolean fraction) {
        String part = String.valueOf(ticks % 20 * 5);
        if (part.length() < 2) part = "0" + part;
        ticks /= 20;// first convert to seconds.
        if (ticks < 60) {
            return ticks + (fraction ? "." + part : "") + "s";
        } else {
            return ticks / 60 + "m " + ticks % 60 + "s";
        }
    }

    /**
     * Takes in any integer, and converts it into a string with a additional postfix if needed. 23000 will convert into 23k for instance.
     *
     * @param amount an integer quantity
     * @return a formatted string representation
     */
    public static String convertAmountToString(int amount) {
        if (amount < 10_000) {
            return NumberFormat.getNumberInstance(Locale.getDefault()).format(amount);
        } else if (amount < 1_000_000) {
            return amount / 1_000 + "K";
        } else {
            return amount / 1_000_000 + "M";
        }
    }

    /**
     * Rounds numbers (round-nearest) to the given number of decimal places.
     * E.g. 1.234 with decimal 1 will result in a string holding "1.2"
     * but 1.26 with decimal 1 will result in "1.3"
     *
     * @param value a double-precision quantity
     * @param decimals number of digits to the right of the decimal point
     * @return a formatted string representation
     */
    public static String roundNumberTo(double value, int decimals) {
        String fmtStr = "%." + decimals + "f";
        return String.format(fmtStr, roundNumberToDouble(value, decimals));
    }

    /**
     * Rounds numbers to the given number of decimal places.
     * E.g. 1.234 with decimal 1 will result in a string holding "1.2"
     *
     * @param value a double-precision quantity
     * @param decimals number of digits to the right of the decimal point
     * @return the rounded value as a double-precision quantity
     */
    public static double roundNumberToDouble(double value, int decimals) {
        return new BigDecimal(value).setScale(decimals, RoundingMode.HALF_DOWN).doubleValue();
    }

    /**
     * Compare two floats which are tested for having (almost) the same value. There are methods in MathHelper to do
     * this but at least one of them is client-only.
     */
    public static boolean epsilonEquals(float f1, float f2) {
        return epsilonEquals(f1, f2, 0.0001F);
    }

    public static boolean epsilonEquals(float f1, float f2, float maxDifference) {
        return Math.abs(f1 - f2) < maxDifference;
    }

    public static boolean epsilonEquals(double d1, double d2) {
        return epsilonEquals(d1, d2, 0.0001);
    }

    public static boolean epsilonEquals(double d1, double d2, double maxDifference) {
        return Math.abs(d1 - d2) < maxDifference;
    }

    /**
     * Sorts the stacks given alphabetically, combines them (so 2x64 will become 1x128), and adds the strings into the
     * given string list.  This method is aware of inventory items implementing the {@link IInventoryItem} interface.
     *
     * @param textList string list to add information to
     * @param originalStacks array of item stacks to sort and combine
     */
    public static List<Component> summariseItemStacks(List<Component> textList, List<ItemStack> originalStacks) {
        return summariseItemStacks(textList, originalStacks, Symbols.bullet());
    }

    /**
     * Sort the given array of itemstacks alphabetically by display name, then combine them (so e.g. 2x64 becomes
     * 1x128), and add the display name to the given text component list.  This method is aware of inventory items
     * implementing the {@link IInventoryItem} interface.
     *
     * @param textList text component list to add information to
     * @param originalStacks array of item stacks to sort and combine
     * @param prefix prefix string to prepend to each line of output
     */
    public static List<Component> summariseItemStacks(List<Component> textList, List<ItemStack> originalStacks, MutableComponent prefix) {
        List<ItemStack> sortedStacks = originalStacks.stream()
                .sorted((o1, o2) -> o1.getHoverName().getString().compareToIgnoreCase(o2.getHoverName().getString()))
                .toList();

        int itemCount = 0;
        ItemStack prevItemStack = ItemStack.EMPTY;
        List<ItemStack> prevInventoryItems = null;
        for (ItemStack stack : sortedStacks) {
            if (!stack.isEmpty()) {
                if (!ItemStack.isSameItem(stack, prevItemStack) || prevInventoryItems != null && prevInventoryItems.size() > 0) {
                    if (!prevItemStack.isEmpty()) {
                        textList.add(prefix.copy().append(PneumaticCraftUtils.convertAmountToString(itemCount) + " x " + prevItemStack.getHoverName().getString()));
                    }
                    if (prevInventoryItems != null) {
                        summariseItemStacks(textList, prevInventoryItems, prefix.copy().append(Symbols.ARROW_DOWN_RIGHT + " "));
                    }
                    prevItemStack = stack;
                    itemCount = stack.getCount();
                } else {
                    itemCount += stack.getCount();
                }
                prevInventoryItems = ItemRegistry.getInstance().getStacksInItem(stack);
            }
        }
        if (itemCount > 0 && !prevItemStack.isEmpty()) {
            textList.add(prefix.copy().append(PneumaticCraftUtils.convertAmountToString(itemCount) + " x " + prevItemStack.getHoverName().getString()));
            summariseItemStacks(textList, prevInventoryItems, prefix.copy().append(Symbols.ARROW_DOWN_RIGHT + " "));
        }
        return textList;
    }

    private static void addText(List<Component> l, String s) {
        l.add(Component.literal(s));
    }

    /**
     * Retrieve a web page from the given URL.
     *
     * @param urlString the URL
     * @return the web page
     * @throws IOException if there are any problems
     */
    public static String getPage(final String urlString) throws IOException {
        StringBuilder all = new StringBuilder();
        URL myUrl = new URL(urlString);
        try (BufferedReader in = new BufferedReader(new InputStreamReader(myUrl.openStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                all.append(line).append(System.getProperty("line.separator"));
            }
        }

        return all.toString();
    }

    public static double distBetween(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Math.sqrt(distBetweenSq(x1, y1, z1, x2, y2, z2));
    }

    public static double distBetweenSq(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2) + Math.pow(z1 - z2, 2);
    }

    public static double distBetweenSq(Vec3i pos, double x, double y, double z) {
        return distBetweenSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, x, y, z);
    }

    public static double distBetweenSq(BlockPos pos1, BlockPos pos2) {
        return distBetweenSq(pos1.getX(), pos1.getY(), pos1.getZ(), pos2.getX(), pos2.getY(), pos2.getZ());
    }

    public static double distBetween(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    public static double distBetweenSq(double x1, double y1, double x2, double y2) {
        return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
    }

    public static double distBetween(Vec3i pos, double x, double y, double z) {
        return distBetween(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, x, y, z);
    }

    public static double distBetween(Vec3i pos1, Vec3i pos2) {
        return distBetween(pos1, pos2.getX() + 0.5, pos2.getY() + 0.5, pos2.getZ() + 0.5);
    }

    public static boolean doesItemMatchFilter(@Nonnull ItemStack filterStack, @Nonnull ItemStack stack, boolean checkDurability, boolean checkNBT, boolean checkModSimilarity) {
        if (filterStack.isEmpty() && stack.isEmpty()) return true;
        if (filterStack.isEmpty() || stack.isEmpty()) return false;

        if (checkModSimilarity) {
            String mod1 = getRegistryName(filterStack.getItem()).map(ResourceLocation::getNamespace).orElse("");
            String mod2 = getRegistryName(stack.getItem()).map(ResourceLocation::getNamespace).orElse("");
            return mod1.equals(mod2);
        }

        if (filterStack.getItem() instanceof IFilteringItem f) {
            return f.matchFilter(filterStack, stack);
        } else if (stack.getItem() instanceof IFilteringItem f) {
            return f.matchFilter(stack, filterStack);
        }

        if (filterStack.getItem() != stack.getItem()) return false;

        boolean durabilityOK = !checkDurability || (filterStack.getMaxDamage() > 0 && filterStack.getDamageValue() == stack.getDamageValue());
        boolean nbtOK = !checkNBT || Objects.equals(filterStack.getTag(), stack.getTag());

        return durabilityOK && nbtOK;
    }

    public static boolean isBlockLiquid(Block block) {
        return block instanceof LiquidBlock;
    }

    public static void dropItemOnGround(ItemStack stack, Level world, BlockPos pos) {
        dropItemOnGround(stack, world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    public static void dropItemOnGround(ItemStack stack, Level world, double x, double y, double z) {
        float dX = world.random.nextFloat() * 0.8F + 0.1F;
        float dY = world.random.nextFloat() * 0.8F + 0.1F;
        float dZ = world.random.nextFloat() * 0.8F + 0.1F;

        ItemEntity entityItem = new ItemEntity(world, x + dX, y + dY, z + dZ, stack.copy());

        if (stack.hasTag()) {
            entityItem.getItem().setTag(Objects.requireNonNull(stack.getTag()).copy());
        }

        float factor = 0.05F;
        entityItem.setDeltaMovement(world.random.nextGaussian() * factor, world.random.nextGaussian() * factor + 0.2, world.random.nextGaussian() * factor);
        world.addFreshEntity(entityItem);
    }

    public static void dropItemOnGroundPrecisely(ItemStack stack, Level world, double x, double y, double z) {
        ItemEntity entityItem = new ItemEntity(world, x, y, z, stack.copy());

        if (stack.hasTag()) {
            entityItem.getItem().setTag(Objects.requireNonNull(stack.getTag()).copy());
        }
        entityItem.setDeltaMovement(0, 0, 0);
        world.addFreshEntity(entityItem);
    }

    public static ServerPlayer getPlayerFromId(UUID uuid) {
        return ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(uuid);
    }

    public static ServerPlayer getPlayerFromName(String name) {
        return ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByName(name);
    }

    public static boolean isPlayerOp(Player player) {
        return player.hasPermissions(2);
    }

    /**
     * Attempt to place a block in the world, respecting BlockEvent.PlaceEvent results.
     *
     * @param w the world
     * @param pos the position in the world
     * @param player the player who is placing the block
     * @param face the face against which the block is placed
     * @param newState the blockstate to change the position to
     * @return true if the block could be placed, false otherwise
     */
    @SuppressWarnings("UnusedReturnValue")
    public static boolean tryPlaceBlock(Level w, BlockPos pos, Player player, Direction face, BlockState newState) {
        BlockSnapshot snapshot = BlockSnapshot.create(w.dimension(), w, pos);
        if (!ForgeEventFactory.onBlockPlace(player, snapshot, face)) {
            w.setBlockAndUpdate(pos, newState);
            return true;
        }
        return false;
    }

    /**
     * A little hack needed here; in 1.8 players were a subclass of EntityLiving and could be used as entities for
     * pathfinding purposes.  But now they extend EntityLivingBase, and pathfinder methods only work for subclasses of
     * EntityLiving.  So create a temporary living entity at the player's location and pathfind from that.
     *
     * @param player the player to mimic
     * @return a dummy player-sized living entity
     */
    public static Mob createDummyEntity(Player player) {
        Zombie dummy = new Zombie(player.level()) {
//            @Override
//            protected void registerAttributes() {
//                super.registerAttributes();
//                this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(CoordTrackUpgradeHandler.SEARCH_RANGE);
//            }
        };
        dummy.setPos(player.getX(), player.getY(), player.getZ());
        return dummy;
    }

    /**
     * Convenience method, ported from 1.8.  Consume one item from the player's inventory.
     *
     * @param inv player's inventory
     * @param item item to consume
     * @return true if an item was consumed
     */
    public static boolean consumeInventoryItem(Inventory inv, Item item) {
        for (int i = 0; i < inv.items.size(); ++i) {
            if (inv.items.get(i).getItem() == item) {
                inv.items.get(i).shrink(1);
                if (inv.items.get(i).getCount() <= 0) {
                    inv.items.set(i, ItemStack.EMPTY);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Add all of the non-empty items in the given item handler to the given list.
     * @param handler the item handler
     * @param items the list
     */
    public static void collectNonEmptyItems(IItemHandler handler, NonNullList<ItemStack> items) {
        if (handler != null) {
            for (int i = 0; i < handler.getSlots(); i++) {
                if (!handler.getStackInSlot(i).isEmpty()) {
                    items.add(handler.getStackInSlot(i));
                }
            }
        }
    }

    /**
     * Convenience method, ported from 1.8.  Try to consume one item from the player's inventory.
     *
     * @param inv player's inventory
     * @param stack item to consume
     * @return true if an item was consumed
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean consumeInventoryItem(Inventory inv, ItemStack stack) {
        int toConsume = stack.getCount();
        for (int i = 0; i < inv.items.size(); ++i) {
            ItemStack invStack = inv.items.get(i);
            int consumed;
            if (ItemStack.isSameItem(invStack, stack)) {
                consumed = Math.min(invStack.getCount(), stack.getCount());
                invStack.shrink(consumed);
            } else {
                consumed = invStack.getCapability(ForgeCapabilities.ITEM_HANDLER).map(h -> {
                    for (int j = 0; j < h.getSlots(); j++) {
                        ItemStack invStack2 = h.getStackInSlot(j);
                        if (ItemStack.isSameItem(invStack2, stack)) {
                            int extracted = Math.min(invStack2.getCount(), stack.getCount());
                            ItemStack s = h.extractItem(j, extracted, false);
                            return s.getCount();
                        }
                    }
                    return 0;
                }).orElse(0);
            }
            toConsume -= consumed;
            if (toConsume <= 0) return true;
        }
        return toConsume <= 0;
    }

    /**
     * Get a translation string for the given key.  This has support for The One Probe which runs server-side.
     *
     * @param s the translation key
     * @return the translated string (if called server-side, a string which The One Probe will handle client-side)
     */
    public static MutableComponent xlate(String s, Object... args) {
        return Component.translatable(s, args);
    }

    public static MutableComponent dyeColorDesc(int c) {
        return Component.translatable("color.minecraft." + DyeColor.byId(c).getName())
                .withStyle(ChatFormatting.BOLD);
    }

    public static void copyItemHandler(IItemHandler source, ItemStackHandler dest, int maxSlots) {
        int nSlots = Math.min(maxSlots, source.getSlots());
        dest.setSize(nSlots);
        for (int i = 0; i < nSlots; i++) {
            dest.setStackInSlot(i, source.getStackInSlot(i).copy());
        }
    }

    public static void copyItemHandler(IItemHandler source, ItemStackHandler dest) {
        copyItemHandler(source, dest, source.getSlots());
    }

    public static String posToString(@Nullable BlockPos pos) {
        return isValidPos(pos) ? String.format("%d, %d, %d", pos.getX(), pos.getY(), pos.getZ()) : "-";
    }

    public static boolean isValidPos(@Nullable BlockPos pos) {
        return pos != null && pos != INVALID_POS;
    }

    public static BlockPos invalidPos() {
        return INVALID_POS;
    }

    public static <T extends BlockEntity> Optional<T> getTileEntityAt(BlockGetter w, BlockPos pos, Class<T> cls) {
        if (w != null && pos != null) {
            BlockEntity te = w.getBlockEntity(pos);
            if (te != null && cls.isAssignableFrom(te.getClass())) {
                //noinspection unchecked
                return Optional.of((T) te);
            }
        }
        return Optional.empty();
    }

    /**
     * Try to transfer the XP from the given XP orb entity into the given fluid handler.  If the handler has enough
     * room to store only part of the orb's XP, it will reduce the orb's XP by the amount taken (assuming the action is
     * execute). Does not remove the entity; the caller should do that iff this returns true.
     *
     * @param handler the fluid handler
     * @param orb the XP orb
     * @param action whether to simulate the action
     * @return true if the XP orb was (or could be) fully absorbed into the fluid handler
     */
    public static boolean fillTankWithOrb(IFluidHandler handler, ExperienceOrb orb, FluidAction action) {
        int ratio = XPFluidManager.getInstance().getXPRatio(ModFluids.MEMORY_ESSENCE.get());
        int fluidAmount = orb.getValue() * ratio;
        FluidStack toFill = new FluidStack(ModFluids.MEMORY_ESSENCE.get(), fluidAmount);
        int filled = handler.fill(toFill, action);
        if (filled > 0 && filled < fluidAmount && action.execute()) {
            orb.value = orb.value - Math.max(1, filled / ratio);  // partial fill, adjust the orb
        }
        return filled == fluidAmount;
    }

    public static double getPlayerReachDistance(Player player) {
        if (player != null) {
            AttributeInstance attr = player.getAttribute(ForgeMod.BLOCK_REACH.get());
            if (attr != null) return attr.getValue() + 1D;
        }
        return 4.5D;
    }

    public static boolean canPlayerReach(Player player, BlockPos pos) {
        if (player == null) return false;
        double dist = getPlayerReachDistance(player);
        return player.distanceToSqr(Vec3.atCenterOf(pos)) <= dist * dist;
    }

    /**
     * Check if a given string encodes a valid integer (negative included). Also intended to work for
     * partial strings, so useful for textfield validation.
     *
     * @param str the string to test
     * @return true if the string encodes an integer (i.e. Integer.parseInt(str) won't throw an exception)
     */
    public static boolean isInteger(String str) {
        if (str.isEmpty() || str.equals("-")) {
            return true;  // treat as numeric zero
        }
        if (str.startsWith("-")) str = str.substring(1);
        return NumberUtils.isDigits(str);
    }

    /**
     * Check if a given string encodes a valid number (negative &amp; decimal point included). Also intended to work for
     * partial strings, so useful for textfield validation.
     *
     * @param str the string to test
     * @return true if the string encodes a number (i.e. NumberUtils.createNumber(str) won't throw an exception)
     */
    public static boolean isNumber(String str) {
        if (str.isEmpty() || str.equals("-")) {
            return true;  // treat as numeric zero
        }
        if (str.startsWith("-")) str = str.substring(1);
        if (str.endsWith(".")) str = str + "0";
        return NumberUtils.isParsable(str);
    }

    /**
     * Get a resource location from the given string, defaulting to "pneumaticcraft:" namespace
     *
     * @param str a string
     * @return a resource location
     */
    public static ResourceLocation modDefaultedRL(String str) {
        return str.indexOf(':') > 0 ? new ResourceLocation(str) : new ResourceLocation(Names.MOD_ID, str);
    }

    /**
     * Stringify a resource location, omitting the namespace if it's "pneumaticcraft:"
     *
     * @param rl a resource location
     * @return stringified resource location
     */
    public static String modDefaultedString(ResourceLocation rl) {
        return rl.getNamespace().equals(Names.MOD_ID) ? rl.getPath() : rl.toString();
    }

    /**
     * Convert a DyeColor to packed RGB integer (top 8 bits - alpha - are 0)
     * @param dyeColor the dye color
     * @return packed RGB integer
     */
    public static int getDyeColorAsRGB(DyeColor dyeColor) {
        return DYE_COLORS[dyeColor.getId()];
    }

    /**
     * Get the name of the block at the given position
     * @param level the level
     * @param pos the blockpos
     * @return the block name, empty if the given position isn't currently loaded
     */
    public static Component getBlockNameAt(Level level, BlockPos pos) {
        return level.isLoaded(pos) ? xlate(level.getBlockState(pos).getBlock().getDescriptionId()) : Component.empty().plainCopy();
    }

    public static CompoundTag copyNBTWithout(@Nonnull CompoundTag nbt, @Nonnull String skip) {
        CompoundTag newNBT = new CompoundTag();

        for (String key : nbt.getAllKeys()) {
            if (!skip.equals(key)) {
                Tag subTag = nbt.get(key);
                if (subTag != null) {
                    newNBT.put(key, subTag.copy());
                }
            }
        }
        return newNBT.isEmpty() ? new CompoundTag() : newNBT;
    }

    public static Set<TagKey<Item>> itemTags(Item item) {
        return ForgeRegistries.ITEMS.tags().getReverseTag(item)
                .map(reverseTag -> reverseTag.getTagKeys().collect(Collectors.toSet()))
                .orElse(Set.of());
    }

    public static Optional<ResourceLocation> getRegistryName(Item item) {
        return getRegistryName(ForgeRegistries.ITEMS, item);
    }

    public static Optional<ResourceLocation> getRegistryName(Block block) {
        return getRegistryName(ForgeRegistries.BLOCKS, block);
    }

    public static Optional<ResourceLocation> getRegistryName(Fluid fluid) {
        return getRegistryName(ForgeRegistries.FLUIDS, fluid);
    }

    public static Optional<ResourceLocation> getRegistryName(Entity entity) {
        return getRegistryName(ForgeRegistries.ENTITY_TYPES, entity.getType());
    }

    public static <T> Optional<ResourceLocation> getRegistryName(IForgeRegistry<T> registry, T object) {
        return Optional.ofNullable(registry.getKey(object));
    }

    public static boolean isChunkLoaded(LevelAccessor level, BlockPos pos) {
        return level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }

    public static double average(long[] pValues) {
        long i = 0L;

        for(long j : pValues) {
            i += j;
        }

        return (double)i / (double)pValues.length;
    }

    public static Component combineComponents(List<Component> components) {
        return components.stream().reduce((c1, c2) -> c1.copy().append("\n").append(c2)).orElse(Component.empty());
    }
}
