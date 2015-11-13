package pneumaticCraft.common.util;

import ic2.api.item.IC2Items;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.PatternSyntaxException;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.oredict.OreDictionary;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import pneumaticCraft.api.item.IInventoryItem;
import pneumaticCraft.common.PneumaticCraftAPIHandler;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.thirdparty.ModInteractionUtils;
import pneumaticCraft.common.tileentity.TileEntitySecurityStation;
import pneumaticCraft.lib.Log;
import pneumaticCraft.lib.ModIds;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PneumaticCraftUtils{

    private static Random rand = new Random();
    private static final List<Item> inventoryItemBlacklist = new ArrayList<Item>();

    /**
     * Returns the ForgeDirection of the facing of the entity given.
     * @param entity
     * @param includeUpAndDown false when UP/DOWN should not be included.
     * @return
     */
    public static ForgeDirection getDirectionFacing(EntityLivingBase entity, boolean includeUpAndDown){
        double yaw = entity.rotationYaw;
        while(yaw < 0)
            yaw += 360;
        yaw = yaw % 360;
        if(includeUpAndDown) {
            if(entity.rotationPitch > 45) return ForgeDirection.DOWN;
            else if(entity.rotationPitch < -45) return ForgeDirection.UP;
        }
        if(yaw < 45) return ForgeDirection.SOUTH;
        else if(yaw < 135) return ForgeDirection.WEST;
        else if(yaw < 225) return ForgeDirection.NORTH;
        else if(yaw < 315) return ForgeDirection.EAST;

        else return ForgeDirection.SOUTH;
    }

    /**
     * Rotates the render matrix dependant on the given metadata of a block. Used in the render methods of many PneumaticCraft TileEntities.
     * @param metadata
     * @return
     */
    @SideOnly(Side.CLIENT)
    public static double rotateMatrixByMetadata(int metadata){
        ForgeDirection facing = ForgeDirection.getOrientation(metadata & 7);
        double metaRotation;
        switch(facing){
            case UP:
                metaRotation = 0;
                GL11.glRotated(90, 1, 0, 0);
                GL11.glTranslated(0, -1, -1);
                break;
            case DOWN:
                metaRotation = 0;
                GL11.glRotated(-90, 1, 0, 0);
                GL11.glTranslated(0, -1, 1);
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
        GL11.glRotated(metaRotation, 0, 1, 0);
        return metaRotation;
    }

    public static double[] sin;
    public static double[] cos;
    public static double[] tan;
    public static final int circlePoints = 500;

    /**
     * Initializes the sin,cos and tan variables, so that they can't be used without having to calculate them every time (render tick).
     */
    static {
        sin = new double[circlePoints];
        cos = new double[circlePoints];
        tan = new double[circlePoints];

        for(int i = 0; i < circlePoints; i++) {
            double angle = 2 * Math.PI * i / circlePoints;
            sin[i] = Math.sin(angle);
            cos[i] = Math.cos(angle);
            tan[i] = Math.tan(angle);
        }
    }

    /** this method takes one very long string, and cuts it into lines which have
     a maxCharPerLine and returns it in a String list.
     it also preserves color formats. \n can be used to force a carriage
     return.
     */
    public static List<String> convertStringIntoList(String text, int maxCharPerLine){
        StringTokenizer tok = new StringTokenizer(text, " ");
        StringBuilder output = new StringBuilder(text.length());
        List<String> textList = new ArrayList<String>();
        String color = "";
        int lineLen = 0;
        while(tok.hasMoreTokens()) {
            String word = tok.nextToken();
            if(word.contains("\u00a7")) {// if there is a text formatter
                                         // present.
                for(int i = 0; i < word.length() - 1; i++)
                    if(word.substring(i, i + 2).contains("\u00a7")) color = word.substring(i, i + 2); // retrieve
                                                                                                      // the
                                                                                                      // color
                                                                                                      // format.
                lineLen -= 2;// don't count a color formatter with the line
                             // length.
            }
            if(lineLen + word.length() > maxCharPerLine || word.contains("\\n")) {
                word = word.replace("\\n", "");
                textList.add(output.toString());
                output.delete(0, output.length());
                output.append(color);
                lineLen = 0;
            } else if(lineLen > 0) {
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
     * @param ticks
     * @param fraction When true, 30 ticks will show as '1.5s' instead of '1s'.
     * @return
     */
    public static String convertTicksToMinutesAndSeconds(int ticks, boolean fraction){
        String part = ticks % 20 * 5 + "";
        if(part.length() < 2) part = "0" + part;
        ticks /= 20;// first convert to seconds.
        if(ticks < 60) {
            return ticks + (fraction ? "." + part : "") + "s";
        } else {
            return ticks / 60 + "m " + ticks % 60 + "s";
        }
    }

    /**
     * Takes in any integer, and converts it into a string with a additional postfix if needed. 2300 will convert into 2k for instance.
     * @param amount
     * @return
     */
    public static String convertAmountToString(int amount){
        if(amount < 1000) {
            return amount + "";
        } else {
            return amount / 1000 + "k";
        }
    }

    /**
     * Rounds numbers down at the given decimal. 1.234 with decimal 1 will result in a string holding "1.2"
     * @param value
     * @param decimals
     * @return
     */
    public static String roundNumberTo(double value, int decimals){
        double ret = roundNumberToDouble(value, decimals);
        if(decimals == 0) {
            return "" + (int)ret;
        } else {
            return "" + ret;
        }
    }

    public static double roundNumberToDouble(double value, int decimals){
        return Math.round(value * Math.pow(10, decimals)) / Math.pow(10, decimals);
    }

    /**
     * Rounds numbers down at the given decimal. 1.234 with decimal 1 will result in a string holding "1.2"
     * @param value
     * @param decimals
     * @return
     */
    public static String roundNumberTo(float value, int decimals){
        return "" + Math.round(value * Math.pow(10, decimals)) / Math.pow(10, decimals);
    }

    /**
     * used to compare two floats which are tested for having (almost) the same value
     */
    public static boolean areFloatsEqual(float f1, float f2){
        return areFloatsEqual(f1, f2, 0.001F);
    }

    public static boolean areFloatsEqual(float f1, float f2, float maxDifference){
        return Math.abs(f1 - f2) < maxDifference;
    }

    /**
     * Returns a part of a string, dependant on the progress. When progress is 20, and maxProgress is 100, 1/5th of the string will be returned.
     * Used to nicely display HUD messages.
     * @param string
     * @param progress
     * @param maxProgress
     * @return
     */
    public static String getPartOfString(String string, int progress, int maxProgress){
        if(progress > maxProgress) return string;
        return string.substring(0, string.length() * progress / maxProgress);
    }

    /**
     * Returns the maximum length of characters that an item name has of all the stacks given. Used to know on how many to sort on
     * bubblesorting the names.
     * @param inventoryStacks
     * @return
     */
    public static int getMaxItemNameLength(ItemStack[] inventoryStacks){
        int maxLength = 0;
        for(ItemStack iStack : inventoryStacks) {
            if(iStack != null) maxLength = Math.max(maxLength, iStack.getDisplayName().length());
        }
        return maxLength;
    }

    /**
     * Bubblesorts the itemstacks alphabetical, on the given charIndex. when the index is 2 for example, the stack with an item name
     * that has a 'B' as second letter will sort in front of a name with a 'D' as second letter.
     * @param stackArray
     * @param charIndex
     */
    public static void bubbleSortOnCharIndex(ItemStack[] stackArray, int charIndex){
        for(int i = 0; i < stackArray.length - 1; i++) {
            for(int j = 1; j < stackArray.length - i; j++) {
                boolean higherStackTooShort = stackArray[j - 1] != null ? stackArray[j - 1].getDisplayName().length() <= charIndex : true;
                boolean lowerStackTooShort = stackArray[j] != null ? stackArray[j].getDisplayName().length() <= charIndex : true;
                if(stackArray[j - 1] == null || stackArray[j] != null && (lowerStackTooShort || higherStackTooShort || stackArray[j - 1].getDisplayName().charAt(charIndex) > stackArray[j].getDisplayName().charAt(charIndex))) {
                    ItemStack temp = stackArray[j - 1];
                    stackArray[j - 1] = stackArray[j];
                    stackArray[j] = temp;
                }
            }
        }
    }

    public static void sortStringArrayAlphabetically(String[] strings){
        int j;
        boolean notDone = true; // will determine when the sort is finished
        String temp;
        while(notDone) {
            notDone = false;
            for(j = 0; j < strings.length - 1; j++) {
                if(strings[j].compareToIgnoreCase(strings[j + 1]) > 0) { // ascending sort
                    temp = strings[j];
                    strings[j] = strings[j + 1]; // swapping
                    strings[j + 1] = temp;
                    notDone = true;
                }
            }
        }
    }

    /**
     * Sorts the stacks given alphabetically, combines them (so 2x64 will become 1x128), and adds the strings into the given string list.
     * @param textList
     * @param stacks
     */
    public static void sortCombineItemStacksAndToString(List<String> textList, ItemStack[] originalStacks){
        ItemStack[] stacks = new ItemStack[originalStacks.length];
        for(int i = 0; i < originalStacks.length; i++) {
            if(originalStacks[i] != null) stacks[i] = originalStacks[i].copy();
        }

        int maxItemNameLength = getMaxItemNameLength(stacks);
        for(int i = maxItemNameLength - 1; i >= 0; i--)
            bubbleSortOnCharIndex(stacks, i);
        int itemCount = 0;
        ItemStack oldItemStack = null;
        List<ItemStack> oldInventoryItems = null;
        for(ItemStack stack : stacks) {
            if(stack != null) {
                if(oldItemStack == null || !stack.isItemEqual(oldItemStack) || oldInventoryItems != null && oldInventoryItems.size() > 0) {
                    if(oldItemStack != null) textList.add("-" + PneumaticCraftUtils.convertAmountToString(itemCount) + " " + oldItemStack.getDisplayName());
                    if(oldInventoryItems != null) {
                        int oldSize = textList.size();
                        sortCombineItemStacksAndToString(textList, oldInventoryItems.toArray(new ItemStack[oldInventoryItems.size()]));
                        for(int i = oldSize; i < textList.size(); i++) {
                            textList.set(i, ">> " + textList.get(i));
                        }
                    }
                    oldItemStack = stack;
                    itemCount = stack.stackSize;
                } else {
                    itemCount += stack.stackSize;
                }
                oldInventoryItems = getStacksInItem(stack);
            }
        }
        if(itemCount > 0 && oldItemStack != null) {
            textList.add("-" + PneumaticCraftUtils.convertAmountToString(itemCount) + " " + oldItemStack.getDisplayName());
            if(oldInventoryItems != null) {
                int oldSize = textList.size();
                sortCombineItemStacksAndToString(textList, oldInventoryItems.toArray(new ItemStack[oldInventoryItems.size()]));
                for(int i = oldSize; i < textList.size(); i++) {
                    textList.set(i, ">> " + textList.get(i));
                }
            }
        }
    }

    public static List<ItemStack> getStacksInItem(ItemStack item){
        List<ItemStack> items = new ArrayList<ItemStack>();
        if(item.getItem() instanceof IInventoryItem && !inventoryItemBlacklist.contains(item.getItem())) {
            try {
                ((IInventoryItem)item.getItem()).getStacksInItem(item, items);
            } catch(Throwable e) {
                Log.error("An InventoryItem crashed:");
                e.printStackTrace();
                inventoryItemBlacklist.add(item.getItem());
            }
        } else {
            Iterator<IInventoryItem> iterator = PneumaticCraftAPIHandler.getInstance().inventoryItems.iterator();
            while(iterator.hasNext()) {
                try {
                    iterator.next().getStacksInItem(item, items);
                } catch(Throwable e) {
                    Log.error("An InventoryItem crashed:");
                    e.printStackTrace();
                    iterator.remove();
                }
            }
        }
        return items;
    }

    /**
     * Returns the redstone level at the given coordinate. Useful when triggering on analog levels. When for example a redstone torch is attached, normally getBlockPowerInput() would return 0.
     * @param world
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static int getRedstoneLevel(World world, int x, int y, int z){
        int analogLevel = world.getBlockPowerInput(x, y, z);
        if(analogLevel == 0) {
            if(world.isBlockIndirectlyGettingPowered(x, y, z)) return 15;
        }
        return analogLevel;
    }

    /**
     * This will return true if the given item ID is the same as the item id that can be retrieved from IC2's item key.
     * @param id
     * @param ic2ItemKey
     * @return
     */
    @Optional.Method(modid = ModIds.INDUSTRIALCRAFT)
    public static boolean isIC2Item(Item id, String ic2ItemKey){
        ItemStack ic2Item = IC2Items.getItem(ic2ItemKey);
        return ic2Item != null && ic2Item.getItem() == id;
    }

    /**
     * Returns true if the given item ID is a IC2 upgrade.
     * @param id
     * @return
     */
    @Optional.Method(modid = ModIds.INDUSTRIALCRAFT)
    public static boolean isIC2Upgrade(Item id){
        return isIC2Item(id, "overclockerUpgrade") || isIC2Item(id, "transformerUpgrade") || isIC2Item(id, "energyStorageUpgrade");
    }

    public enum EnumBuildcraftModule{
        BUILDERS, CORE, ENERGY, FACTORY, SILICON, TRANSPORT
    }

    public static ItemStack getBuildcraftItemStack(EnumBuildcraftModule module, String itemName){

        try {
            Class buildcraftItems = Class.forName(getItemClassForModule(module));

            Object ret = buildcraftItems.getField(itemName).get(null);

            if(ret instanceof Item) {
                return new ItemStack((Item)ret);
            } else if(ret instanceof Block) {
                return new ItemStack((Block)ret);
            } else {
                return null;
            }
        } catch(Exception e) {
            Log.warning("Tried to retrieve a Buildcraft item which failed. Tried to retrieve: " + itemName + ", from module " + getItemClassForModule(module));

            return null;
        }
    }

    private static String getItemClassForModule(EnumBuildcraftModule module){
        switch(module){
            case BUILDERS:
                return "buildcraft.BuildCraftBuilders";
            case CORE:
                return "buildcraft.BuildCraftCore";
            case ENERGY:
                return "buildcraft.BuildCraftEnergy";
            case FACTORY:
                return "buildcraft.BuildCraftFactory";
            case SILICON:
                return "buildcraft.BuildCraftSilicon";
            case TRANSPORT:
                return "buildcraft.BuildCraftTransport";
        }
        return "";
    }

    public static boolean isRenderIDCamo(int renderID){
        return PneumaticCraftAPIHandler.getInstance().concealableRenderIds.contains(renderID);
    }

    public static int getProtectingSecurityStations(World world, int x, int y, int z, EntityPlayer player, boolean showRangeLines, boolean placementRange){
        int blockingStations = 0;
        for(TileEntitySecurityStation station : getSecurityStations(world, x, y, z, placementRange)) {
            if(!station.doesAllowPlayer(player)) {
                blockingStations++;
                if(showRangeLines) station.showRangeLines();
            }
        }
        return blockingStations;
    }

    public static Iterable<TileEntitySecurityStation> getSecurityStations(final World world, final int x, final int y, final int z, final boolean placementRange){
        return new Iterable<TileEntitySecurityStation>(){
            @Override
            public Iterator<TileEntitySecurityStation> iterator(){
                return new Iterator<TileEntitySecurityStation>(){

                    private final int range = placementRange ? 32 : 16;
                    private int i = x - range;
                    private int j = z - range;
                    private TileEntitySecurityStation curStation;
                    private int chunkTileEntityIndex = -1;

                    @Override
                    public boolean hasNext(){
                        if(curStation != null) return true;
                        for(; i <= x + range; i += 16) {
                            for(; j <= z + range; j += 16) {
                                Chunk chunk = world.getChunkFromBlockCoords(i, j);
                                int curIndex = 0;
                                for(TileEntity te : (Iterable<TileEntity>)chunk.chunkTileEntityMap.values()) {
                                    if(curIndex > chunkTileEntityIndex && te instanceof TileEntitySecurityStation) {
                                        TileEntitySecurityStation station = (TileEntitySecurityStation)te;
                                        if(station.hasValidNetwork()) {
                                            if(Math.abs(station.xCoord - x) <= station.getSecurityRange() + (placementRange ? 16 : 0) && Math.abs(station.yCoord - y) <= station.getSecurityRange() + (placementRange ? 16 : 0) && Math.abs(station.zCoord - z) <= station.getSecurityRange() + (placementRange ? 16 : 0)) {
                                                curStation = station;
                                                chunkTileEntityIndex = curIndex;
                                                return true;
                                            }
                                        }
                                    }
                                    curIndex++;
                                }
                                chunkTileEntityIndex = -1;
                            }
                            j = z - range;
                        }
                        return false;
                    }

                    @Override
                    public TileEntitySecurityStation next(){
                        if(hasNext()) {
                            TileEntitySecurityStation station = curStation;
                            curStation = null;
                            return station;
                        } else {
                            throw new NoSuchElementException();
                        }
                    }

                    @Override
                    public void remove(){
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    public static MovingObjectPosition getEntityLookedObject(EntityLivingBase entity){
        return getEntityLookedObject(entity, 4.5F);
    }

    public static MovingObjectPosition getEntityLookedObject(EntityLivingBase entity, float maxDistance){
        Pair<Vec3, Vec3> vecs = getStartAndEndLookVec(entity, maxDistance);
        return entity.worldObj.rayTraceBlocks(vecs.getLeft(), vecs.getRight());
    }

    public static Pair<Vec3, Vec3> getStartAndEndLookVec(EntityLivingBase entity){
        return getStartAndEndLookVec(entity, 4.5F);
    }

    public static Pair<Vec3, Vec3> getStartAndEndLookVec(EntityLivingBase entity, float maxDistance){
        Vec3 entityVec;
        if(entity.worldObj.isRemote && entity instanceof EntityPlayer) {
            entityVec = Vec3.createVectorHelper(entity.posX, entity.posY + 1.6200000000000001D - entity.yOffset, entity.posZ);
        } else {
            entityVec = Vec3.createVectorHelper(entity.posX, entity.posY + entity.getEyeHeight() - entity.yOffset - (entity.isSneaking() ? 0.08 : 0), entity.posZ);
        }
        Vec3 entityLookVec = entity.getLook(1.0F);
        Vec3 maxDistVec = entityVec.addVector(entityLookVec.xCoord * maxDistance, entityLookVec.yCoord * maxDistance, entityLookVec.zCoord * maxDistance);
        return new ImmutablePair(entityVec, maxDistVec);
    }

    public static ChunkPosition getEntityLookedBlock(EntityLivingBase entity, float maxDistance){
        MovingObjectPosition hit = getEntityLookedObject(entity, maxDistance);
        if(hit == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            return null;
        }
        return new ChunkPosition(hit.blockX, hit.blockY, hit.blockZ);
    }

    public static boolean isEntityValidForFilter(String filter, Entity entity){
        try {
            return isEntityValidForFilterUnsafe(filter, entity);
        } catch(IllegalArgumentException e) {}
        return false;
    }

    public static boolean isEntityValidForFilterUnsafe(String filter, Entity entity) throws IllegalArgumentException{
        if(filter == null) return true;
        if(StringUtils.countMatches(filter, "(") != StringUtils.countMatches(filter, ")")) throw new IllegalArgumentException("Not an equal amount of opening and closing braces");
        String[] splits = filter.split("[(),]");
        for(int i = 0; i < splits.length; i++)
            splits[i] = splits[i].trim();
        if(!isEntityValidForName(splits[0], entity)) return false;
        for(int i = 1; i < splits.length; i++) {
            String[] modifier = splits[i].split("=");
            if(modifier.length == 2) {
                if(!isEntityValidForModifier(modifier[0].trim(), modifier[1].trim(), entity)) return false;
            } else {
                throw new IllegalArgumentException("No '=' sign in the modifier.");
            }
        }
        return true;
    }

    private static boolean isEntityValidForModifier(String modifier, String value, Entity entity) throws IllegalArgumentException{
        if(modifier.equalsIgnoreCase("age")) {
            if(entity instanceof EntityAgeable) {
                if(value.equalsIgnoreCase("adult")) {
                    return ((EntityAgeable)entity).getGrowingAge() >= 0;
                } else if(value.equalsIgnoreCase("baby")) {
                    return ((EntityAgeable)entity).getGrowingAge() < 0;
                } else {
                    throw new IllegalArgumentException(value + " doesn't match 'adult'/'baby'.");
                }
            } else {
                throw new IllegalArgumentException("This modifier can't be applied to this entity.");
            }
        } else if(modifier.equalsIgnoreCase("breedable")) {
            if(entity instanceof EntityAgeable) {
                if(value.equalsIgnoreCase("yes")) {
                    return ((EntityAgeable)entity).getGrowingAge() == 0;
                } else if(value.equalsIgnoreCase("no")) {
                    return ((EntityAgeable)entity).getGrowingAge() != 0;
                } else {
                    throw new IllegalArgumentException(value + " doesn't match 'yes'/'no'.");
                }
            } else {
                throw new IllegalArgumentException("This modifier can't be applied to this entity.");
            }
        }
        throw new IllegalArgumentException(modifier + " is not a valid modifier");
    }

    private static boolean isEntityValidForName(String filter, Entity entity) throws IllegalArgumentException{
        if(filter.equals("")) {
            return true;
        } else if(filter.startsWith("@")) {//entity type selection
            filter = filter.substring(1); //cut off the '@'.
            Class typeClass = null;
            if(filter.equals("mob")) {
                typeClass = EntityMob.class;
            } else if(filter.equals("animal")) {
                typeClass = EntityAnimal.class;
            } else if(filter.equals("living")) {
                typeClass = EntityLivingBase.class;
            } else if(filter.equals("player")) {
                typeClass = EntityPlayer.class;
            } else if(filter.equals("item")) {
                typeClass = EntityItem.class;
            } else if(filter.equals("minecart")) {
                typeClass = EntityMinecart.class;
            } else if(filter.equals("drone")) {
                typeClass = EntityDrone.class;
            }
            if(typeClass != null) {
                return typeClass.isAssignableFrom(entity.getClass());
            } else {
                throw new IllegalArgumentException(filter + " is not a valid entity type.");
            }
        } else {
            try {
                String regex = filter.toLowerCase().replaceAll(".", "[$0]").replace("[*]", ".*");//Wildcard regex
                return entity.getCommandSenderName().toLowerCase().matches(regex);//TODO when player, check if entity is tamed by the player (see EntityAIAvoidEntity for example)
            } catch(PatternSyntaxException e) {
                return entity.getCommandSenderName().toLowerCase().equals(filter.toLowerCase());
            }
        }
    }

    public static Method getDeclaredMethodIncludingSupertype(Class clazz, String methodName, Class... methodParms){
        while(!clazz.equals(Object.class)) {
            try {
                Method method = clazz.getDeclaredMethod(methodName, methodParms);
                return method;
            } catch(Exception e) {

            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    /**
     * Transfers the given stack to the given inventory. The returned stack is the leftover if there is any.
     * @param inv
     * @param stack
     * @return
     */
    public static ItemStack exportStackToInventory(IInventory inv, ItemStack stack, ForgeDirection side){
        return TileEntityHopper.func_145889_a(inv, stack, side.ordinal());
    }

    public static ItemStack exportStackToInventory(TileEntity te, ItemStack stack, ForgeDirection side){
        if(te instanceof IInventory) {
            return TileEntityHopper.func_145889_a((IInventory)te, stack, side.ordinal());
        } else {
            stack = ModInteractionUtils.getInstance().exportStackToTEPipe(te, stack, side);
            stack = ModInteractionUtils.getInstance().exportStackToBCPipe(te, stack, side);
            return stack;
        }
    }

    public static boolean isOutputInventory(TileEntity te){
        return te instanceof IInventory || ModInteractionUtils.getInstance().isBCPipe(te) || ModInteractionUtils.getInstance().isTEPipe(te);
    }

    /**
     * Returns a set of integers of slots that are accessible for the given sides.
     * @param inventory
     * @param accessibleSides a boolean[6], representing for each of the sides if it is accessible or not.
     * @return
     */
    public static Set<Integer> getAccessibleSlotsForInventoryAndSides(IInventory inventory, boolean[] accessibleSides){
        Set<Integer> slots = new HashSet<Integer>();
        if(inventory instanceof ISidedInventory) {
            for(int i = 0; i < accessibleSides.length; i++) {
                if(accessibleSides[i]) {
                    int[] accessibleSlots = ((ISidedInventory)inventory).getAccessibleSlotsFromSide(i);
                    for(int accessibleSlot : accessibleSlots) {
                        slots.add(accessibleSlot);
                    }
                }
            }
        } else {
            for(boolean bool : accessibleSides) {
                if(bool) {
                    for(int i = 0; i < inventory.getSizeInventory(); i++) {
                        slots.add(i);
                    }
                    break;
                }
            }
        }
        return slots;
    }

    public static double distBetween(double x1, double y1, double z1, double x2, double y2, double z2){
        return Math.sqrt(distBetweenSq(x1, y1, z1, x2, y2, z2));
    }

    public static double distBetweenSq(double x1, double y1, double z1, double x2, double y2, double z2){
        return Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2) + Math.pow(z1 - z2, 2);
    }

    public static double distBetween(double x1, double y1, double x2, double y2){
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    public static double distBetween(ChunkPosition pos, double x, double y, double z){
        return distBetween(pos.chunkPosX + 0.5, pos.chunkPosY + 0.5, pos.chunkPosZ + 0.5, x, y, z);
    }

    public static double distBetween(ChunkPosition pos1, ChunkPosition pos2){
        return distBetween(pos1, pos2.chunkPosX + 0.5, pos2.chunkPosY + 0.5, pos2.chunkPosZ + 0.5);
    }

    public static double distBetween(Vec3 vec, double x, double y, double z){
        return distBetween(vec.xCoord, vec.yCoord, vec.zCoord, x, y, z);
    }

    public static double distBetween(Vec3 vec1, Vec3 vec2){
        return distBetween(vec1, vec2.xCoord, vec2.yCoord, vec2.zCoord);
    }

    public static boolean areStacksEqual(ItemStack stack1, ItemStack stack2, boolean checkMeta, boolean checkNBT, boolean checkOreDict, boolean checkModSimilarity){
        if(stack1 == null && stack2 == null) return true;
        if(stack1 == null && stack2 != null || stack1 != null && stack2 == null) return false;

        if(checkModSimilarity) {
            UniqueIdentifier id1 = GameRegistry.findUniqueIdentifierFor(stack1.getItem());
            if(id1 == null || id1.modId == null) return false;
            String modId1 = id1.modId;
            UniqueIdentifier id2 = GameRegistry.findUniqueIdentifierFor(stack2.getItem());
            if(id2 == null || id2.modId == null) return false;
            String modId2 = id2.modId;
            return modId1.equals(modId2);
        }
        if(checkOreDict) {
            return isSameOreDictStack(stack1, stack2);
        }

        if(stack1.getItem() != stack2.getItem()) return false;

        boolean metaSame = stack1.getItemDamage() == stack2.getItemDamage();
        boolean nbtSame = stack1.hasTagCompound() ? stack1.getTagCompound().equals(stack2.getTagCompound()) : !stack2.hasTagCompound();

        return (!checkMeta || metaSame) && (!checkNBT || nbtSame);
    }

    public static boolean isSameOreDictStack(ItemStack stack1, ItemStack stack2){
        int[] oredictIds = OreDictionary.getOreIDs(stack1);
        for(int oredictId : oredictIds) {
            List<ItemStack> oreDictStacks = OreDictionary.getOres(OreDictionary.getOreName(oredictId));
            for(ItemStack oreDictStack : oreDictStacks) {
                if(OreDictionary.itemMatches(oreDictStack, stack2, false)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isBlockLiquid(Block block){
        return block instanceof BlockLiquid || block instanceof IFluidBlock;
    }

    public static String getOrientationName(ForgeDirection dir){
        switch(dir){
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

    public static void dropItemOnGround(ItemStack stack, World world, double x, double y, double z){
        float dX = rand.nextFloat() * 0.8F + 0.1F;
        float dY = rand.nextFloat() * 0.8F + 0.1F;
        float dZ = rand.nextFloat() * 0.8F + 0.1F;

        EntityItem entityItem = new EntityItem(world, x + dX, y + dY, z + dZ, new ItemStack(stack.getItem(), stack.stackSize, stack.getItemDamage()));

        if(stack.hasTagCompound()) {
            entityItem.getEntityItem().setTagCompound((NBTTagCompound)stack.getTagCompound().copy());
        }

        float factor = 0.05F;
        entityItem.motionX = rand.nextGaussian() * factor;
        entityItem.motionY = rand.nextGaussian() * factor + 0.2F;
        entityItem.motionZ = rand.nextGaussian() * factor;
        world.spawnEntityInWorld(entityItem);
        stack.stackSize = 0;
    }

    public static World getWorldForDimension(int dimension){
        for(WorldServer w : MinecraftServer.getServer().worldServers) {
            if(w.provider.dimensionId == dimension) {
                return w;
            }
        }
        return null;
    }

    public static TileEntity getTileEntity(ChunkPosition pos, int dimension){
        World world = getWorldForDimension(dimension);
        if(world != null && world.blockExists(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ)) {
            return world.getTileEntity(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
        }
        return null;
    }

    public static EntityPlayer getPlayerFromId(String uuid){
        for(EntityPlayer checkingPlayer : (List<EntityPlayer>)MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
            if(checkingPlayer.getGameProfile().getId().toString().equals(uuid)) {
                return checkingPlayer;
            }
        }
        return null;
    }

    public static EntityPlayer getPlayerFromName(String name){
        for(EntityPlayer checkingPlayer : (List<EntityPlayer>)MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
            if(checkingPlayer.getGameProfile().getName().equals(name)) {
                return checkingPlayer;
            }
        }
        return null;
    }

    public static boolean isPlayerOp(EntityPlayer player){
        return player.canCommandSenderUseCommand(2, "PneumaticCraftIsPlayerOp");
    }

    private static MovingObjectPosition raytraceEntityBlocks(EntityLivingBase entity, double range){
        Pair<Vec3, Vec3> startAndEnd = getStartAndEndLookVec(entity, (float)range);
        return entity.worldObj.func_147447_a(startAndEnd.getLeft(), startAndEnd.getRight(), false, false, true);
    }

    public static MovingObjectPosition getMouseOverServer(EntityLivingBase lookingEntity, double range){

        MovingObjectPosition mop = raytraceEntityBlocks(lookingEntity, range);
        double d1 = range;
        Pair<Vec3, Vec3> startAndEnd = getStartAndEndLookVec(lookingEntity, (float)range);
        Vec3 vec3 = startAndEnd.getLeft();

        if(mop != null) {
            d1 = mop.hitVec.distanceTo(vec3);
        }

        Vec3 vec31 = lookingEntity.getLookVec();
        Vec3 vec32 = startAndEnd.getRight();
        Entity pointedEntity = null;
        Vec3 vec33 = null;
        float f1 = 1.0F;
        List list = lookingEntity.worldObj.getEntitiesWithinAABBExcludingEntity(lookingEntity, lookingEntity.boundingBox.addCoord(vec31.xCoord * range, vec31.yCoord * range, vec31.zCoord * range).expand(f1, f1, f1));
        double d2 = d1;

        for(int i = 0; i < list.size(); ++i) {
            Entity entity = (Entity)list.get(i);

            if(entity.canBeCollidedWith()) {
                float f2 = entity.getCollisionBorderSize();
                AxisAlignedBB axisalignedbb = entity.boundingBox.expand(f2, f2, f2);
                MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);

                if(axisalignedbb.isVecInside(vec3)) {
                    if(0.0D < d2 || d2 == 0.0D) {
                        pointedEntity = entity;
                        vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                        d2 = 0.0D;
                    }
                } else if(movingobjectposition != null) {
                    double d3 = vec3.distanceTo(movingobjectposition.hitVec);

                    if(d3 < d2 || d2 == 0.0D) {
                        if(entity == entity.ridingEntity && !entity.canRiderInteract()) {
                            if(d2 == 0.0D) {
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

        if(pointedEntity != null && (d2 < d1 || mop == null)) {
            mop = new MovingObjectPosition(pointedEntity, vec33);
        }
        return mop;
    }

}
