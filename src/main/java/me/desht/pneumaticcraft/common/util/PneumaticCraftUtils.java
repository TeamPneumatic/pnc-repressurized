package me.desht.pneumaticcraft.common.util;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.item.IInventoryItem;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.renderHandler.CoordTrackUpgradeHandler;
import me.desht.pneumaticcraft.common.item.ItemRegistry;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import me.desht.pneumaticcraft.lib.GuiConstants;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.*;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.stream.Stream;

public class PneumaticCraftUtils {
    private static final List<Item> inventoryItemBlacklist = new ArrayList<>();

    /**
     * Returns the ForgeDirection of the facing of the entity given.
     *
     * @param entity the entity
     * @param includeUpAndDown false when UP/DOWN should not be included.
     * @return the entity's facing direction
     */
    public static EnumFacing getDirectionFacing(EntityLivingBase entity, boolean includeUpAndDown) {
        double yaw = entity.rotationYaw;
        while (yaw < 0)
            yaw += 360;
        yaw = yaw % 360;
        if (includeUpAndDown) {
            if (entity.rotationPitch > 45) return EnumFacing.DOWN;
            else if (entity.rotationPitch < -45) return EnumFacing.UP;
        }
        if (yaw < 45) return EnumFacing.SOUTH;
        else if (yaw < 135) return EnumFacing.WEST;
        else if (yaw < 225) return EnumFacing.NORTH;
        else if (yaw < 315) return EnumFacing.EAST;
        else return EnumFacing.SOUTH;
    }

    public static int getYawFromFacing(EnumFacing facing) {
        switch (facing) {
            case NORTH:
                return 180;
            case SOUTH:
                return 0;
            case WEST:
                return 90;
            case EAST:
                return -90;
            default:
                return 0;
        }
    }

    /**
     * Rotates the render matrix dependant on the given metadata of a block. Used in the render methods of many PneumaticCraft TileEntities.
     *
     * @param metadata block metadata
     * @return the angle (in degrees) of resulting rotation around the Y axis
     */
    @SideOnly(Side.CLIENT)
    public static double rotateMatrixByMetadata(int metadata) {
        EnumFacing facing = EnumFacing.byIndex(metadata & 7);
        float metaRotation;
        switch (facing) {
            case UP:
                metaRotation = 0;
                GlStateManager.rotate(90, 1, 0, 0);
                GlStateManager.translate(0, -1, -1);
                break;
            case DOWN:
                metaRotation = 0;
                GlStateManager.rotate(-90, 1, 0, 0);
                GlStateManager.translate(0, -1, 1);
                break;
            case NORTH:
                metaRotation = 0;
                break;
            case EAST:
                metaRotation = 90;
                break;
            case SOUTH:
                metaRotation = 180;
                break;
            default:
                metaRotation = 270;
                break;
        }
        GlStateManager.rotate(metaRotation, 0, 1, 0);
        return metaRotation;
    }

    public static final double[] sin;
    public static final double[] cos;
    public static final double[] tan;
    public static final int CIRCLE_POINTS = 500;

    /*
     * Initializes the sin,cos and tan variables, so that they can be used without having to calculate them every time (render tick).
     */
    static {
        sin = new double[CIRCLE_POINTS];
        cos = new double[CIRCLE_POINTS];
        tan = new double[CIRCLE_POINTS];

        for (int i = 0; i < CIRCLE_POINTS; i++) {
            double angle = 2 * Math.PI * i / CIRCLE_POINTS;
            sin[i] = Math.sin(angle);
            cos[i] = Math.cos(angle);
            tan[i] = Math.tan(angle);
        }
    }

    public static List<String> convertStringIntoList(String text) {
        return convertStringIntoList(text, GuiConstants.MAX_CHAR_PER_LINE_LEFT);
    }

    /**
     * This method takes one long string, and cuts it into lines which have
     * a maxCharPerLine and returns it in a String list.
     * It also preserves color formats. '\n' can be used to force a carriage
     * return.
     */
    public static List<String> convertStringIntoList(String text, int maxCharPerLine) {
        StringTokenizer tok = new StringTokenizer(text, " ");
        StringBuilder output = new StringBuilder(text.length());
        List<String> textList = new ArrayList<>();
        String color = "";
        int lineLen = 0;
        while (tok.hasMoreTokens()) {
            String word = tok.nextToken();
            if (word.contains("\u00a7")) { // if there is a text formatter present.
                for (int i = 0; i < word.length() - 1; i++) {
                    if (word.substring(i, i + 2).contains("\u00a7"))
                        color = word.substring(i, i + 2); // retrieve the color format
                }
                lineLen -= 2;// don't count a color formatter with the line length.
            }
            if (lineLen + word.length() > maxCharPerLine || word.contains("\\n")) {
                word = word.replace("\\n", "");
                textList.add(output.toString());
                output.delete(0, output.length());
                output.append(color);
                lineLen = 0;
            } else if (lineLen > 0) {
                output.append(" ");
                lineLen++;
            }
            output.append(word);
            lineLen += word.length();
        }
        textList.add(output.toString());
        return textList;
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
     * Takes in any integer, and converts it into a string with a additional postfix if needed. 2300 will convert into 2k for instance.
     *
     * @param amount an integer quantity
     * @return a formatted string representation
     */
    public static String convertAmountToString(int amount) {
        if (amount < 1000) {
            return amount + "";
        } else {
            return amount / 1000 + "k";
        }
    }

    /**
     * Rounds numbers down at the given decimal. 1.234 with decimal 1 will result in a string holding "1.2"
     *
     * @param value a double-precison quantity
     * @param decimals number of digits to the right of the decimal point
     * @return a formatted string representation
     */
    public static String roundNumberTo(double value, int decimals) {
        double ret = roundNumberToDouble(value, decimals);
        if (decimals == 0) {
            return "" + (int) ret;
        } else {
            return "" + ret;
        }
    }

    public static double roundNumberToDouble(double value, int decimals) {
        return Math.round(value * Math.pow(10, decimals)) / Math.pow(10, decimals);
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
     * used to compare two floats which are tested for having (almost) the same value
     */
    public static boolean areFloatsEqual(float f1, float f2) {
        return areFloatsEqual(f1, f2, 0.001F);
    }

    public static boolean areFloatsEqual(float f1, float f2, float maxDifference) {
        return Math.abs(f1 - f2) < maxDifference;
    }

    /**
     * Returns the maximum length of characters that an item name has of all the stacks given. Used to know on how many to sort on
     * bubblesorting the names.
     *
     * @param inventoryStacks
     * @return
     */
    private static int getMaxItemNameLength(ItemStack[] inventoryStacks) {
        int maxLength = 0;
        for (ItemStack iStack : inventoryStacks) {
            if (!iStack.isEmpty()) maxLength = Math.max(maxLength, iStack.getDisplayName().length());
        }
        return maxLength;
    }

    /**
     * Bubblesorts the itemstacks alphabetical, on the given charIndex. when the index is 2 for example, the stack with an item name
     * that has a 'B' as second letter will sort in front of a name with a 'D' as second letter.
     *
     * @param stackArray
     * @param charIndex
     */
    private static void bubbleSortOnCharIndex(ItemStack[] stackArray, int charIndex) {
        for (int i = 0; i < stackArray.length - 1; i++) {
            for (int j = 1; j < stackArray.length - i; j++) {
                boolean higherStackTooShort = stackArray[j - 1].isEmpty() || stackArray[j - 1].getDisplayName().length() <= charIndex;
                boolean lowerStackTooShort = stackArray[j].isEmpty() || stackArray[j].getDisplayName().length() <= charIndex;
                if (stackArray[j - 1].isEmpty() || !stackArray[j].isEmpty() && (lowerStackTooShort || higherStackTooShort || stackArray[j - 1].getDisplayName().charAt(charIndex) > stackArray[j].getDisplayName().charAt(charIndex))) {
                    ItemStack temp = stackArray[j - 1];
                    stackArray[j - 1] = stackArray[j];
                    stackArray[j] = temp;
                }
            }
        }
    }

    /**
     * Sorts the stacks given alphabetically, combines them (so 2x64 will become 1x128), and adds the strings into the given string list.
     *
     * @param textList string list to add information to
     * @param originalStacks array of item stacks to sort & combine
     */
    public static void sortCombineItemStacksAndToString(List<String> textList, ItemStack[] originalStacks) {
        ItemStack[] stacks = new ItemStack[originalStacks.length];
        Arrays.setAll(stacks, value -> originalStacks[value].copy());

        int maxItemNameLength = getMaxItemNameLength(stacks);
        for (int i = maxItemNameLength - 1; i >= 0; i--) {
            bubbleSortOnCharIndex(stacks, i);
        }
        int itemCount = 0;
        ItemStack oldItemStack = ItemStack.EMPTY;
        List<ItemStack> oldInventoryItems = null;
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                if (oldItemStack.isEmpty() || !stack.isItemEqual(oldItemStack) || oldInventoryItems != null && oldInventoryItems.size() > 0) {
                    if (!oldItemStack.isEmpty()) {
                        textList.add("\u2022 " + PneumaticCraftUtils.convertAmountToString(itemCount) + " x " + oldItemStack.getDisplayName());
                    }
                    if (oldInventoryItems != null) {
                        int oldSize = textList.size();
                        sortCombineItemStacksAndToString(textList, oldInventoryItems.toArray(new ItemStack[0]));
                        for (int i = oldSize; i < textList.size(); i++) {
                            textList.set(i, textList.get(i).replace('\u2022', '\u21b3'));
                        }
                    }
                    oldItemStack = stack;
                    itemCount = stack.getCount();
                } else {
                    itemCount += stack.getCount();
                }
                oldInventoryItems = getStacksInItem(stack);
            }
        }
        if (itemCount > 0 && !oldItemStack.isEmpty()) {
            textList.add("\u2022 " + PneumaticCraftUtils.convertAmountToString(itemCount) + " x " + oldItemStack.getDisplayName());
            if (oldInventoryItems != null) {
                int oldSize = textList.size();
                sortCombineItemStacksAndToString(textList, oldInventoryItems.toArray(new ItemStack[0]));
                for (int i = oldSize; i < textList.size(); i++) {
                    textList.set(i, textList.get(i).replace('\u2022', '\u21b3'));
                }
            }
        }
    }

    public static List<ItemStack> getStacksInItem(@Nonnull ItemStack item) {
        List<ItemStack> items = new ArrayList<>();
        if (item.getItem() instanceof IInventoryItem && !inventoryItemBlacklist.contains(item.getItem())) {
            try {
                ((IInventoryItem) item.getItem()).getStacksInItem(item, items);
            } catch (Throwable e) {
                Log.error("An InventoryItem crashed:");
                e.printStackTrace();
                inventoryItemBlacklist.add(item.getItem());
            }
        } else {
            Iterator<IInventoryItem> iterator = ItemRegistry.getInstance().inventoryItems.iterator();
            while (iterator.hasNext()) {
                try {
                    iterator.next().getStacksInItem(item, items);
                } catch (Throwable e) {
                    Log.error("An InventoryItem crashed:");
                    e.printStackTrace();
                    iterator.remove();
                }
            }
        }
        return items;
    }

    /**
     * Returns the redstone level at the given position. Use this when you don't care what side(s) the signal is
     * coming from, just the level of the signal at the position.
     *
     * @param world the world
     * @param pos the position to check
     * @return the redstone level
     */
    public static int getRedstoneLevel(World world, BlockPos pos) {
        return world != null ? world.getRedstonePowerFromNeighbors(pos) : 0;
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

    public static int getProtectingSecurityStations(World world, BlockPos pos, EntityPlayer player, boolean showRangeLines, boolean placementRange) {
        int blockingStations = 0;
        Iterator<TileEntitySecurityStation> iterator = getSecurityStations(world, pos, placementRange).iterator();
        for (TileEntitySecurityStation station; iterator.hasNext();) {
            station = iterator.next();
            if (!station.doesAllowPlayer(player)) {
                blockingStations++;
                if (showRangeLines) station.showRangeLines();
            }
        }
        return blockingStations;
    }

    public static Stream<TileEntitySecurityStation> getSecurityStations(final World world, final BlockPos pos, final boolean placementRange) {
        return GlobalTileEntityCacheManager.getInstance().securityStations
                                                         .stream()
                                                         .filter(station -> isValidAndInRange(pos, placementRange, station));     
    }
    
    private static boolean isValidAndInRange(BlockPos pos, boolean placementRange, TileEntitySecurityStation station){
        if (station.hasValidNetwork()) {
            AxisAlignedBB aabb = station.getAffectingAABB();
            if(placementRange) aabb = aabb.grow(16);
            return aabb.contains(new Vec3d(pos));
        }
        return false;
    }

    public static RayTraceResult getEntityLookedObject(EntityLivingBase entity) {
        return getEntityLookedObject(entity, 4.5F);
    }

    public static RayTraceResult getEntityLookedObject(EntityLivingBase entity, float maxDistance) {
        Pair<Vec3d, Vec3d> vecs = getStartAndEndLookVec(entity, maxDistance);
        return entity.world.rayTraceBlocks(vecs.getLeft(), vecs.getRight());
    }

    public static Pair<Vec3d, Vec3d> getStartAndEndLookVec(EntityLivingBase entity) {
        return getStartAndEndLookVec(entity, 4.5F);
    }

    public static Pair<Vec3d, Vec3d> getStartAndEndLookVec(EntityLivingBase entity, float maxDistance) {
        Vec3d entityVec;
        if (entity.world.isRemote && entity instanceof EntityPlayer) {
            entityVec = new Vec3d(entity.posX, entity.posY + 1.6200000000000001D, entity.posZ);
        } else {
            entityVec = new Vec3d(entity.posX, entity.posY + entity.getEyeHeight() - (entity.isSneaking() ? 0.08 : 0), entity.posZ);
        }
        Vec3d entityLookVec = entity.getLook(1.0F);
        Vec3d maxDistVec = entityVec.add(entityLookVec.x * maxDistance, entityLookVec.y * maxDistance, entityLookVec.z * maxDistance);
        return new ImmutablePair<>(entityVec, maxDistVec);
    }

    public static BlockPos getEntityLookedBlock(EntityLivingBase entity, float maxDistance) {
        RayTraceResult hit = getEntityLookedObject(entity, maxDistance);
        if (hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK) {
            return null;
        }
        return hit.getBlockPos();
    }

    @Nonnull
    public static ItemStack exportStackToInventory(ICapabilityProvider provider, ItemStack stack, EnumFacing side) {
        if (provider.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)) {
            IItemHandler handler = provider.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
            return ItemHandlerHelper.insertItem(handler, stack, false);
        }
        return stack;
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

    public static double distBetween(Vec3d vec, double x, double y, double z) {
        return distBetween(vec.x, vec.y, vec.z, x, y, z);
    }

    public static double distBetween(Vec3d vec1, Vec3d vec2) {
        return distBetween(vec1, vec2.x, vec2.y, vec2.z);
    }

    public static boolean areStacksEqual(@Nonnull ItemStack stack1, @Nonnull ItemStack stack2, boolean checkMeta, boolean checkNBT, boolean checkOreDict, boolean checkModSimilarity) {
        if (stack1.isEmpty() && stack2.isEmpty()) return true;
        if (stack1.isEmpty() || stack2.isEmpty()) return false;

        if (checkModSimilarity) {
            String mod1 = stack1.getItem().getRegistryName().getNamespace();
            String mod2 = stack2.getItem().getRegistryName().getNamespace();
            return mod1.equals(mod2);
        }
        if (checkOreDict) {
            return isSameOreDictStack(stack1, stack2);
        }

        if (stack1.getItem() != stack2.getItem()) return false;

        boolean metaOK = !checkMeta || (stack1.getItemDamage() == stack2.getItemDamage());
        boolean nbtOK = !checkNBT || (stack1.hasTagCompound() ? stack1.getTagCompound().equals(stack2.getTagCompound()) : !stack2.hasTagCompound());

        return metaOK && nbtOK;
    }

    public static boolean isSameOreDictStack(ItemStack stack1, ItemStack stack2) {
        int[] oredictIds = OreDictionary.getOreIDs(stack1);
        for (int oredictId : oredictIds) {
            List<ItemStack> oreDictStacks = OreDictionary.getOres(OreDictionary.getOreName(oredictId));
            for (ItemStack oreDictStack : oreDictStacks) {
                if (OreDictionary.itemMatches(oreDictStack, stack2, false)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isBlockLiquid(Block block) {
        return block instanceof BlockLiquid || block instanceof IFluidBlock;
    }

    public static String getOrientationName(EnumFacing dir) {
        switch (dir) {
            case UP:
                return "Top";
            case DOWN:
                return "Bottom";
            case NORTH:
                return "North";
            case SOUTH:
                return "South";
            case EAST:
                return "East";
            case WEST:
                return "West";
            default:
                return "Unknown";
        }
    }

    public static void dropInventory(IItemHandler inventory, World world, double x, double y, double z) {
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack itemStack = inventory.getStackInSlot(i);
            if (itemStack.getCount() > 0) {
                dropItemOnGround(itemStack, world, x, y, z);
            }
        }
    }

    public static void dropItemOnGround(ItemStack stack, World world, BlockPos pos) {
        dropItemOnGround(stack, world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    public static void dropItemOnGround(ItemStack stack, World world, double x, double y, double z) {
        float dX = world.rand.nextFloat() * 0.8F + 0.1F;
        float dY = world.rand.nextFloat() * 0.8F + 0.1F;
        float dZ = world.rand.nextFloat() * 0.8F + 0.1F;

        EntityItem entityItem = new EntityItem(world, x + dX, y + dY, z + dZ, new ItemStack(stack.getItem(), stack.getCount(), stack.getItemDamage()));

        if (stack.hasTagCompound()) {
            entityItem.getItem().setTagCompound(stack.getTagCompound().copy());
        }

        float factor = 0.05F;
        entityItem.motionX = world.rand.nextGaussian() * factor;
        entityItem.motionY = world.rand.nextGaussian() * factor + 0.2F;
        entityItem.motionZ = world.rand.nextGaussian() * factor;
        world.spawnEntity(entityItem);
        stack.setCount(0);
    }

    public static void dropItemOnGroundPrecisely(ItemStack stack, World world, double x, double y, double z) {
        EntityItem entityItem = new EntityItem(world, x, y, z, new ItemStack(stack.getItem(), stack.getCount(), stack.getItemDamage()));

        if (stack.hasTagCompound()) {
            entityItem.getItem().setTagCompound(stack.getTagCompound().copy());
        }
        entityItem.motionX = 0;
        entityItem.motionY = 0;
        entityItem.motionZ = 0;
        world.spawnEntity(entityItem);
        stack.setCount(0);
    }

    public static TileEntity getTileEntity(BlockPos pos, int dimension) {
        World world = DimensionManager.getWorld(dimension);
        if (world != null && world.isBlockLoaded(pos)) {
            return world.getTileEntity(pos);
        }
        return null;
    }

    public static EntityPlayer getPlayerFromId(String uuid) {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(UUID.fromString(uuid));
    }

    public static EntityPlayer getPlayerFromName(String name) {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(name);
    }

    public static boolean isPlayerOp(EntityPlayer player) {
        return player.canUseCommand(2, "PneumaticCraftIsPlayerOp");
    }

    private static RayTraceResult raytraceEntityBlocks(EntityLivingBase entity, double range) {
        Pair<Vec3d, Vec3d> startAndEnd = getStartAndEndLookVec(entity, (float) range);
        return entity.world.rayTraceBlocks(startAndEnd.getLeft(), startAndEnd.getRight(), false, false, true);
    }

    public static RayTraceResult getMouseOverServer(EntityLivingBase lookingEntity, double range) {

        RayTraceResult mop = raytraceEntityBlocks(lookingEntity, range);
        double d1 = range;
        Pair<Vec3d, Vec3d> startAndEnd = getStartAndEndLookVec(lookingEntity, (float) range);
        Vec3d vec3 = startAndEnd.getLeft();

        if (mop != null) {
            d1 = mop.hitVec.distanceTo(vec3);
        }

        Vec3d vec31 = lookingEntity.getLookVec();
        Vec3d vec32 = startAndEnd.getRight();
        Entity pointedEntity = null;
        Vec3d vec33 = null;
        float f1 = 1.0F;
        List<Entity> list = lookingEntity.world.getEntitiesWithinAABBExcludingEntity(lookingEntity, lookingEntity.getEntityBoundingBox()
                .grow(vec31.x * range, vec31.y * range, vec31.z * range).grow(f1, f1, f1));
        double d2 = d1;

        for (Entity entity : list) {
            if (entity.canBeCollidedWith()) {
                float f2 = entity.getCollisionBorderSize();
                AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox().grow(f2, f2, f2);
                RayTraceResult movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);

                if (axisalignedbb.contains(vec3)) {
                    if (0.0D < d2 || d2 == 0.0D) {
                        pointedEntity = entity;
                        vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                        d2 = 0.0D;
                    }
                } else if (movingobjectposition != null) {
                    double d3 = vec3.distanceTo(movingobjectposition.hitVec);

                    if (d3 < d2 || d2 == 0.0D) {
                        if (entity == entity.getRidingEntity() && !entity.canRiderInteract()) {
                            if (d2 == 0.0D) {
                                pointedEntity = entity;
                                vec33 = movingobjectposition.hitVec;
                            }
                        } else {
                            pointedEntity = entity;
                            vec33 = movingobjectposition.hitVec;
                            d2 = d3;
                        }
                    }
                }
            }
        }

        if (pointedEntity != null && (d2 < d1 || mop == null)) {
            mop = new RayTraceResult(pointedEntity, vec33);
        }
        return mop;
    }

    public static PathFinder getPathFinder() {
        WalkNodeProcessor processor = new WalkNodeProcessor();
        processor.setCanEnterDoors(true);
        return new PathFinder(processor);
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
    public static boolean tryPlaceBlock(World w, BlockPos pos, EntityPlayer player, EnumFacing face, IBlockState newState) {
        BlockSnapshot snapshot = BlockSnapshot.getBlockSnapshot(w, pos);
        BlockEvent.PlaceEvent event = ForgeEventFactory.onPlayerBlockPlace(player, snapshot, face, EnumHand.MAIN_HAND);
        if (!event.isCanceled()) {
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
    public static EntityLiving createDummyEntity(EntityPlayer player) {
        EntityZombie dummy = new EntityZombie(player.world) {
            @Override
            protected void applyEntityAttributes() {
                super.applyEntityAttributes();
                this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(CoordTrackUpgradeHandler.SEARCH_RANGE);
            }
        };
        dummy.setPosition(player.posX, player.posY, player.posZ);
        return dummy;
    }

    /**
     * Convenience method, ported from 1.8.  Consume one item from the player's inventory.
     *
     * @param inv player's inventory
     * @param item item to consume
     * @return true if an item was consumed
     */
    public static boolean consumeInventoryItem(InventoryPlayer inv, Item item) {
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

    public static boolean consumeInventoryItem(InventoryPlayer inv, ItemStack item) {
        for (int i = 0; i < inv.mainInventory.size(); ++i) {
            if (ItemStack.areItemsEqual(inv.mainInventory.get(i), item)) {
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
     * Gets a tile entity without risking creation of a new one, which can cause all sorts of problems if called
     * from Block#getActualState or Block#getExtendedState
     *
     * @param world the world
     * @param pos the block position
     * @return the tile entity, or null if there is none
     */
    public static TileEntity getTileEntitySafely(IBlockAccess world, BlockPos pos) {
        return world instanceof ChunkCache ?
                ((ChunkCache) world).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK) :
                world.getTileEntity(pos);
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

    public static String xlate(String s) {
        return PneumaticCraftRepressurized.proxy.xlate(s);
    }
}
