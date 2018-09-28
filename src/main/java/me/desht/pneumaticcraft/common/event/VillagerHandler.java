package me.desht.pneumaticcraft.common.event;

import me.desht.pneumaticcraft.common.item.ItemAssemblyProgram;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.lib.Names;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.passive.EntityVillager.ListItemForEmeralds;
import net.minecraft.entity.passive.EntityVillager.PriceInfo;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerCareer;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;

@Mod.EventBusSubscriber
public class VillagerHandler {
    public static VillagerProfession mechanicProfession;

    public static void init() {
        mechanicProfession = new VillagerProfession(Names.MOD_ID + ":mechanic",
                Textures.VILLAGER_MECHANIC, "minecraft:textures/entity/zombie_villager/zombie_villager.png");
        VillagerCareer career = new VillagerCareer(mechanicProfession, Names.MOD_ID + ".mechanic");
        career.addTrade(1,
                new ListItemForEmeralds(Itemss.PCB_BLUEPRINT, new PriceInfo(10, 19)),
                new ListItemForEmeralds(Itemss.NUKE_VIRUS, new PriceInfo(1, 5)),
                new ListItemForEmeralds(Itemss.STOP_WORM, new PriceInfo(1, 5))
        );
        for (int i = 0; i < ItemAssemblyProgram.PROGRAMS_AMOUNT; i++) {
            career.addTrade(1,
                    new ListItemForEmeralds(new ItemStack(Itemss.ASSEMBLY_PROGRAM, 1, i), new PriceInfo(5, 11)));
        }
    }

    @SubscribeEvent
    public static void onVillagerRegister(RegistryEvent.Register<VillagerProfession> event) {
        init();

        event.getRegistry().register(mechanicProfession);
    }
}
