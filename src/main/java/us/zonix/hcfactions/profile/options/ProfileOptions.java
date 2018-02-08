package us.zonix.hcfactions.profile.options;

import us.zonix.hcfactions.profile.options.item.ProfileOptionsItemState;
import us.zonix.hcfactions.profile.options.item.ProfileOptionsItem;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import us.zonix.hcfactions.profile.options.item.ProfileOptionsItemState;

@Accessors(chain = true)
public class ProfileOptions {

    @Getter @Setter private boolean viewFoundDiamondMessages = true;
    @Getter @Setter private boolean viewDeathMessages = true;
    @Getter @Setter private boolean receivePublicMessages = true;
    @Getter @Setter private ProfileOptionsItemState modifyTabList = ProfileOptionsItemState.TAB_DETAILED;

    public Inventory getInventory() {
        Inventory toReturn = Bukkit.createInventory(null, 9, "Options");

        toReturn.setItem(1  , ProfileOptionsItem.FOUND_DIAMOND_MESSAGES.getItem(viewFoundDiamondMessages ? ProfileOptionsItemState.ENABLED : ProfileOptionsItemState.DISABLED));
        toReturn.setItem(3, ProfileOptionsItem.DEATH_MESSAGES.getItem(viewDeathMessages ? ProfileOptionsItemState.ENABLED : ProfileOptionsItemState.DISABLED));
        toReturn.setItem(5, ProfileOptionsItem.PUBLIC_MESSAGES.getItem(receivePublicMessages ? ProfileOptionsItemState.ENABLED : ProfileOptionsItemState.DISABLED));
        toReturn.setItem(7, ProfileOptionsItem.TAB_LIST_INFO.getItem(modifyTabList));

        return toReturn;
    }

}
