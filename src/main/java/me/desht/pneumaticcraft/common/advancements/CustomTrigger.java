package me.desht.pneumaticcraft.common.advancements;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class CustomTrigger implements ICriterionTrigger {
    private final ResourceLocation ID;
    private final Map<PlayerAdvancements, CustomTrigger.Listeners> listeners = Maps.newHashMap();

    public CustomTrigger(String parString) {
        super();
        ID = RL(parString);
    }

    public CustomTrigger(ResourceLocation parRL) {
        super();
        ID = parRL;
    }

    /* (non-Javadoc)
     * @see net.minecraft.advancements.ICriterionTrigger#getId()
     */
    @Override
    public ResourceLocation getId() {
        return ID;
    }

    /* (non-Javadoc)
     * @see net.minecraft.advancements.ICriterionTrigger#addListener(net.minecraft.advancements.PlayerAdvancements, net.minecraft.advancements.ICriterionTrigger.Listener)
     */
    @Override
    public void addListener(PlayerAdvancements playerAdvancementsIn, ICriterionTrigger.Listener listener) {
        CustomTrigger.Listeners l = this.listeners.computeIfAbsent(playerAdvancementsIn, Listeners::new);
        l.add(listener);
    }

    /* (non-Javadoc)
     * @see net.minecraft.advancements.ICriterionTrigger#removeListener(net.minecraft.advancements.PlayerAdvancements, net.minecraft.advancements.ICriterionTrigger.Listener)
     */
    @Override
    public void removeListener(PlayerAdvancements playerAdvancementsIn, ICriterionTrigger.Listener listener) {
        CustomTrigger.Listeners tameanimaltrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (tameanimaltrigger$listeners != null) {
            tameanimaltrigger$listeners.remove(listener);

            if (tameanimaltrigger$listeners.isEmpty()) {
                this.listeners.remove(playerAdvancementsIn);
            }
        }
    }

    /* (non-Javadoc)
     * @see net.minecraft.advancements.ICriterionTrigger#removeAllListeners(net.minecraft.advancements.PlayerAdvancements)
     */
    @Override
    public void removeAllListeners(PlayerAdvancements playerAdvancementsIn) {
        this.listeners.remove(playerAdvancementsIn);
    }

    /**
     * Deserialize a ICriterionInstance of this trigger from the data in the JSON.
     *
     * @param json    the json
     * @param context the context
     * @return the tame bird trigger. instance
     */
    @Override
    public CustomTrigger.Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
        return new CustomTrigger.Instance(this.getId());
    }

    /**
     * Trigger.
     *
     * @param parPlayer the player
     */
    public void trigger(EntityPlayerMP parPlayer) {
        CustomTrigger.Listeners tameanimaltrigger$listeners = this.listeners.get(parPlayer.getAdvancements());

        if (tameanimaltrigger$listeners != null) {
            tameanimaltrigger$listeners.trigger(parPlayer);
        }
    }

    public static class Instance extends AbstractCriterionInstance {
        /**
         * Instantiates a new instance.
         */
        public Instance(ResourceLocation parID) {
            super(parID);
        }

        /**
         * Test.
         *
         * @return true, if successful
         */
        public boolean test() {
            return true;
        }
    }

    static class Listeners {
        private final PlayerAdvancements playerAdvancements;
        private final Set<ICriterionTrigger.Listener<CustomTrigger.Instance>> listeners = Sets.newHashSet();

        /**
         * Instantiates a new listeners.
         *
         * @param playerAdvancementsIn the player advancements in
         */
        public Listeners(PlayerAdvancements playerAdvancementsIn) {
            this.playerAdvancements = playerAdvancementsIn;
        }

        /**
         * Checks if is empty.
         *
         * @return true, if is empty
         */
        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        /**
         * Adds the.
         *
         * @param listener the listener
         */
        public void add(ICriterionTrigger.Listener listener) {
            this.listeners.add(listener);
        }

        /**
         * Removes the.
         *
         * @param listener the listener
         */
        public void remove(ICriterionTrigger.Listener listener) {
            this.listeners.remove(listener);
        }

        /**
         * Trigger.
         *
         * @param player the player
         */
        public void trigger(EntityPlayerMP player) {
            List<ICriterionTrigger.Listener<CustomTrigger.Instance>> list = null;

            for (ICriterionTrigger.Listener<CustomTrigger.Instance> listener : this.listeners) {
                if (listener.getCriterionInstance().test()) {
                    if (list == null) {
                        list = Lists.newArrayList();
                    }

                    list.add(listener);
                }
            }

            if (list != null) {
                for (ICriterionTrigger.Listener listener1 : list) {
                    listener1.grantCriterion(this.playerAdvancements);
                }
            }
        }
    }
}