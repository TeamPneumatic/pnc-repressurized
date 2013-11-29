package pneumaticExample;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
import pneumaticCraft.api.client.GuiAnimatedStatSupplier;
import pneumaticCraft.api.client.IGuiAnimatedStat;
import pneumaticCraft.api.client.pneumaticHelmet.IOptionPage;
import pneumaticCraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class RenderHandlerExample implements IUpgradeRenderHandler{
    @SideOnly(Side.CLIENT)
    private IGuiAnimatedStat stat;

    @Override
    @SideOnly(Side.CLIENT)
    public String getUpgradeName(){
        return "Example";
    }

    @Override
    public void initConfig(Configuration config){}

    @Override
    @SideOnly(Side.CLIENT)
    public void saveToConfig(){}

    @Override
    @SideOnly(Side.CLIENT)
    public void update(EntityPlayer player, int rangeUpgrades){}

    @Override
    @SideOnly(Side.CLIENT)
    public void render3D(float partialTicks){}

    @Override
    @SideOnly(Side.CLIENT)
    public void render2D(float partialTicks, boolean helmetEnabled){}

    @Override
    @SideOnly(Side.CLIENT)
    public IGuiAnimatedStat getAnimatedStat(){
        if(stat == null) {
            stat = GuiAnimatedStatSupplier.getAnimatedStat(null, new ItemStack(Item.diamond), 0xFFAAAAFF);
            stat.setBaseX(30);
            stat.setBaseY(30);
            stat.setTitle("Example");
            stat.setText("Here is some example text.");
        }
        return stat;
    }

    @Override
    public boolean isEnabled(ItemStack[] upgradeStacks){
        return true;
    }

    @Override
    public float getEnergyUsage(int rangeUpgrades, EntityPlayer player){
        return 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void reset(){
        stat = null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IOptionPage getGuiOptionsPage(){
        return new OptionPageExample();
    }

}
