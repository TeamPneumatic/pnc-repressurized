package pneumaticCraft.common.semiblock;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import pneumaticCraft.common.network.PacketDescription;

public interface ISemiBlock{

    public World getWorld();

    public ChunkPosition getPos();

    public void writeToNBT(NBTTagCompound tag);

    public void readFromNBT(NBTTagCompound tag);

    public void update();

    public void initialize(World world, ChunkPosition pos);

    public void invalidate();

    public boolean isInvalid();

    public void addDrops(List<ItemStack> drops);

    public boolean canPlace();

    public void onPlaced(EntityPlayer player, ItemStack stack);

    public boolean onRightClickWithConfigurator(EntityPlayer player);

    public PacketDescription getDescriptionPacket();
}
