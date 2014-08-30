package pneumaticCraft.common.thirdparty.fmp;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.block.tubes.ISidedPart;
import pneumaticCraft.common.block.tubes.ModuleRegistrator;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.JItemMultiPart;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TMultiPart;

public class ItemPart extends JItemMultiPart{
    private final String partName;

    public ItemPart(String partName){
        this.partName = partName;
        setUnlocalizedName(partName);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World w, int x, int y, int z, int side, float hitX, float hitY, float hitZ){
        if(super.onItemUse(stack, player, w, x, y, z, side, hitX, hitY, hitZ)) {
            w.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, Block.soundTypeGlass.getStepResourcePath(), Block.soundTypeGlass.getVolume() * 5.0F, Block.soundTypeGlass.getPitch() * .9F);
            return true;
        }
        return false;
    }

    @Override
    public TMultiPart newPart(ItemStack item, EntityPlayer player, World world, BlockCoord pos, int side, Vector3 vhit){
        TMultiPart part = MultiPartRegistry.createPart(partName, false);
        if(part instanceof PartTubeModule) {
            ((PartTubeModule)part).setModule(ModuleRegistrator.getModule(partName));
        }
        if(part instanceof ISidedPart) {
            ((ISidedPart)part).setDirection(ForgeDirection.getOrientation(side));
        }
        return part;
    }

    @Override
    public void registerIcons(IIconRegister reg){}
}
