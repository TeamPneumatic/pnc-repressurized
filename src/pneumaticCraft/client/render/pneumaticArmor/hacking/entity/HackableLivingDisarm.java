package pneumaticCraft.client.render.pneumaticArmor.hacking.entity;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import pneumaticCraft.api.client.pneumaticHelmet.IHackableEntity;
import pneumaticCraft.lib.Log;
import cpw.mods.fml.relauncher.ReflectionHelper;

public class HackableLivingDisarm implements IHackableEntity{
    private static Field fieldDropChance;

    @Override
    public String getId(){
        return null;
    }

    @Override
    public boolean canHack(Entity entity, EntityPlayer player){
        for(ItemStack stack : ((EntityLiving)entity).getLastActiveItems()) {
            if(stack != null) return true;
        }
        return false;
    }

    @Override
    public void addInfo(Entity entity, List<String> curInfo, EntityPlayer player){
        curInfo.add("pneumaticHelmet.hacking.result.disarm");
    }

    @Override
    public void addPostHackInfo(Entity entity, List<String> curInfo, EntityPlayer player){
        curInfo.add("pneumaticHelmet.hacking.finished.disarmed");
    }

    @Override
    public int getHackTime(Entity entity, EntityPlayer player){
        return 60;
    }

    @Override
    public void onHackFinished(Entity entity, EntityPlayer player){
        if(!entity.worldObj.isRemote) {
            Random rand = new Random();

            if(fieldDropChance == null) {
                fieldDropChance = ReflectionHelper.findField(EntityLiving.class, "field_82174_bp", "equipmentDropChances");
            }
            try {
                float[] equipmentDropChances = (float[])fieldDropChance.get(entity);
                for(int i = 0; i < ((EntityLiving)entity).getLastActiveItems().length; i++) {
                    ItemStack stack = ((EntityLiving)entity).getLastActiveItems()[i];
                    float equipmentDropChance = equipmentDropChances[i];

                    boolean flag1 = equipmentDropChance > 1.0F;

                    if(stack != null && rand.nextFloat() < equipmentDropChance) {
                        if(!flag1 && stack.isItemStackDamageable()) {
                            int k = Math.max(stack.getMaxDamage() - 25, 1);
                            int l = stack.getMaxDamage() - rand.nextInt(rand.nextInt(k) + 1);

                            if(l > k) {
                                l = k;
                            }

                            if(l < 1) {
                                l = 1;
                            }

                            stack.setItemDamage(l);
                        }

                        entity.entityDropItem(stack, 0.0F);
                    }
                    ((EntityLiving)entity).setCurrentItemOrArmor(i, null);
                }
                ((EntityLiving)entity).setCanPickUpLoot(false);
            } catch(Exception e) {
                Log.error("Reflection failed on HackableLivingDisarm");
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean afterHackTick(Entity entity){
        return false;
    }

}
