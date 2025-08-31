package karnickeldev.playerdistributor.distribution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : KarnickelDev
 * @since : 31.08.2025
 **/
public class FactionPreAssigner {


    /**
     * Filters out all players with a preassigned faction
     * and also assigns players that are friended by such a player
     * a faction if they do not have one already
     *
     * @param playerList Input Player-List
     * @return Players that still have no faction
     */
    public static List<PlayerData> filterAndPreAssignFactions(List<PlayerData> playerList) {
        List<PlayerData> filtered = new ArrayList<>(playerList);

        Map<String, PlayerData> byName = new HashMap<>();

        for(PlayerData pd: playerList) {
            byName.put(pd.name.toLowerCase(), pd);
        }

        for(PlayerData pd: playerList) {
            if(!pd.faction.isEmpty()) {
                filtered.remove(pd);

                for(String f: pd.friends) {
                    PlayerData friend = byName.get(f.toLowerCase());
                    if(friend == null) continue;
                    if(friend.faction.isEmpty()) {
                        friend.faction = pd.faction;
                        filtered.remove(friend);
                    }
                }
            }

        }

        return filtered;
    }


}
