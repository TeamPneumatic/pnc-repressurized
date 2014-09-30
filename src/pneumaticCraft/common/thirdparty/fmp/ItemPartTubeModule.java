package pneumaticCraft.common.thirdparty.fmp;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.common.block.Blockss;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

public class ItemPartTubeModule extends ItemPart{

    public ItemPartTubeModule(String partName){
        super(partName);
        setCreativeTab(PneumaticCraft.tabPneumaticCraft);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World w, int x, int y, int z, int side, float hitX, float hitY, float hitZ){
        if(FMP.getMultiPart(w, new ChunkPosition(x, y, z), PartPressureTube.class) != null || w.getBlock(x, y, z) == Blockss.pressureTube) {
            BlockCoord pos = new BlockCoord(x, y, z);
            Vector3 vhit = new Vector3(hitX, hitY, hitZ);
            double d = getHitDepth(vhit, side);
            if(d < 1 && place(stack, player, pos, w, side, vhit)) {
                w.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, Block.soundTypeGlass.getStepResourcePath(), Block.soundTypeGlass.getVolume() * 5.0F, Block.soundTypeGlass.getPitch() * .9F);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean place(ItemStack item, EntityPlayer player, BlockCoord pos, World world, int side, Vector3 vhit){
        TMultiPart part = newPart(item, player, world, pos, side, vhit);
        if(part == null || !TileMultipart.canPlacePart(world, pos, part)) return false;
        if(!world.isRemote) TileMultipart.addPart(world, pos, part);
        if(!player.capabilities.isCreativeMode) item.stackSize -= 1;
        return true;
    }
}
