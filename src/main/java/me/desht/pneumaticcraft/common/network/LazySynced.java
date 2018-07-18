package me.desht.pneumaticcraft.common.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Fields marked with this and also @DescSynced will be included in a desc packet. However, changes to this field won't
 * cause a packet to be sent; it's up to the holding object to decide when the sync should be done.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface LazySynced {

}
