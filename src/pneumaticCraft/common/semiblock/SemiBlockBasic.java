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

public class SemiBlockBasic implements ISemiBlock{
    protected World world;
    protected ChunkPosition pos;
    private boolean isInvalid;
    private TileEntity cachedTE;

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
    }

    protected void drop(){
        SemiBlockManager.getInstance().breakSemiBlock(world, pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
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
}
