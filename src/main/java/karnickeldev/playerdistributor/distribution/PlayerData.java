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
    public final List<String> friends;
    public final int row;
    public final String discordId;
    public final boolean twitchChecked;
    public final boolean discordChecked;
    public final boolean guaranteed_slot;

    public String faction;
    public boolean accepted = false;

    public PlayerData(int row, String discordId, String name, boolean twitchChecked, boolean discordChecked, String role, boolean guaranteed_slot, List<String> friends) {
        this(row, discordId, name, twitchChecked, discordChecked, role, "", guaranteed_slot, friends);
    }

    public PlayerData(int row, String discordId, String name, boolean twitchChecked, boolean discordChecked, String role, String faction, boolean guaranteed_slot, List<String> friends) {
        this.row = row;
        this.name = name;
        this.role = role;
        this.friends = friends;
        this.faction = faction;
        this.discordId = discordId;
        this.twitchChecked = twitchChecked;
        this.discordChecked = discordChecked;
        this.guaranteed_slot = guaranteed_slot;
    }

    public PlayerData copy() {
        return new PlayerData(row, discordId, name, twitchChecked, discordChecked, role, faction, guaranteed_slot, new ArrayList<>(friends));
    }

    @Override
    public String toString() {
        return "(name=" + name + ",id=" + discordId + ",slot=" + guaranteed_slot +  ",accept=" + accepted + ")";
    }
}
