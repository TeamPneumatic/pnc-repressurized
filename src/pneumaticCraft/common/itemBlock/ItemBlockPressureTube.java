package pneumaticCraft.common.itemBlock;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBlockPressureTube extends ItemBlock{

    public ItemBlockPressureTube(Block block){
        super(block);
        setHasSubtypes(true);
    }

    @Override
    public String getItemStackDisplayName(ItemStack is){
        switch(is.getItemDamage()){
            case 0:
                return super.getItemStackDisplayName(is);
            case 1:
                return "Flow Detection Tube";
            case 2:
                return "Safety Valve Tube";
            case 3:
                return "Pressure Regulator Tube";
            case 4:
                return "Air Grate Tube";
            case 5:
                return "Pressure Gauge Tube";
        }
        return "Pressure Tube";
    }

    @Override
    public int getMetadata(int meta){
        return meta;
    }

    @Override
    @SideOnly(Side.CLIENT)
    /**
     * allows items to add custom lines of information to the mouseover description
     */
    public void addInformation(ItemStack iStack, EntityPlayer player, List list, boolean par4){
        String formula = getFormula(iStack.getItemDamage());
        if(!formula.equals("")) list.add(formula);
        switch(iStack.getItemDamage()){
            case 1:
                list.add("This tube emits a redstone signal of which");
                list.add("the strength is dependant on how much air");
                list.add("is travelling through the tube.");
                break;
            case 2:
                list.add("This tube will release high pressure gases");
                list.add("when a certain threshold's reached. Though");
                list.add("it prevents overpressure it can be counted");
                list.add("as energy loss.");
                break;
            case 3:
                list.add("This tube will stop pressurized air from");
                list.add("travelling through this tube when a certain");
                list.add("threshold's reached.");
                break;
            case 4:
                list.add("This tube will attract or repel any entity");
                list.add("within range dependant on whether it is in");
                list.add("vacuum or under pressure respectively.");
                break;
            case 5:
                list.add("This tube emits a redstone signal of which");
                list.add("the strength is dependant on how much pressure");
                list.add("the tube is at.");
                break;
        }
    }

    @SideOnly(Side.CLIENT)
    protected String getFormula(int itemDamage){
        switch(itemDamage){
            case 1:
                return EnumChatFormatting.BLUE + "Formula: Redstone = 0.2 x flow(mL/tick)";
            case 2:
                return EnumChatFormatting.BLUE + "Formula: Threshold(bar) = 7.5 - Redstone x 0.5";
            case 3:
                return EnumChatFormatting.BLUE + "Formula: Threshold(bar) = 7.5 - Redstone x 0.5";
            case 4:
                return EnumChatFormatting.BLUE + "Formula: Range(blocks) = 4.0 x pressure(bar), or -16 x pressure(bar), if vacuum";
            case 5:
                return EnumChatFormatting.BLUE + "Formula: Redstone = 2.0 x pressure(bar)";
        }
        return "";
    }

}
