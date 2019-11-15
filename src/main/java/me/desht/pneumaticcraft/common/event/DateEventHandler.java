package me.desht.pneumaticcraft.common.event;

import java.util.Calendar;
import java.util.Random;

public class DateEventHandler {
    private static Random rand = new Random();
    private static boolean initialized;
    private static boolean isIronManEvent;

    public static boolean isEvent() {
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(2) + 1 == 4 && calendar.get(5) == 17) {//MineMaarten's birthday
            return true;
        } else if (calendar.get(2) + 1 == 12 && calendar.get(5) == 31) {//New Years eve
            return true;
        } else if (calendar.get(2) + 1 == 6 && calendar.get(5) == 9) {//PneumaticCraft's birthday
            return true;
        } else if (calendar.get(2) + 1 == 2 && calendar.get(5) == 19) {//MineMaarten released his first mod
            return true;
        }
        return false;
    }

    public static boolean isIronManEvent() {
        if (!initialized) {
            Calendar calendar = Calendar.getInstance();
            if (calendar.get(2) + 1 == 4 && calendar.get(5) == 14) {//Iron Man (1) premiere
                isIronManEvent = true;
            } else if (calendar.get(2) + 1 == 4 && calendar.get(5) == 26) {//Iron Man 2 premiere
                isIronManEvent = true;
            } else if (calendar.get(2) + 1 == 4 && calendar.get(5) == 18) {//Iron Man 3 premiere
                isIronManEvent = true;
            } else if (calendar.get(2) + 1 == 4 && calendar.get(5) == 11) {//Avengers premiere
                isIronManEvent = true;
            }
            initialized = true;
        }
        return isIronManEvent;
    }

//    public static void spawnFirework(World world, double x, double y, double z) {
//        ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET);
//
//        ItemStack itemstack1 = getFireworkCharge();
//
//        CompoundNBT nbttagcompound = new CompoundNBT();
//        CompoundNBT nbttagcompound1 = new CompoundNBT();
//        ListNBT nbttaglist = new ListNBT();
//
//        if (itemstack1 != null && itemstack1.getItem() == Items.FIRE_CHARGE && itemstack1.hasTag() && itemstack1.getTag().contains("Explosion")) {
//            nbttaglist.add(nbttaglist.size(), itemstack1.getTag().getCompound("Explosion"));
//        }
//
//        nbttagcompound1.put("Explosions", nbttaglist);
//        nbttagcompound1.putByte("Flight", (byte) 2);
//        nbttagcompound.put("Fireworks", nbttagcompound1);
//
//        rocket.setTag(nbttagcompound);
//
//        FireworkRocketEntity entity = new FireworkRocketEntity(world, x, y, z, rocket);
//        world.addEntity(entity);
//    }
//
//    private static ItemStack getFireworkCharge() {
//        ItemStack charge = new ItemStack(Items.FIRE_CHARGE);
//        CompoundNBT nbttagcompound = new CompoundNBT();
//        CompoundNBT nbttagcompound1 = new CompoundNBT();
//        ArrayList<DyeColor> arraylist = new ArrayList<>();
//
//        arraylist.add(DyeColor.byId(rand.nextInt(16)));
//
//        if (rand.nextBoolean()) nbttagcompound1.putBoolean("Flicker", true);
//        if (rand.nextBoolean()) nbttagcompound1.putBoolean("Trail", true);
//
//        byte b0 = (byte) rand.nextInt(5);
//
//        DyeColor[] aint = new DyeColor[arraylist.size()];
//
//        for (int j2 = 0; j2 < aint.length; ++j2) {
//            aint[j2] = arraylist.get(j2);
//        }
//
//        nbttagcompound1.setIntArray("Colors", aint);
//        nbttagcompound1.setByte("Type", b0);
//        nbttagcompound.setTag("Explosion", nbttagcompound1);
//        charge.setTagCompound(nbttagcompound);
//        return charge;
//    }
}
