package me.desht.pneumaticcraft.common.semiblock;

import me.desht.pneumaticcraft.common.network.*;
import me.desht.pneumaticcraft.common.tileentity.IGUIButtonSensitive;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import java.util.List;

public abstract class SemiBlockBasic<TTileEntity extends TileEntity> implements ISemiBlock, IDescSynced, IGUIButtonSensitive {
    private final Class<TTileEntity> tileClass;
    protected World world;
    protected BlockPos pos;
    private int index = -1; //There can be multiple semi blocks in one block.
    private boolean isInvalid;
    private TTileEntity cachedTE;
    private List<SyncedField> descriptionFields;
    private boolean descriptionPacketScheduled;

    public SemiBlockBasic(Class<TTileEntity> tileClass){
        this.tileClass = tileClass;
    }
    
    @Override
    public void initialize(World world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
    }
    
    @Override
    public int getIndex(){
        if(index == -1){
            index = SemiBlockManager.getInstance(world).getSemiBlocksAsList(world, getPos()).indexOf(this);
            if(index == -1) throw new IllegalStateException("Semi block is not part of the world! " + this);
        }
        return index;
    }
    
    @Override
    public void onSemiBlockRemovedFromThisPos(ISemiBlock semiBlock){
        index = -1; //Invalidate cache, only update on removing, because added semiblocks are appended to the back, not influencing the index.
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {

    }

    @Override
    public void readFromNBT(CompoundNBT tag) {

    }

    @Override
    public void tick() {
        if (!world.isRemote && !canStay()) drop();
        if (!world.isRemote && !isInvalid()) {
            if (descriptionFields == null) descriptionPacketScheduled = true;
            for (SyncedField field : getDescriptionFields()) {
                if (field.update()) {
                    descriptionPacketScheduled = true;
                }
            }

            if (descriptionPacketScheduled) {
                descriptionPacketScheduled = false;
                sendDescriptionPacket();
            }
        }
    }

    private void sendDescriptionPacket() {
        NetworkHandler.sendToAllAround(getDescriptionPacket(), world);
    }

    @Override
    public PacketDescription getDescriptionPacket() {
        return new PacketDescription(this);
    }

    @Override
    public BlockPos getPosition() {
        return getPos();
    }

    protected void drop() {
        SemiBlockManager.getInstance(world).breakSemiBlock(this);
    }

    protected boolean isAirBlock() {
        return world.isAirBlock(pos);
    }

    public BlockState getBlockState() {
        return world.getBlockState(pos);
    }
    
    public boolean isAir(){
        BlockState state = getBlockState();
        return state.getBlock().isAir(state, world, pos);
    }

    @SuppressWarnings("unchecked")
    public TTileEntity getTileEntity() {
        if (cachedTE == null || cachedTE.isRemoved()) {
            TileEntity te = world.getTileEntity(pos);
            if(te != null && tileClass.isAssignableFrom(te.getClass())){
                cachedTE = (TTileEntity)te;
            }else{
                cachedTE = null;
            }
        }
        return cachedTE;
    }

    @Override
    public void invalidate() {
        isInvalid = true;
    }

    @Override
    public boolean isInvalid() {
        return isInvalid;
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public BlockPos getPos() {
        return pos;
    }

    @Override
    public void addDrops(NonNullList<ItemStack> drops) {
        Item item = SemiBlockManager.getItemForSemiBlock(this);
        if (item != null) drops.add(new ItemStack(item));
    }

    @Override
    public boolean canPlace(Direction facing) {
        return true;
    }
    
    @Override
    public void prePlacement(PlayerEntity player, ItemStack stack, Direction facing){
        
    }

    /**
     * Check if the player can place this semiblock here.
     * @param player the player
     * @param stack the semiblock item to be placed
     * @param facing the facing direction
     */
    @Override
    public void onPlaced(PlayerEntity player, ItemStack stack, Direction facing) {
    }

    /**
     * Check if this semiblock can remain here
     * @return true if the semiblock can stay, false if it should be dropped
     */
    public boolean canStay() {
        return canPlace(null);
    }

    @Override
    public boolean onRightClickWithConfigurator(PlayerEntity player, Direction side) {
        return false;
    }

    /**
     * Add information for the benefit of info mods such as TOP or WAILA/HWYLA.
     * Adds nothing by default; subclasses will override this. Note: the semiblock name is expected to be added by
     * the caller. NOTE: this can be called on the server too (TOP) so don't use any client-only methods (I18n.format)
     *
     * @param curInfo list to add info to
     * @param tag NBT data from the semiblock in question containing extra info
     * @param extended show extended data?
     */
    public void addTooltip(List<ITextComponent> curInfo, CompoundNBT tag, boolean extended) {
    }

    public void addWailaInfoToTag(CompoundNBT tag) {

    }

    @Override
    public Type getSyncType() {
        return Type.SEMI_BLOCK;
    }

    @Override
    public List<SyncedField> getDescriptionFields() {
        if (descriptionFields == null) {
            descriptionFields = NetworkUtils.getSyncedFields(this, DescSynced.class);
            for (SyncedField field : descriptionFields) {
                field.update();
            }
        }
        return descriptionFields;
    }
    
    

    @Override
    public void writeToPacket(CompoundNBT tag) {
        tag.putByte("index", (byte)getIndex()); //Used in packet decoding to figure out which semiblock updated.
    }

    @Override
    public void readFromPacket(CompoundNBT tag) {

    }

    @Override
    public void onDescUpdate() {
    }

    @Override
    public void handleGUIButtonPress(String tag, PlayerEntity player) {
    }
    
    @Override
    public String toString(){
        return String.format("Pos: %s, %s", getPos(), getClass());
    }
}
