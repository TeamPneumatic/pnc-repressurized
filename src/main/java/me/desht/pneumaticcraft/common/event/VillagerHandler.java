package me.desht.pneumaticcraft.common.event;

import com.google.common.collect.ImmutableSet;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.village.PointOfInterestType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class VillagerHandler {
    public static VillagerProfession mechanicProfession;

    public static void init() {
        // todo 1.14 where do the textures get defined?
        mechanicProfession = new VillagerProfession(Names.MOD_ID + ":mechanic", PointOfInterestType.HOME, ImmutableSet.of(), ImmutableSet.of());

        // todo 1.14 villagers

//                Textures.VILLAGER_MECHANIC, "minecraft:textures/entity/zombie_villager/zombie_villager.png");
//        VillagerCareer career = new VillagerCareer(mechanicProfession, Names.MOD_ID + ".mechanic");
//        career.addTrade(1,
//                new ListItemForEmeralds(ModItems.PCB_BLUEPRINT, new PriceInfo(10, 19)),
//                new ListItemForEmeralds(ModItems.NUKE_VIRUS, new PriceInfo(1, 5)),
//                new ListItemForEmeralds(ModItems.STOP_WORM, new PriceInfo(1, 5))
//        );
//        for (int i = 0; i < ItemAssemblyProgram.PROGRAMS_AMOUNT; i++) {
//            career.addTrade(1,
//                    new ListItemForEmeralds(new ItemStack(ModItems.ASSEMBLY_PROGRAM, 1, i), new PriceInfo(5, 11)));
//        }
    }

    @SubscribeEvent
    public static void onVillagerRegister(RegistryEvent.Register<VillagerProfession> event) {
        init();

        event.getRegistry().register(mechanicProfession);
    }
}
