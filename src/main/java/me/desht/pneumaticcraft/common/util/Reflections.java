package me.desht.pneumaticcraft.common.util;

import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Gather all the reflection work we need to do here for ease of reference.
 */
public class Reflections {
    private static Field entityItem_Age;
    private static Field pim_isDigging;
    private static Field pim_acknowledged;
    private static Method msbl_isActivated;
    private static Method msbl_getEntityId;
    private static Field msbl_delay;
    private static Field msbl_activatingRangeFromPlayer;
    private static Field msbl_prevMobRotation;
    private static Field entity_inventoryHandsDropChances;
    private static Field entity_inventoryArmorDropChances;
    private static Field player_invulnerableDimensionChange;
    private static Field witch_potionUseTimer;
    @SideOnly(Side.CLIENT)
    private static Field itemRenderer_equippedProgress;
    @SideOnly(Side.CLIENT)
    private static Field itemRenderer_prevEquippedProgress;

    public static Class blaze_aiFireballAttack;
    public static Class ghast_aiFireballAttack;

    public static void init() {
        entityItem_Age = ReflectionHelper.findField(EntityItem.class, "field_70292_b", "age");
        pim_isDigging = ReflectionHelper.findField(PlayerInteractionManager.class, "field_73088_d", "isDestroyingBlock");
        pim_acknowledged = ReflectionHelper.findField(PlayerInteractionManager.class, "field_73097_j", "receivedFinishDiggingPacket");
        msbl_isActivated = ReflectionHelper.findMethod(MobSpawnerBaseLogic.class, "isActivated", "func_98279_f");
        msbl_getEntityId = ReflectionHelper.findMethod(MobSpawnerBaseLogic.class, "getEntityId", "func_190895_g");
        msbl_delay = ReflectionHelper.findField(MobSpawnerBaseLogic.class,  "field_98286_b", "spawnDelay");
        msbl_activatingRangeFromPlayer = ReflectionHelper.findField(MobSpawnerBaseLogic.class, "field_98289_l","activatingRangeFromPlayer");
        msbl_prevMobRotation = ReflectionHelper.findField(MobSpawnerBaseLogic.class, "field_98284_d", "prevMobRotation");
        entity_inventoryArmorDropChances = ReflectionHelper.findField(EntityLiving.class, "field_184655_bs", "inventoryArmorDropChances");
        entity_inventoryHandsDropChances = ReflectionHelper.findField(EntityLiving.class, "field_82174_bp", "inventoryHandsDropChances");
        player_invulnerableDimensionChange = ReflectionHelper.findField(EntityPlayerMP.class, "field_184851_cj", "invulnerableDimensionChange");
        witch_potionUseTimer = ReflectionHelper.findField(EntityWitch.class, "field_82200_e", "potionUseTimer");

        // access to non-public entity AI's for hacking purposes
        blaze_aiFireballAttack = findEnclosedClass(EntityBlaze.class, "AIFireballAttack", "a");
        ghast_aiFireballAttack = findEnclosedClass(EntityGhast.class, "AIFireballAttack", "c");
    }

    public static void initClientOnly() {
        itemRenderer_equippedProgress = ReflectionHelper.findField(ItemRenderer.class, "field_187469_f", "equippedProgressMainHand");
        itemRenderer_prevEquippedProgress = ReflectionHelper.findField(ItemRenderer.class, "field_187470_g", "prevEquippedProgressMainHand");
    }

    private static Class<?> findEnclosedClass(Class<?> cls, String... enclosedClassNames) {
        for (Class c : cls.getDeclaredClasses()) {
            for (String name : enclosedClassNames) {
                if (c.getSimpleName().equals(name)) {
                    return c;
                }
            }
        }
        return null;
    }

    public static int getItemAge(EntityItem item) {
        try {
            return entityItem_Age.getInt(item);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static void setItemAge(EntityItem item, int age) {
        try {
            entityItem_Age.setInt(item, age);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static boolean isDestroyingBlock(PlayerInteractionManager pim) {
        try {
            return pim_isDigging.getBoolean(pim);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return true;
        }
    }

    public static boolean isAcknowledged(PlayerInteractionManager pim) {
        try {
            return pim_acknowledged.getBoolean(pim);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return true;
        }
    }


    public static ResourceLocation getEntityId(MobSpawnerBaseLogic msbl) {
        try {
            return (ResourceLocation) msbl_getEntityId.invoke(msbl);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isActivated(MobSpawnerBaseLogic msbl) {
        try {
            return (boolean) msbl_isActivated.invoke(msbl);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int getSpawnDelay(MobSpawnerBaseLogic spawner) {
        try {
            return msbl_delay.getInt(spawner);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return 20;
        }
    }

    public static void setSpawnDelay(MobSpawnerBaseLogic spawner, int delay) {
        try {
            msbl_delay.setInt(spawner, delay);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void setPrevMobRotation(MobSpawnerBaseLogic spawner, double prev) {
        try {
            msbl_prevMobRotation.setDouble(spawner, prev);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static int getActivatingRangeFromPlayer(MobSpawnerBaseLogic spawner) {
        try {
            return msbl_activatingRangeFromPlayer.getInt(spawner);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return 16;
        }
    }

    private static final float[] DEF_ARMOR = { 0.085f, 0.085f, 0.085f, 0.085f };
    public static float[] getArmorDropChances(EntityLiving entityLiving) {
        try {
            return (float[]) entity_inventoryArmorDropChances.get(entityLiving);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return DEF_ARMOR;
        }
    }

    private static final float[] DEF_HANDS = { 0.085f, 0.085f };
    public static float[] getHandsDropChances(EntityLiving entityLiving) {
        try {
            return (float[]) entity_inventoryHandsDropChances.get(entityLiving);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return DEF_HANDS;
        }
    }

    /**
     * This is a nasty hack to get around "player moved wrongly!" messages, which can be caused if player movement
     * triggers a player teleport (e.g. player moves onto pressure plate, triggers air cannon with an entity tracker).
     *
     * @param player the player
     */
    public static void setInvulnerableDimensionChange(EntityPlayerMP player) {
        try {
            player_invulnerableDimensionChange.set(player, true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void setWitchPotionUseTimer(EntityWitch witch, int time) {
        try {
            witch_potionUseTimer.setInt(witch, time);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void resetEquippedProgress(ItemRenderer itemRenderer) {
        try {
            itemRenderer_equippedProgress.setInt(itemRenderer, 1);
            itemRenderer_prevEquippedProgress.setInt(itemRenderer, 1);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
