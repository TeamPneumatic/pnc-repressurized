package me.desht.pneumaticcraft.common.thirdparty.enderio;

import me.desht.pneumaticcraft.common.PneumaticCraftAPIHandler;
import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.InterModComms;

public class EnderIO implements IThirdParty {

    @Override
    public void init() {
        registerFuel(ModFluids.DIESEL_SOURCE);
        registerFuel(ModFluids.KEROSENE_SOURCE);
        registerFuel(ModFluids.GASOLINE_SOURCE);
        registerFuel(ModFluids.LPG_SOURCE);
    }

    private void registerFuel(Fluid fluid) {
        registerFuel(fluid, 60, PneumaticCraftAPIHandler.getInstance().liquidFuels.get(fluid.getRegistryName()) / 60);
    }

    private void registerFuel(Fluid fluid, int powerPerCycle, int burnTime) {
        CompoundNBT tag = new CompoundNBT();
        tag.putString("fluidName", fluid.getRegistryName().toString());
        tag.putInt("powerPerCycle", powerPerCycle);
        tag.putInt("totalBurnTime", burnTime);
        // TODO 1.14 is this right?
        InterModComms.sendTo(ModIds.EIO, "fluidFuel:add", () -> tag);
    }

    @Override
    public void postInit() {
        IThirdParty.registerFuel("enderio:hootch", 60 * 6000);
        IThirdParty.registerFuel("enderio:rocket_fuel", 160 * 7000);
        IThirdParty.registerFuel("enderio:fire_water",80 * 15000);
    }
}
