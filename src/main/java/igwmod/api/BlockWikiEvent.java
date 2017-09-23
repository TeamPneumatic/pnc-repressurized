package igwmod.api;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.FMLClientHandler;

/**
 * This event will be fired on MinecraftForge.EVENT_BUS when a player opens the wiki GUI while looking at a block in the world. Your job as subscriber is to change the
 * pageOpened field when you find the right block. If no subscriber changes the pageOpened field, IGW will try to open a page at
 * assets/igwmod/wiki/block/drawnStack.getUnlocalizedName()>.
 */
public class BlockWikiEvent extends WorldEvent{
    public final BlockPos pos;
    public final IBlockState blockState;
    public ItemStack itemStackPicked;
    public ItemStack drawnStack; //ItemStack that is drown in the top left corner of the GUI.
    public String pageOpened; //current page this gui will go to. It contains the default location, but can be changed.

    public BlockWikiEvent(World world, BlockPos pos){
        super(world);
        this.pos = pos;
        blockState = world.getBlockState(pos);
        try {
            itemStackPicked = blockState.getBlock().getPickBlock(blockState, FMLClientHandler.instance().getClient().objectMouseOver, world, pos, FMLClientHandler.instance().getClientPlayerEntity());
        } catch(Throwable e) {}//FMP parts have the habit to throw a ClassCastException.
        drawnStack = itemStackPicked != null ? itemStackPicked : new ItemStack(blockState.getBlock(), 1, blockState.getBlock().getMetaFromState(blockState));
    }
}
