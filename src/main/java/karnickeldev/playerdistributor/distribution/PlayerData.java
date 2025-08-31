package karnickeldev.playerdistributor.distribution;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : KarnickelDev
 * @since : 30.08.2025
 **/
public class PlayerData {

    public final String name;
    public final String role;
    public String faction;
    public final List<String> friends;
    public final int row;
    public final String discordId;
    public final boolean twitchChecked;
    public final boolean discordChecked;

    public PlayerData(int row, String discordId, String name, boolean twitchChecked, boolean discordChecked, String role, List<String> friends) {
        this(row, discordId, name, twitchChecked, discordChecked, role, "", friends);
    }

    public PlayerData(int row, String discordId, String name, boolean twitchChecked, boolean discordChecked, String role, String faction, List<String> friends) {
        this.row = row;
        this.name = name;
        this.role = role;
        this.friends = friends;
        this.faction = faction;
        this.discordId = discordId;
        this.twitchChecked = twitchChecked;
        this.discordChecked = discordChecked;
    }

    public PlayerData copy() {
        return new PlayerData(row, discordId, name, twitchChecked, discordChecked, role, faction, new ArrayList<>(friends));
    }
}
