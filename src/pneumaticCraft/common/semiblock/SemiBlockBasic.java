package pneumaticCraft.common.semiblock;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import pneumaticCraft.common.inventory.SyncedField;
import pneumaticCraft.common.network.DescSynced;
import pneumaticCraft.common.network.IDescSynced;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.NetworkUtils;
import pneumaticCraft.common.network.PacketDescription;
import pneumaticCraft.common.tileentity.IGUIButtonSensitive;

public class SemiBlockBasic implements ISemiBlock, IDescSynced, IGUIButtonSensitive{
    protected World world;
    protected ChunkPosition pos;
    private boolean isInvalid;
    private TileEntity cachedTE;
    private List<SyncedField> descriptionFields;
    private boolean descriptionPacketScheduled;

    @Override
    public void initialize(World world, ChunkPosition pos){
        this.world = world;
        this.pos = pos;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){

    }

    @Override
    public void readFromNBT(NBTTagCompound tag){

    }

    @Override
    public void update(){
        if(!world.isRemote && !canStay()) drop();
        if(!world.isRemote) {
            if(descriptionFields == null) descriptionPacketScheduled = true;
            for(SyncedField field : getDescriptionFields()) {
                if(field.update()) {
                    descriptionPacketScheduled = true;
                }
            }

            if(descriptionPacketScheduled) {
                descriptionPacketScheduled = false;
                sendDescriptionPacket();
            }
        }
    }

    private void sendDescriptionPacket(){
        NetworkHandler.sendToAllAround(getDescriptionPacket(), world);
    }

    @Override
    public PacketDescription getDescriptionPacket(){
        return new PacketDescription(this);
    }

    protected void drop(){
        SemiBlockManager.getInstance(world).breakSemiBlock(world, pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
    }

    protected boolean isAirBlock(){
        return world.isAirBlock(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
    }

    public Block getBlock(){
        return world.getBlock(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
    }

    public TileEntity getTileEntity(){
        if(cachedTE == null || cachedTE.isInvalid()) {
            cachedTE = world.getTileEntity(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
        }
        return cachedTE;
    }

    @Override
    public void invalidate(){
        isInvalid = true;
    }

    @Override
    public boolean isInvalid(){
        return isInvalid;
    }

    @Override
    public World getWorld(){
        return world;
    }

    @Override
    public ChunkPosition getPos(){
        return pos;
    }

    @Override
    public void addDrops(List<ItemStack> drops){
        Item item = SemiBlockManager.getItemForSemiBlock(this);
        if(item != null) drops.add(new ItemStack(item));
    }

    @Override
    public boolean canPlace(){
        return true;
    }

    @Override
    public void onPlaced(EntityPlayer player, ItemStack stack){

    }

    public boolean canStay(){
        return canPlace();
    }

    @Override
    public boolean onRightClickWithConfigurator(EntityPlayer player){
        return false;
    }

    public void addWailaTooltip(List<String> curInfo, NBTTagCompound tag){
        curInfo.add(EnumChatFormatting.YELLOW + "[" + StatCollector.translateToLocal(SemiBlockManager.getItemForSemiBlock(this).getUnlocalizedName() + ".name") + "]");
    }

    public void addWailaInfoToTag(NBTTagCompound tag){

    }

    @Override
    public Type getSyncType(){
        return Type.SEMI_BLOCK;
    }

    @Override
    public List<SyncedField> getDescriptionFields(){
        if(descriptionFields == null) {
            descriptionFields = NetworkUtils.getSyncedFields(this, DescSynced.class);
            for(SyncedField field : descriptionFields) {
                field.update();
            }
        }
        return descriptionFields;
    }

    @Override
    public void writeToPacket(NBTTagCompound tag){

    }

    @Override
    public void readFromPacket(NBTTagCompound tag){

    }

    @Override
    public int getX(){
        return pos.chunkPosX;
    }

    @Override
    public int getY(){
        return pos.chunkPosY;
    }

    @Override
    public int getZ(){
        return pos.chunkPosZ;
    }

    @Override
    public void onDescUpdate(){

    }

    @Override
    public void handleGUIButtonPress(int guiID, EntityPlayer player){

    }
}
