package me.desht.pneumaticcraft.common.util;

import com.google.common.base.Splitter;
import me.desht.pneumaticcraft.api.item.IInventoryItem;
import me.desht.pneumaticcraft.api.item.ITagFilteringItem;
import me.desht.pneumaticcraft.common.XPFluidManager;
import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.common.item.ItemRegistry;
import me.desht.pneumaticcraft.lib.GuiConstants;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.math.NumberUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PneumaticCraftUtils {
    // an impossible blockpos to indicate invalid positions
    private static final BlockPos INVALID_POS = new BlockPos(0, Integer.MIN_VALUE, 0);

    /**
     * Returns the EnumFacing of the given entity.
     *
     * @param entity the entity
     * @param includeUpAndDown false when UP/DOWN should not be included.
     * @return the entity's facing direction
     */
    public static Direction getDirectionFacing(LivingEntity entity, boolean includeUpAndDown) {
        double yaw = entity.rotationYaw;
        while (yaw < 0)
            yaw += 360;
        yaw = yaw % 360;
        if (includeUpAndDown) {
            if (entity.rotationPitch > 45) return Direction.DOWN;
            else if (entity.rotationPitch < -45) return Direction.UP;
        }
        if (yaw < 45) return Direction.SOUTH;
        else if (yaw < 135) return Direction.WEST;
        else if (yaw < 225) return Direction.NORTH;
        else if (yaw < 315) return Direction.EAST;
        else return Direction.SOUTH;
    }

    /**
     * Get a yaw angle from an EnumFacing
     *
     * @param facing the facing direction
     * @return the yaw angle
     */
    public static int getYawFromFacing(Direction facing) {
        switch (facing) {
            case NORTH:
                return 180;
            case WEST:
                return 90;
            case EAST:
                return -90;
            default:  // SOUTH
                return 0;
        }
    }

    public static final double[] sin;
    public static final double[] cos;
    public static final int CIRCLE_POINTS = 500;

    /*
     * Initializes the sin,cos and tan variables, so that they can be used without having to calculate them every time (render tick).
     */
    static {
        sin = new double[CIRCLE_POINTS];
        cos = new double[CIRCLE_POINTS];

        for (int i = 0; i < CIRCLE_POINTS; i++) {
            double angle = 2 * Math.PI * i / CIRCLE_POINTS;
            sin[i] = Math.sin(angle);
            cos[i] = Math.cos(angle);
        }
    }

    public static List<ITextComponent> splitStringComponent(String text) {
        return asStringComponent(splitString(text, GuiConstants.MAX_CHAR_PER_LINE));
    }

    public static List<ITextComponent> splitStringComponent(String text, int maxCharPerLine) {
        return asStringComponent(splitString(text, maxCharPerLine));
    }

    public static List<String> splitString(String text, int maxCharPerLine) {
        List<String> result = new ArrayList<>();

        StringBuilder builder = new StringBuilder(text.length());
        String format = "";
        for (String para : text.split(Pattern.quote("${br}"))) {
            StringTokenizer tok = new StringTokenizer(para, " ");
            int lineLen = 0;
            while (tok.hasMoreTokens()) {
                String token = tok.nextToken();
                // the Splitter here ensures any very long words with no whitespace get split up
                // also important for some localizations, e.g. Chinese text does not use whitespace
                for (String word : Splitter.fixedLength(maxCharPerLine).split(token)) {
                    int idx = word.lastIndexOf("\u00a7");
                    if (idx >= 0 && idx < word.length() - 1) {
                        // note any formatting sequence so it can also be applied to start of next line
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
        return splitString(text, GuiConstants.MAX_CHAR_PER_LINE);
    }

    public static List<ITextComponent> asStringComponent(List<String> l) {
        return l.stream().map(StringTextComponent::new).collect(Collectors.toList());
    }

    /**
     * Takes in the amount of ticks, and converts it into a time notation. 40 ticks will become "2s", while 2400 will result in "2m".
     *
     * @param ticks number of ticks
     * @param fraction When true, 30 ticks will show as '1.5s' instead of '1s'.
     * @return a formatted time
     */
    public static String convertTicksToMinutesAndSeconds(long ticks, boolean fraction) {
        String part = ticks % 20 * 5 + "";
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
     * Rounds numbers down at the given decimal. 1.234 with decimal 1 will result in a string holding "1.2"
     *
     * @param value a double-precision quantity
     * @param decimals number of digits to the right of the decimal point
     * @return a formatted string representation
     */
    public static String roundNumberTo(double value, int decimals) {
        return new BigDecimal(value).setScale(decimals, BigDecimal.ROUND_HALF_DOWN).toPlainString();
    }

    /**
     * Rounds numbers down at the given decimal. 1.234 with decimal 1 will result in a string holding "1.2"
     *
     * @param value a double-precision quantity
     * @param decimals number of digits to the right of the decimal point
     * @return the rounded value as a double-precision quantity
     */
    public static double roundNumberToDouble(double value, int decimals) {
        return new BigDecimal(value).setScale(decimals, BigDecimal.ROUND_HALF_DOWN).doubleValue();
    }

    /**
     * Rounds numbers down at the given decimal. 1.234 with decimal 1 will result in a string holding "1.2"
     *
     * @param value a floating point quantity
     * @param decimals number of digits to the right of the decimal point
     * @return a formatted string representation
     */
    public static String roundNumberTo(float value, int decimals) {
        return "" + Math.round(value * Math.pow(10, decimals)) / Math.pow(10, decimals);
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
     * @param originalStacks array of item stacks to sort & combine
     */
    public static void summariseItemStacks(List<ITextComponent> textList, ItemStack[] originalStacks) {
        summariseItemStacks(textList, originalStacks, GuiConstants.bullet().getString());
    }

    /**
     * Sort the given array of itemstacks alphabetically by display name, then combine them (so e.g. 2x64 becomes
     * 1x128), and add the display name to the given text component list.  This method is aware of inventory items
     * implementing the {@link IInventoryItem} interface.
     *
     * @param textList text component list to add information to
     * @param originalStacks array of item stacks to sort & combine
     * @param prefix prefix string to prepend to each line of output
     */
    public static void summariseItemStacks(List<ITextComponent> textList, ItemStack[] originalStacks, String prefix) {
        ItemStack[] stacks = Arrays.copyOf(originalStacks, originalStacks.length);

        Arrays.sort(stacks, (o1, o2) -> o1.getDisplayName().getString().compareToIgnoreCase(o2.getDisplayName().getString()));

        int itemCount = 0;
        ItemStack prevItemStack = ItemStack.EMPTY;
        List<ItemStack> prevInventoryItems = null;
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                if (!stack.isItemEqual(prevItemStack) || prevInventoryItems != null && prevInventoryItems.size() > 0) {
                    if (!prevItemStack.isEmpty()) {
                        addText(textList, prefix  + PneumaticCraftUtils.convertAmountToString(itemCount) + " x " + prevItemStack.getDisplayName().getString());
                    }
                    if (prevInventoryItems != null) {
                        summariseItemStacks(textList, prevInventoryItems.toArray(new ItemStack[0]), prefix + GuiConstants.ARROW_DOWN_RIGHT + " ");
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
            addText(textList,prefix + PneumaticCraftUtils.convertAmountToString(itemCount) + " x " + prevItemStack.getDisplayName().getString());
            summariseItemStacks(textList, prevInventoryItems.toArray(new ItemStack[0]), prefix + GuiConstants.ARROW_DOWN_RIGHT + " ");
        }
    }

    private static void addText(List<ITextComponent> l, String s) {
        l.add(new StringTextComponent(s));
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

    public static double distBetweenSq(Vector3i pos, double x, double y, double z) {
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

    public static double distBetween(Vector3i pos, double x, double y, double z) {
        return distBetween(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, x, y, z);
    }

    public static double distBetween(Vector3i pos1, Vector3i pos2) {
        return distBetween(pos1, pos2.getX() + 0.5, pos2.getY() + 0.5, pos2.getZ() + 0.5);
    }

    public static boolean doesItemMatchFilter(@Nonnull ItemStack filterStack, @Nonnull ItemStack stack, boolean checkDurability, boolean checkNBT, boolean checkModSimilarity) {
        if (filterStack.isEmpty() && stack.isEmpty()) return true;
        if (filterStack.isEmpty() || stack.isEmpty()) return false;

        if (checkModSimilarity) {
            String mod1 = filterStack.getItem().getRegistryName().getNamespace();
            String mod2 = stack.getItem().getRegistryName().getNamespace();
            return mod1.equals(mod2);
        }

        if (filterStack.getItem() instanceof ITagFilteringItem) {
            return ((ITagFilteringItem) filterStack.getItem()).matchTags(filterStack, stack.getItem());
        }

        if (filterStack.getItem() != stack.getItem()) return false;

        boolean durabilityOK = !checkDurability || (filterStack.getMaxDamage() > 0 && filterStack.getDamage() == stack.getDamage());
        boolean nbtOK = !checkNBT || (filterStack.hasTag() ? filterStack.getTag().equals(stack.getTag()) : !stack.hasTag());

        return durabilityOK && nbtOK;
    }

    public static boolean isBlockLiquid(Block block) {
        return block instanceof FlowingFluidBlock;
    }

    public static void dropItemOnGround(ItemStack stack, World world, BlockPos pos) {
        dropItemOnGround(stack, world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    public static void dropItemOnGround(ItemStack stack, World world, double x, double y, double z) {
        float dX = world.rand.nextFloat() * 0.8F + 0.1F;
        float dY = world.rand.nextFloat() * 0.8F + 0.1F;
        float dZ = world.rand.nextFloat() * 0.8F + 0.1F;

        ItemEntity entityItem = new ItemEntity(world, x + dX, y + dY, z + dZ, stack.copy());

        if (stack.hasTag()) {
            entityItem.getItem().setTag(stack.getTag().copy());
        }

        float factor = 0.05F;
        entityItem.setMotion(world.rand.nextGaussian() * factor, world.rand.nextGaussian() * factor + 0.2, world.rand.nextGaussian() * factor);
        world.addEntity(entityItem);
        stack.setCount(0);
    }

    public static void dropItemOnGroundPrecisely(ItemStack stack, World world, double x, double y, double z) {
        ItemEntity entityItem = new ItemEntity(world, x, y, z, stack.copy());

        if (stack.hasTag()) {
            entityItem.getItem().setTag(stack.getTag().copy());
        }
        entityItem.setMotion(0, 0, 0);
        world.addEntity(entityItem);
        stack.setCount(0);
    }

    public static PlayerEntity getPlayerFromId(UUID uuid) {
        return ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUUID(uuid);
    }

    public static PlayerEntity getPlayerFromName(String name) {
        return ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUsername(name);
    }

    public static boolean isPlayerOp(PlayerEntity player) {
        return player.hasPermissionLevel(2);
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
    public static boolean tryPlaceBlock(World w, BlockPos pos, PlayerEntity player, Direction face, BlockState newState) {
        BlockSnapshot snapshot = BlockSnapshot.create(w.getDimensionKey(), w, pos);
        if (!ForgeEventFactory.onBlockPlace(player, snapshot, face)) {
            w.setBlockState(pos, newState);
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
    public static MobEntity createDummyEntity(PlayerEntity player) {
        ZombieEntity dummy = new ZombieEntity(player.world) {
//            @Override
//            protected void registerAttributes() {
//                super.registerAttributes();
//                this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(CoordTrackUpgradeHandler.SEARCH_RANGE);
//            }
        };
        dummy.setPosition(player.getPosX(), player.getPosY(), player.getPosZ());
        return dummy;
    }

    /**
     * Convenience method, ported from 1.8.  Consume one item from the player's inventory.
     *
     * @param inv player's inventory
     * @param item item to consume
     * @return true if an item was consumed
     */
    public static boolean consumeInventoryItem(PlayerInventory inv, Item item) {
        for (int i = 0; i < inv.mainInventory.size(); ++i) {
            if (inv.mainInventory.get(i).getItem() == item) {
                inv.mainInventory.get(i).shrink(1);
                if (inv.mainInventory.get(i).getCount() <= 0) {
                    inv.mainInventory.set(i, ItemStack.EMPTY);
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
    public static boolean consumeInventoryItem(PlayerInventory inv, ItemStack stack) {
        int toConsume = stack.getCount();
        for (int i = 0; i < inv.mainInventory.size(); ++i) {
            ItemStack invStack = inv.mainInventory.get(i);
            if (ItemStack.areItemsEqual(invStack, stack)) {
                int consumed = Math.min(invStack.getCount(), stack.getCount());
                invStack.shrink(consumed);
                toConsume -= consumed;
                if (toConsume <= 0) return true;
            }
        }
        return toConsume <= 0;
    }

    /**
     * Get a resource location with the domain of PneumaticCraft: Repressurized's mod ID.
     *
     * @param path the path
     * @return a mod-specific ResourceLocation for the given path
     */
    public static ResourceLocation RL(String path) {
        return new ResourceLocation(Names.MOD_ID, path);
    }

    /**
     * Get a translation string for the given key.  This has support for The One Probe which runs server-side.
     *
     * @param s the translation key
     * @return the translated string (if called server-side, a string which The One Probe will handle client-side)
     */
    public static TranslationTextComponent xlate(String s, Object... args) {
        return new TranslationTextComponent(s, args);
    }

    public static ITextComponent dyeColorDesc(int c) {
        return new TranslationTextComponent("color.minecraft." + DyeColor.byId(c).getTranslationKey())
                .mergeStyle(TextFormatting.BOLD);
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

    public static BlockPos getPosForEntity(Entity e) {
        return new BlockPos(e.getPosX(), e.getPosY(), e.getPosZ());
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

    public static <T extends TileEntity> Optional<T> getTileEntityAt(IBlockReader w, BlockPos pos, Class<T> cls) {
        if (w != null && pos != null) {
            TileEntity te = w.getTileEntity(pos);
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
     * @param action whether or not to simulate the action
     * @return true if the XP orb was (or could be) fully absorbed into the fluid handler
     */
    public static boolean fillTankWithOrb(IFluidHandler handler, ExperienceOrbEntity orb, FluidAction action) {
        int ratio = XPFluidManager.getInstance().getXPRatio(ModFluids.MEMORY_ESSENCE.get());
        int fluidAmount = orb.getXpValue() * ratio;
        FluidStack toFill = new FluidStack(ModFluids.MEMORY_ESSENCE.get(), fluidAmount);
        int filled = handler.fill(toFill, action);
        if (filled > 0 && filled < fluidAmount && action.execute()) {
            orb.xpValue = orb.xpValue - Math.max(1, filled / ratio);  // partial fill, adjust the orb
        }
        return filled == fluidAmount;
    }

    public static double getPlayerReachDistance(PlayerEntity player) {
        if (player != null) {
            ModifiableAttributeInstance attr = player.getAttribute(ForgeMod.REACH_DISTANCE.get());
            if (attr != null) return attr.getValue() + 1D;
        }
        return 4.5D;
    }

    public static boolean canPlayerReach(PlayerEntity player, BlockPos pos) {
        if (player == null) return false;
        double dist = getPlayerReachDistance(player);
        return player.getDistanceSq(Vector3d.copyCentered(pos)) <= dist * dist;
    }

    /**
     * In 1.17 this will become non-trivial.  Adding it now to make porting easier in future.
     *
     * @param world the world
     * @return minimum height allowed for this world.
     */
    public static int getMinHeight(@SuppressWarnings("unused") World world) {
        return 0;
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
     * Check if a given string encodes a valid number (negative & decimal point included). Also intended to work for
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
}
