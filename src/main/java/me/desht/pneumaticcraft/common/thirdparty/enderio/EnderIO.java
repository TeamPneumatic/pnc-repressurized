package me.desht.pneumaticcraft.common.thirdparty.enderio;

import me.desht.pneumaticcraft.common.PneumaticCraftAPIHandler;
import me.desht.pneumaticcraft.common.fluid.Fluids;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public class EnderIO implements IThirdParty {

    @Override
    public void preInit() {
        registerFuel(Fluids.DIESEL);
        registerFuel(Fluids.KEROSENE);
        registerFuel(Fluids.GASOLINE);
        registerFuel(Fluids.LPG);
    }

    private void registerFuel(Fluid fluid) {
        registerFuel(fluid, 60, PneumaticCraftAPIHandler.getInstance().liquidFuels.get(fluid.getName()) / 60);
    }

    private void registerFuel(Fluid fluid, int powerPerCycle, int burnTime) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("fluidName", fluid.getName());
        tag.setInteger("powerPerCycle", powerPerCycle);
        tag.setInteger("totalBurnTime", burnTime);
        FMLInterModComms.sendMessage(ModIds.EIO, "fluidFuel:add", tag);
    }

    @Override
    public void postInit() {
        IThirdParty.registerFuel("hootch", "EnderIO", 60 * 6000);
        IThirdParty.registerFuel("rocket_fuel", "EnderIO",160 * 7000);
        IThirdParty.registerFuel("fire_water", "EnderIO",80 * 15000);
    }
}
