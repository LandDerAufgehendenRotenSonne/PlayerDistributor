package karnickeldev.playerdistributor.distribution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : KarnickelDev
 * @since : 30.08.2025
 **/
public class Faction {

    public final String name;
    private final List<PlayerGroup> groups;
    private final List<PlayerData> players;

    private final List<String> roles;

    private final Map<String, Integer> roleCount = new HashMap<>();

    public Faction(String name, List<String> roles) {
        this.name = name;
        this.groups = new ArrayList<>();
        this.players = new ArrayList<>();

        this.roles = roles;
    }

    public void addGroup(PlayerGroup group) {
        if(!groups.contains(group)) {
            groups.add(group);
            for(PlayerData player: group.getMembers()) addPlayer(player);
        }
    }

    public void removeGroup(PlayerGroup group) {
        groups.remove(group);
        for(PlayerData player: group.getMembers()) removePlayer(player);
    }

    public void addPlayer(PlayerData playerData) {
        playerData.faction = name;  //TODO: bad
        players.add(playerData);
        roleCount.put(playerData.role, roleCount.getOrDefault(playerData.role, 0) + 1);
    }

    public void removePlayer(PlayerData playerData) {
        players.remove(playerData);
        roleCount.put(playerData.role, roleCount.getOrDefault(playerData.role, 1) - 1);
    }

    public int groupCount() {
        return groups.size();
    }

    public int playerCount() {
        return players.size();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('{');

        builder.append(name).append("=[");

        builder.append("size=").append(playerCount()).append(",");
        for(int i = 0; i < roles.size(); i++) {
            builder.append(roles.get(i)).append('=');
            builder.append(roleCount.getOrDefault(roles.get(i), 0));
            if(i < roles.size()-1) builder.append(',');
        }
        builder.append("]}");
        return builder.toString();
    }


}
