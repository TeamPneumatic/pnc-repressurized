package pneumaticCraft.common;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.init.Items;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

public class DateEventHandler{
    private static Random rand = new Random();
    private static boolean initialized;
    private static boolean isIronManEvent;

    public static boolean isEvent(){
        Calendar calendar = Calendar.getInstance();
        if(calendar.get(2) + 1 == 4 && calendar.get(5) == 17) {//MineMaarten's birthday
            return true;
        } else if(calendar.get(2) + 1 == 12 && calendar.get(5) == 31) {//New Years eve
            return true;
        } else if(calendar.get(2) + 1 == 6 && calendar.get(5) == 9) {//PneumaticCraft's birthday
            return true;
        } else if(calendar.get(2) + 1 == 2 && calendar.get(5) == 19) {//MineMaarten released his first mod
            return true;
        }
        return false;
    }

    public static boolean isIronManEvent(){
        if(!initialized) {
            Calendar calendar = Calendar.getInstance();
            if(calendar.get(2) + 1 == 4 && calendar.get(5) == 14) {//Iron Man (1) premiere
                isIronManEvent = true;
            } else if(calendar.get(2) + 1 == 4 && calendar.get(5) == 26) {//Iron Man 2 premiere
                isIronManEvent = true;
            } else if(calendar.get(2) + 1 == 4 && calendar.get(5) == 18) {//Iron Man 3 premiere
                isIronManEvent = true;
            } else if(calendar.get(2) + 1 == 4 && calendar.get(5) == 11) {//Avengers premiere
                isIronManEvent = true;
            }
            initialized = true;
        }
        return isIronManEvent;
    }

    public static void spawnFirework(World world, double x, double y, double z){
        ItemStack rocket = new ItemStack(Items.fireworks);

        ItemStack itemstack1 = getFireworkCharge();

        NBTTagCompound nbttagcompound = new NBTTagCompound();
        NBTTagCompound nbttagcompound1 = new NBTTagCompound();
        NBTTagList nbttaglist = new NBTTagList();

        if(itemstack1 != null && itemstack1.getItem() == Items.firework_charge && itemstack1.hasTagCompound() && itemstack1.getTagCompound().hasKey("Explosion")) {
            nbttaglist.appendTag(itemstack1.getTagCompound().getCompoundTag("Explosion"));
        }

        nbttagcompound1.setTag("Explosions", nbttaglist);
        nbttagcompound1.setByte("Flight", (byte)2);
        nbttagcompound.setTag("Fireworks", nbttagcompound1);

        rocket.setTagCompound(nbttagcompound);

        EntityFireworkRocket entity = new EntityFireworkRocket(world, x, y, z, rocket);
        world.spawnEntityInWorld(entity);
    }

    private static ItemStack getFireworkCharge(){
        ItemStack charge = new ItemStack(Items.firework_charge);
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        NBTTagCompound nbttagcompound1 = new NBTTagCompound();
        byte b0 = 0;
        ArrayList arraylist = new ArrayList();

        arraylist.add(Integer.valueOf(ItemDye.field_150922_c[rand.nextInt(16)]));

        if(rand.nextBoolean()) nbttagcompound1.setBoolean("Flicker", true);

        if(rand.nextBoolean()) nbttagcompound1.setBoolean("Trail", true);

        b0 = (byte)rand.nextInt(5);

        int[] aint = new int[arraylist.size()];

        for(int j2 = 0; j2 < aint.length; ++j2) {
            aint[j2] = ((Integer)arraylist.get(j2)).intValue();
        }

        nbttagcompound1.setIntArray("Colors", aint);
        nbttagcompound1.setByte("Type", b0);
        nbttagcompound.setTag("Explosion", nbttagcompound1);
        charge.setTagCompound(nbttagcompound);
        return charge;
    }
}
