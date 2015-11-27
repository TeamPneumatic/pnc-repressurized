package pneumaticCraft.common.tileentity;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayerFactory;
import pneumaticCraft.common.ai.StringFilterEntitySelector;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.minigun.Minigun;
import pneumaticCraft.common.network.DescSynced;
import pneumaticCraft.common.network.GuiSynced;
import pneumaticCraft.common.util.PneumaticCraftUtils;

import com.mojang.authlib.GameProfile;

public class TileEntitySentryTurret extends TileEntityBase implements IRedstoneControlled, ISidedInventory,
        IGUITextFieldSensitive{

    private final ItemStack[] inventory = new ItemStack[8];
    @GuiSynced
    private String entityFilter = "";
    @GuiSynced
    private int redstoneMode;
    @DescSynced
    private int range;
    @DescSynced
    private boolean activated;
    @DescSynced
    private ItemStack minigunColorStack;
    private Minigun minigun;
    @DescSynced
    private int targetEntityId = -1;
    @DescSynced
    private boolean sweeping;
    private final SentryTurretEntitySelector entitySelector = new SentryTurretEntitySelector();

    public TileEntitySentryTurret(){
        setUpgradeSlots(0, 1, 2, 3);
    }

    @Override
    public void updateEntity(){
        super.updateEntity();
        if(!worldObj.isRemote) {
            range = 16 + Math.min(16, getUpgrades(ItemMachineUpgrade.UPGRADE_RANGE));
            if(getMinigun().getAttackTarget() == null && redstoneAllows()) {
                getMinigun().setSweeping(true);
                if(worldObj.getTotalWorldTime() % 20 == 0) {
                    List<EntityLivingBase> entities = worldObj.selectEntitiesWithinAABB(EntityLivingBase.class, getTargetingBoundingBox(), entitySelector);
                    if(entities.size() > 0) {
                        Collections.sort(entities, new TargetSorter());
                        getMinigun().setAttackTarget(entities.get(0));
                        targetEntityId = entities.get(0).getEntityId();
                    }
                }
            } else {
                getMinigun().setSweeping(false);
            }
            EntityLivingBase target = getMinigun().getAttackTarget();
            if(target != null) {
                if(!redstoneAllows() || !entitySelector.isEntityApplicable(target)) {
                    getMinigun().setAttackTarget(null);
                    targetEntityId = -1;
                } else {
                    if(worldObj.getTotalWorldTime() % 5 == 0) {
                        getFakePlayer().setPosition(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5); //Make sure the knockback has the right direction.
                        boolean usedAmmo = getMinigun().tryFireMinigun(target);
                        if(usedAmmo) {
                            for(int i = 4; i < inventory.length; i++) {
                                if(inventory[i] != null) {
                                    setInventorySlotContents(i, null);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        getMinigun().update(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5);
    }

    private boolean canSeeEntity(Entity entity){
        Vec3 entityVec = Vec3.createVectorHelper(entity.posX + entity.width / 2, entity.posY + entity.height / 2, entity.posZ + entity.width / 2);
        Vec3 tileVec = Vec3.createVectorHelper(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5);
        MovingObjectPosition trace = worldObj.rayTraceBlocks(entityVec, tileVec);
        return trace != null && trace.blockX == xCoord && trace.blockY == yCoord && trace.blockZ == zCoord;
    }

    private AxisAlignedBB getTargetingBoundingBox(){
        return AxisAlignedBB.getBoundingBox(xCoord - range, yCoord - range, zCoord - range, xCoord + range + 1, yCoord + range + 1, zCoord + range + 1);
    }

    @Override
    protected void onFirstServerUpdate(){
        super.onFirstServerUpdate();
        updateAmmo();
    }

    @Override
    public void onDescUpdate(){
        super.onDescUpdate();
        Entity entity = worldObj.getEntityByID(targetEntityId);
        if(entity instanceof EntityLivingBase) {
            getMinigun().setAttackTarget((EntityLivingBase)entity);
        } else {
            getMinigun().setAttackTarget(null);
        }
    }

    public Minigun getMinigun(){
        if(minigun == null) {
            minigun = new MinigunSentryTurret();
            minigun.setWorld(worldObj);
            if(worldObj != null && !worldObj.isRemote) {
                minigun.setPlayer(getFakePlayer());
            }
        }
        return minigun;
    }

    private EntityPlayer getFakePlayer(){
        return FakePlayerFactory.get((WorldServer)worldObj, new GameProfile(null, "Sentry Turret"));
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        writeInventoryToNBT(tag, inventory);
        tag.setByte("redstoneMode", (byte)redstoneMode);
        tag.setString("entityFilter", entityFilter);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        readInventoryFromNBT(tag, inventory);
        redstoneMode = tag.getByte("redstoneMode");
        setText(0, tag.getString("entityFilter"));
    }

    @Override
    public boolean redstoneAllows(){
        if(redstoneMode == 3) return true;
        return super.redstoneAllows();
    }

    @Override
    public int getRedstoneMode(){
        return redstoneMode;
    }

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player){
        if(buttonID == 0) {
            redstoneMode++;
            if(redstoneMode > 2) redstoneMode = 0;
        }
    }

    /*
     * ---------------IInventory---------------------
     */

    /**
     * Returns the name of the inventory.
     */
    @Override
    public String getInventoryName(){
        return Blockss.sentryTurret.getUnlocalizedName();
    }

    /**
     * Returns the number of slots in the inventory.
     */
    @Override
    public int getSizeInventory(){
        return inventory.length;
    }

    /**
     * Returns the stack in slot i
     */
    @Override
    public ItemStack getStackInSlot(int par1){
        return inventory[par1];
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount){
        ItemStack itemStack = getStackInSlot(slot);
        if(itemStack != null) {
            if(itemStack.stackSize <= amount) {
                setInventorySlotContents(slot, null);
            } else {
                itemStack = itemStack.splitStack(amount);
                if(itemStack.stackSize == 0) {
                    setInventorySlotContents(slot, null);
                }
            }
        }
        return itemStack;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot){
        ItemStack itemStack = getStackInSlot(slot);
        if(itemStack != null) {
            setInventorySlotContents(slot, null);
        }
        return itemStack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack itemStack){
        inventory[slot] = itemStack;
        if(itemStack != null && itemStack.stackSize > getInventoryStackLimit()) {
            itemStack.stackSize = getInventoryStackLimit();
        }
        if(slot >= 4) {
            updateAmmo();
        }
    }

    private void updateAmmo(){
        ItemStack ammo = null;
        for(int i = 4; i < inventory.length; i++) {
            if(inventory[i] != null) {
                ammo = inventory[i];
                break;
            }
        }
        getMinigun().setAmmo(ammo);
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack){
        if(slot < 4) {
            return stack != null && stack.getItem() == Itemss.machineUpgrade;
        } else {
            return stack != null && stack.getItem() == Itemss.gunAmmo;
        }
    }

    @Override
    public boolean hasCustomInventoryName(){
        return false;
    }

    @Override
    public int getInventoryStackLimit(){
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer p_70300_1_){
        return isGuiUseableByPlayer(p_70300_1_);
    }

    @Override
    public void openInventory(){}

    @Override
    public void closeInventory(){}

    @Override
    public int[] getAccessibleSlotsFromSide(int p_94128_1_){
        return new int[]{0, 1, 2, 3, 4, 5, 6, 7};
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int side){
        return isItemValidForSlot(slot, stack);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side){
        return true;
    }

    private class MinigunSentryTurret extends Minigun{

        public MinigunSentryTurret(){
            super(true);
        }

        @Override
        public boolean isMinigunActivated(){
            return activated;
        }

        @Override
        public void setMinigunActivated(boolean activated){
            TileEntitySentryTurret.this.activated = activated;
        }

        @Override
        public void setAmmoColorStack(ItemStack ammo){
            minigunColorStack = ammo;
        }

        @Override
        public int getAmmoColor(){
            return getAmmoColor(minigunColorStack);
        }

        @Override
        public void playSound(String soundName, float volume, float pitch){
            worldObj.playSoundEffect(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, soundName, volume, pitch);
        }

        @Override
        public void setSweeping(boolean sweeping){
            TileEntitySentryTurret.this.sweeping = sweeping;
        }

        @Override
        public boolean isSweeping(){
            return sweeping;
        }
    }

    private class TargetSorter implements Comparator<Entity>{

        private final ChunkPosition pos;

        public TargetSorter(){
            pos = new ChunkPosition(xCoord, yCoord, zCoord);
        }

        @Override
        public int compare(Entity arg0, Entity arg1){
            double dist1 = PneumaticCraftUtils.distBetween(pos, arg0.posX, arg0.posY, arg0.posZ);
            double dist2 = PneumaticCraftUtils.distBetween(pos, arg1.posX, arg1.posY, arg1.posZ);
            return Double.compare(dist1, dist2);
        }
    }

    private class SentryTurretEntitySelector extends StringFilterEntitySelector{

        @Override
        public boolean isEntityApplicable(Entity entity){
            if(entity instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer)entity;
                if(player.capabilities.isCreativeMode || isExcludedBySecurityStations(player)) return false;
            }
            return super.isEntityApplicable(entity) && inRange(entity) && canSeeEntity(entity);
        }

        private boolean inRange(Entity entity){
            return PneumaticCraftUtils.distBetween(new ChunkPosition(xCoord, yCoord, zCoord), entity.posX, entity.posY, entity.posZ) <= range;
        }

        private boolean isExcludedBySecurityStations(EntityPlayer player){
            Iterator<TileEntitySecurityStation> iterator = PneumaticCraftUtils.getSecurityStations(worldObj, xCoord, yCoord, zCoord, false).iterator();
            if(iterator.hasNext()) { //When there are Security Stations, all stations need to be allowing the player.
                while(iterator.hasNext()) {
                    if(!iterator.next().doesAllowPlayer(player)) return false;
                }
                return true;
            } else {
                return false; //When there are no Security Stations at all, the player isn't automatically 'allowed to live'.
            }
        }
    }

    @Override
    public void setText(int textFieldID, String text){
        entityFilter = text;
        entitySelector.setFilter(text);
        if(minigun != null) minigun.setAttackTarget(null);
    }

    @Override
    public String getText(int textFieldID){
        return entityFilter;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox(){
        return getTargetingBoundingBox();
    }
}
