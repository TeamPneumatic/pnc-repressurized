package pneumaticCraft.common;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraft.world.ChunkCache;
import net.minecraftforge.common.AchievementPage;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.fluid.Fluids;
import pneumaticCraft.common.item.Itemss;

public class AchievementHandler{
    public static final Map<String, Achievement> achieveList = new HashMap<String, Achievement>();

    public static void init(){
        registerAcquire(0, 0, Itemss.ingotIronCompressed, null);
        registerAcquire(2, 0, Blockss.airCompressor, getAchieve(Itemss.ingotIronCompressed));
        registerAcquire(4, 0, Fluids.getBucket(Fluids.oil), getAchieve(Itemss.ingotIronCompressed));
        registerAcquire(6, 0, Blockss.refinery, getAchieve(Fluids.getBucket(Fluids.oil)));
        registerAcquire(8, 0, Itemss.plastic, getAchieve(Blockss.refinery));
        registerAcquire(10, 0, Blockss.uvLightBox, getAchieve(Itemss.plastic));
        registerAcquire(12, 0, Fluids.getBucket(Fluids.etchingAcid), getAchieve(Blockss.uvLightBox));

        register("dw9x9", 0, 2, new ItemStack(Blocks.cobblestone), null).setSpecial();

        AchievementPage.registerAchievementPage(new AchievementPage("PneumaticCraft", achieveList.values().toArray(new Achievement[achieveList.size()])));
    }

    private static void registerAcquire(int x, int y, Item item, Achievement parentAchievement){
        registerAcquire(x, y, new ItemStack(item), parentAchievement);
    }

    private static void registerAcquire(int x, int y, Block block, Achievement parentAchievement){
        registerAcquire(x, y, new ItemStack(block), parentAchievement);
    }

    private static void registerAcquire(int x, int y, ItemStack stack, Achievement parentAchievement){
        register(stack.getItem().getUnlocalizedName().substring(5), x, y, stack, parentAchievement);
    }

    private static Achievement register(String id, int x, int y, ItemStack icon, Achievement parentAchievement){
        Achievement achieve = new Achievement(id, id, x, y, icon, parentAchievement);
        achieve.initIndependentStat();
        achieve.registerStat();
        achieveList.put(id, achieve);
        return achieve;
    }

    public static void giveAchievement(EntityPlayer player, ItemStack acquiredStack){
        try {
            if(FluidContainerRegistry.containsFluid(acquiredStack, new FluidStack(Fluids.oil, 1))) {
                giveAchievement(player, Fluids.getBucket(Fluids.oil).getUnlocalizedName().substring(5));
            }
            giveAchievement(player, acquiredStack.getItem().getUnlocalizedName().substring(5));
        } catch(Throwable e) {}
    }

    public static void giveAchievement(EntityPlayer player, String id){
        Achievement achieve = getAchieve(id);
        if(achieve != null) player.triggerAchievement(achieve);
    }

    private static Achievement getAchieve(Item item){
        return getAchieve(new ItemStack(item));
    }

    private static Achievement getAchieve(Block block){
        return getAchieve(new ItemStack(block));
    }

    private static Achievement getAchieve(ItemStack stack){
        return getAchieve(stack.getItem().getUnlocalizedName().substring(5));
    }

    private static Achievement getAchieve(String id){
        return achieveList.get(id);
    }

    public static void checkFor9x9(EntityPlayer player, int x, int y, int z){
        ChunkCache cache = new ChunkCache(player.worldObj, x - 8, y, z - 8, x + 8, y, z + 8, 0);
        ForgeDirection[] dirs = {ForgeDirection.NORTH, ForgeDirection.WEST};
        for(ForgeDirection dir : dirs) {
            int wallLength = 1;
            int minX = x;
            int minZ = z;
            int maxX = x;
            int maxZ = z;
            int newX = x + dir.offsetX;
            int newZ = z + dir.offsetZ;
            while(wallLength < 9 && cache.getBlock(newX, y, newZ) == Blocks.cobblestone) {
                wallLength++;
                minX = Math.min(minX, newX);
                minZ = Math.min(minZ, newZ);
                maxX = Math.max(maxX, newX);
                maxZ = Math.max(maxZ, newZ);
                newX += dir.offsetX;
                newZ += dir.offsetZ;
            }
            newX = x - dir.offsetX;
            newZ = z - dir.offsetZ;
            while(wallLength < 9 && cache.getBlock(newX, y, newZ) == Blocks.cobblestone) {
                wallLength++;
                minX = Math.min(minX, newX);
                minZ = Math.min(minZ, newZ);
                maxX = Math.max(maxX, newX);
                maxZ = Math.max(maxZ, newZ);
                newX -= dir.offsetX;
                newZ -= dir.offsetZ;
            }
            if(wallLength == 9) {
                if(checkFor9x9(cache, x, y, z, minX, minZ, maxX, maxZ)) {
                    giveAchievement(player, "dw9x9");
                    return;
                }
            }
        }
    }

    private static boolean checkFor9x9(ChunkCache cache, int x, int y, int z, int minX, int minZ, int maxX, int maxZ){
        if(minX == maxX) {
            for(int offset = 0; offset < 2; offset++) {
                boolean isValid = true;
                for(int i = 0; i < 9; i++) {
                    if(cache.getBlock(x - 8 + offset * 16, y, minZ + i) != Blocks.cobblestone) {
                        isValid = false;
                        break;
                    }
                    if(cache.getBlock(x - 8 + offset * 8 + i, y, minZ) != Blocks.cobblestone) {
                        isValid = false;
                        break;
                    }
                    if(cache.getBlock(x - 8 + offset * 8 + i, y, maxZ) != Blocks.cobblestone) {
                        isValid = false;
                        break;
                    }
                }
                if(isValid) return true;
            }
        } else {
            for(int offset = 0; offset < 2; offset++) {
                boolean isValid = true;
                for(int i = 0; i < 9; i++) {
                    if(cache.getBlock(minX + i, y, z - 8 + offset * 16) != Blocks.cobblestone) {
                        isValid = false;
                        break;
                    }
                    if(cache.getBlock(minX, y, z - 8 + offset * 8 + i) != Blocks.cobblestone) {
                        isValid = false;
                        break;
                    }
                    if(cache.getBlock(maxX, y, z - 8 + offset * 8 + i) != Blocks.cobblestone) {
                        isValid = false;
                        break;
                    }
                }
                if(isValid) return true;
            }
        }
        return false;
    }
}
