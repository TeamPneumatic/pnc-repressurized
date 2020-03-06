package me.desht.pneumaticcraft.common.thirdparty.enderio;

import me.desht.pneumaticcraft.api.fuel.IFuelRegistry;
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
        registerFuel(ModFluids.DIESEL.get());
        registerFuel(ModFluids.KEROSENE.get());
        registerFuel(ModFluids.GASOLINE.get());
        registerFuel(ModFluids.LPG.get());
    }

    private void registerFuel(Fluid fluid) {
        IFuelRegistry api = PneumaticCraftAPIHandler.getInstance().getFuelRegistry();
        float burnTime = api.getBurnRateMultiplier(fluid) * 60;
        registerFuel(fluid, (int)burnTime, api.getFuelValue(fluid) / (int)burnTime);
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
