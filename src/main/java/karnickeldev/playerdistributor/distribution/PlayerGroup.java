package karnickeldev.playerdistributor.distribution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : KarnickelDev
 * @since : 30.08.2025
 **/
public class PlayerGroup {

    private final List<PlayerData> members;
    private final Map<String, Integer> roleCounts = new HashMap<>(4);

    public PlayerGroup(List<PlayerData> members) {
        this.members = new ArrayList<>();
        if(members != null) this.members.addAll(members);

        for(PlayerData pd : this.members) {
            roleCounts.put(pd.role, roleCounts.getOrDefault(pd.role, 0) + 1);
        }
    }

    public List<PlayerData> getMembers() {
        return members;
    }

    public void addMember(PlayerData member) {
        members.add(member);
        roleCounts.put(member.role, roleCounts.getOrDefault(member.role, 0) + 1);
    }

    public int size() {
        return members.size();
    }

    @Override
    public String toString() {
        return "{size=" + size() + "}";
    }

}
