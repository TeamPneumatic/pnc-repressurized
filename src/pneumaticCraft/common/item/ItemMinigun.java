package pneumaticCraft.common.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import pneumaticCraft.common.NBTUtil;
import pneumaticCraft.common.minigun.Minigun;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemMinigun extends ItemPressurizable{

    private final Minigun minigun = new MinigunItem();

    public ItemMinigun(int maxAir, int volume){
        super(null, maxAir, volume);
    }

    /**
     * called when the player releases the use item button. Args: itemstack, world, entityplayer, itemInUseCount
     */
    @Override
    public void onPlayerStoppedUsing(ItemStack p_77615_1_, World p_77615_2_, EntityPlayer p_77615_3_, int p_77615_4_){

    }

    /**
     * returns the action that specifies what animation to play when the items is being used
     */
    /* public EnumAction getItemUseAction(ItemStack p_77661_1_)
     {
         return EnumAction.bow;
     }*/

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean currentItem){
        super.onUpdate(stack, world, entity, slot, currentItem);
        EntityPlayer player = (EntityPlayer)entity;
        getMinigun(stack, player);
        if(!currentItem) {
            minigun.setMinigunSoundCounter(-1);
            minigun.setMinigunSpeed(0);
            minigun.setMinigunActivated(false);
            minigun.setMinigunTriggerTimeOut(0);
        }
        minigun.update(player.posX, player.posY, player.posZ);
        if(world.isRemote && currentItem && minigun.getMinigunSpeed() > 0) {
            suppressSwitchAnimation();
        }
    }

    @SideOnly(Side.CLIENT)
    private void suppressSwitchAnimation(){
        Minecraft mc = Minecraft.getMinecraft();
        ItemRenderer renderer = mc.entityRenderer.itemRenderer;
        renderer.updateEquippedItem();
        renderer.equippedProgress = 1;
        renderer.prevEquippedProgress = 1;

    }

    public Minigun getMinigun(ItemStack stack, EntityPlayer player){
        minigun.setItemStack(stack).setAmmo(getAmmo(player)).setPlayer(player).setPressurizable(this, 20).setWorld(player.worldObj);
        return minigun;
    }

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player){
        if(!world.isRemote) {
            ItemStack ammo = getAmmo(player);
            if(ammo != null) {
                boolean usedAmmo = getMinigun(stack, player).tryFireMinigun(null);
                if(usedAmmo) {
                    player.inventory.consumeInventoryItem(Itemss.gunAmmo);
                }
            }
        }
        return stack;
    }

    private ItemStack getAmmo(EntityPlayer player){
        for(ItemStack stack : player.inventory.mainInventory) {
            if(stack != null && stack.getItem() == Itemss.gunAmmo) return stack;
        }
        return null;
    }

    private class MinigunItem extends Minigun{

        public MinigunItem(){
            super(false);
        }

        @Override
        public boolean isMinigunActivated(){
            return NBTUtil.getBoolean(stack, "activated");
        }

        @Override
        public void setMinigunActivated(boolean activated){
            NBTUtil.setBoolean(stack, "activated", activated);
        }

        @Override
        public void setAmmoColorStack(ItemStack ammo){
            if(ammo != null) {
                NBTTagCompound tag = new NBTTagCompound();
                ammo.writeToNBT(tag);
                NBTUtil.setCompoundTag(stack, "ammoColorStack", tag);
            } else {
                NBTUtil.removeTag(stack, "ammoColorStack");
            }
        }

        @Override
        public int getAmmoColor(){
            ItemStack ammo = null;
            if(NBTUtil.hasTag(stack, "ammoColorStack")) {
                NBTTagCompound tag = NBTUtil.getCompoundTag(stack, "ammoColorStack");
                ammo = ItemStack.loadItemStackFromNBT(tag);
            }
            return getAmmoColor(ammo);
        }

        @Override
        public void playSound(String soundName, float volume, float pitch){
            world.playSoundAtEntity(player, soundName, volume, pitch);
        }

        @Override
        public double getMinigunSpeed(){
            return NBTUtil.getDouble(stack, "speed");
        }

        @Override
        public void setMinigunSpeed(double minigunSpeed){
            NBTUtil.setDouble(stack, "speed", minigunSpeed);
        }

        @Override
        public int getMinigunTriggerTimeOut(){
            return NBTUtil.getInteger(stack, "triggerTimeout");
        }

        @Override
        public void setMinigunTriggerTimeOut(int minigunTriggerTimeOut){
            NBTUtil.setInteger(stack, "triggerTimeout", minigunTriggerTimeOut);
        }

        @Override
        public int getMinigunSoundCounter(){
            return NBTUtil.getInteger(stack, "soundCounter");
        }

        @Override
        public void setMinigunSoundCounter(int minigunSoundCounter){
            NBTUtil.setInteger(stack, "soundCounter", minigunSoundCounter);
        }

        @Override
        public double getMinigunRotation(){
            return NBTUtil.getDouble(stack, "rotation");
        }

        @Override
        public void setMinigunRotation(double minigunRotation){
            NBTUtil.setDouble(stack, "rotation", minigunRotation);
        }

        @Override
        public double getOldMinigunRotation(){
            return NBTUtil.getDouble(stack, "oldRotation");
        }

        @Override
        public void setOldMinigunRotation(double oldMinigunRotation){
            NBTUtil.setDouble(stack, "oldRotation", oldMinigunRotation);
        }
    }

}
