package karnickeldev.playerdistributor.distribution;

import java.util.List;

/**
 * @author : KarnickelDev
 * @since : 30.08.2025
 **/
public class PlayerList {

    private final List<PlayerData> factionedPlayers;
    private final List<PlayerData> unfactionedPlayers;

    public PlayerList(List<PlayerData> factionedPlayers, List<PlayerData> unfactionedPlayers) {
        this.factionedPlayers = factionedPlayers;
        this.unfactionedPlayers = unfactionedPlayers;
    }

    public List<PlayerData> getFactionedPlayers() {
        return factionedPlayers;
    }

    public List<PlayerData> getUnfactionedPlayers() {
        return unfactionedPlayers;
    }
}
