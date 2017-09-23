package igwmod.api;

import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * This event is fired on MinecraftForge.EVENT_BUS when a wikipage contains a [variable{<variableName>}] block, in which the <variableName is a string that is passed to this event.
 * This event can be used to retrieve values that you want to display on a wikipage that are dynamic, for example config options, or parameters that are prone to change.
 * You are responsible for a decent variable name. I'd suggest prefixing it with your modid to prevent collisions with other mods' variables.
 * If no subscriber has passed a return value for a variable name, it will be replaced by the variable name itself.
 */
public class VariableRetrievalEvent extends Event{

    public final String variableName;//The name passed via the wikipage's [variable] command.
    public String replacementValue; //The string value you should set with a replacement, like '15%', '1.0 bar'.

    public VariableRetrievalEvent(String variableName){
        this.variableName = variableName;
    }
}
