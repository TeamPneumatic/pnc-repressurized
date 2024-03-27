package me.desht.pneumaticcraft.api.pneumatic_armor.hacking;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/**
 * Convenience base class for hacks which are added as a persistent object to an entity's hack list. Such a hack will
 * be ticked each client &amp; server tick until the entity dies, or until the hack's
 * {@link IHackableEntity#afterHackTick(Entity)} method returns false.
 *
 * @param <T> entity class
 */
public abstract class AbstractPersistentEntityHack<T extends Entity> implements IHackableEntity<T> {
    private final Component info;
    private final Component postInfo;

    protected AbstractPersistentEntityHack(StockHackTypes hack) {
        this(hack.getPreKey(), hack.getPostKey());
    }

    protected AbstractPersistentEntityHack(String infoKey, String postInfoKey) {
        this.info = Component.translatable(infoKey);
        this.postInfo = Component.translatable(postInfoKey);
    }

    @Override
    public boolean canHack(Entity entity, Player player) {
        //noinspection unchecked
        return IHackableEntity.super.canHack(entity, player) && !alreadyHacked((T)entity); // cast is safe after checking super.canHack()
    }

    private boolean alreadyHacked(T entity) {
        ICommonArmorRegistry reg = PneumaticRegistry.getInstance().getCommonArmorRegistry();
        return reg.getCurrentEntityHacks(entity).stream()
                .anyMatch(hack -> hack.getHackableClass().isAssignableFrom(entity.getClass()));
    }

    @Override
    public void addHackInfo(T entity, List<Component> curInfo, Player player) {
        curInfo.add(info);
    }

    @Override
    public void addPostHackInfo(T entity, List<Component> curInfo, Player player) {
        curInfo.add(postInfo);
    }

    @Override
    public int getHackTime(T entity, Player player) {
        return 60;
    }

    @Override
    public void onHackFinished(T entity, Player player) {
        PneumaticRegistry.getInstance().getMiscHelpers().getHackingForEntity(entity, true)
                .ifPresent(hacking -> hacking.addHackable(this));
    }

    @Override
    public boolean afterHackTick(T entity) {
        return true;
    }

    /**
     * Convenience method to check if an entity has a specific persisting hack
     * @param entity the entity to check
     * @param cls class of the hack to check for
     * @param <T> entity type
     * @return true if the entity has been hacked, false otherwise
     */
    public static <T extends Entity> boolean hasPersistentHack(T entity, Class<? extends AbstractPersistentEntityHack<T>> cls) {
        ICommonArmorRegistry reg = PneumaticRegistry.getInstance().getCommonArmorRegistry();
        return reg.getCurrentEntityHacks(entity).stream()
                .anyMatch(hack -> cls.isAssignableFrom(hack.getClass()));
    }

    /**
     * Some common hack types which can be passed to the {@code AbstractPersistentEntityHack} constructor, only used
     * for armor HUD display purposes.
     */
    public enum StockHackTypes {
        DISARM("disarm", "disarmed"),
        STOP_TELEPORT("stopTeleport", "stoppedTeleport"),
        NEUTRALIZE("neutralize", "neutralized");

        private final String preHack;
        private final String postHack;

        StockHackTypes(String preHack, String postHack) {
            this.preHack = preHack;
            this.postHack = postHack;
        }

        String getPreKey() {
            return "pneumaticcraft.armor.hacking.result." + preHack;
        }

        String getPostKey() {
            return "pneumaticcraft.armor.hacking.finished." + postHack;
        }
    }
}
