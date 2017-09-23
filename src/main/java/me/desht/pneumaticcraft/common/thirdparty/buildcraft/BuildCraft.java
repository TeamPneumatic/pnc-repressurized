package me.desht.pneumaticcraft.common.thirdparty.buildcraft;

import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;

public class BuildCraft implements IThirdParty {

    @Override
    public void preInit() {
    }

    @Override
    public void init() {
//        ItemStack stoneGear = PneumaticCraftUtils.getBuildcraftItemStack(EnumBuildcraftModule.CORE, "stoneGearItem");
//
//        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Itemss.compressedIronGear), " i ", "isi", " i ", 'i', Names.INGOT_IRON_COMPRESSED, 's', stoneGear));

        //PneumaticRegistry.getInstance().registerFuel(FluidRegistry.getFluid("fuel"), 1500000);
    }

    @Override
    public void postInit() {
    }

    @Override
    public void clientSide() {
    }

    @Override
    public void clientInit() {
    }

}
