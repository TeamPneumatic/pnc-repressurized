package me.desht.pneumaticcraft.common.thirdparty.thaumcraft;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import net.minecraft.item.ItemStack;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

public class Thaumcraft implements IThirdParty {
    @Override
    public void preInit() {
        PneumaticRegistry.getInstance().getHelmetRegistry().registerBlockTrackEntry(new BlockTrackEntryThaumcraft());
    }

    // no init() method needed: recipe registration is done vis JSON

    @Override
    public void postInit() {
        // it appears aspect levels for common vanilla items are 5x what they were in TC4 (1.7.10)
        // scaling these accordingly...

        AspectList transAndCapAspects = new AspectList().add(Aspect.ENERGY, 10).add(Aspect.MECHANISM, 10).add(Aspect.METAL, 30);
        AspectList pcbAspects = new AspectList().add(Aspect.ENERGY, 5).add(Aspect.ORDER, 10).add(Aspect.METAL, 30);

        ThaumcraftApi.registerObjectTag(new ItemStack(Itemss.TURBINE_BLADE), new AspectList()
                .add(Aspect.METAL, 15)
                .add(Aspect.MOTION, 10)
                .add(Aspect.ENERGY, 20));
        ThaumcraftApi.registerObjectTag(new ItemStack(Itemss.CAPACITOR), transAndCapAspects);
        ThaumcraftApi.registerObjectTag(new ItemStack(Itemss.TRANSISTOR), transAndCapAspects);
        ThaumcraftApi.registerObjectTag(new ItemStack(Itemss.EMPTY_PCB), pcbAspects);
        ThaumcraftApi.registerObjectTag(new ItemStack(Itemss.UNASSEMBLED_PCB), pcbAspects);
        ThaumcraftApi.registerObjectTag(new ItemStack(Itemss.AIR_CANISTER), new AspectList().add(Aspect.METAL, 30).add(Aspect.ENERGY, 20).add(Aspect.AIR, 20));
    }
}
