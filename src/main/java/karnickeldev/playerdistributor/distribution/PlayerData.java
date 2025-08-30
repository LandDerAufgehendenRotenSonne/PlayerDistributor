package karnickeldev.playerdistributor.distribution;

import java.util.List;

/**
 * @author : KarnickelDev
 * @since : 30.08.2025
 **/
public class PlayerData {

    public final String name;
    public final String role;
    public String faction = "";
    public final List<String> friends;
    public final int row;

    public PlayerData(int row, String name, String role, List<String> friends) {
        this.row = row;
        this.name = name;
        this.role = role;
        this.friends = friends;
    }

}
