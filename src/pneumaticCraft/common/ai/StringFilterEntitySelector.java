package pneumaticCraft.common.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import pneumaticCraft.common.util.PneumaticCraftUtils;

public class StringFilterEntitySelector implements IEntitySelector{

    private List<String> filter = new ArrayList<String>();

    @Override
    public boolean isEntityApplicable(Entity entity){
        List<String> filte = getFilter();
        for(String filt : filte) {
            if(PneumaticCraftUtils.isEntityValidForFilter(filt, entity)) return true;
        }
        return false;
    }

    protected List<String> getFilter(){
        return filter;
    }

    public StringFilterEntitySelector setFilter(String filter){
        this.filter = Arrays.asList(new String[]{filter});
        return this;
    }

    public StringFilterEntitySelector setFilter(List<String> filter){
        this.filter = filter;
        return this;
    }

    public StringFilterEntitySelector addEntry(String filterEntry){
        filter.add(filterEntry);
        return this;
    }

}
