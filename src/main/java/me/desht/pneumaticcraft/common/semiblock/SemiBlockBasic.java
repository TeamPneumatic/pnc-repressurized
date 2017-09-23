package me.desht.pneumaticcraft.common.semiblock;

import me.desht.pneumaticcraft.common.inventory.SyncedField;
import me.desht.pneumaticcraft.common.network.*;
import me.desht.pneumaticcraft.common.tileentity.IGUIButtonSensitive;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class SemiBlockBasic implements ISemiBlock, IDescSynced, IGUIButtonSensitive {
    protected World world;
    protected BlockPos pos;
    private boolean isInvalid;
    private TileEntity cachedTE;
    private List<SyncedField> descriptionFields;
    private boolean descriptionPacketScheduled;

    @Override
    public void initialize(World world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {

    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {

    }

    @Override
    public void update() {
        if (!world.isRemote && !canStay()) drop();
        if (!world.isRemote) {
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
        SemiBlockManager.getInstance(world).breakSemiBlock(world, pos);
    }

    protected boolean isAirBlock() {
        return world.isAirBlock(pos);
    }

    public IBlockState getBlockState() {
        return world.getBlockState(pos);
    }

    public TileEntity getTileEntity() {
        if (cachedTE == null || cachedTE.isInvalid()) {
            cachedTE = world.getTileEntity(pos);
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
    public boolean canPlace() {
        return true;
    }

    @Override
    public void onPlaced(EntityPlayer player, ItemStack stack) {

    }

    public boolean canStay() {
        return canPlace();
    }

    @Override
    public boolean onRightClickWithConfigurator(EntityPlayer player) {
        return false;
    }

    public void addWailaTooltip(List<String> curInfo, NBTTagCompound tag) {
        curInfo.add(TextFormatting.YELLOW + "[" + I18n.format(SemiBlockManager.getItemForSemiBlock(this).getUnlocalizedName() + ".name") + "]");
    }

    public void addWailaInfoToTag(NBTTagCompound tag) {

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
    public void writeToPacket(NBTTagCompound tag) {

    }

    @Override
    public void readFromPacket(NBTTagCompound tag) {

    }

    @Override
    public void onDescUpdate() {

    }

    @Override
    public void handleGUIButtonPress(int guiID, EntityPlayer player) {

    }
}
