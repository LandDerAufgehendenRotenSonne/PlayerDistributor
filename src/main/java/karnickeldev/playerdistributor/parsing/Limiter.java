package karnickeldev.playerdistributor.parsing;

import karnickeldev.playerdistributor.distribution.PlayerData;
import karnickeldev.playerdistributor.util.LoggingUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @author : KarnickelDev
 * @since : 07.09.2025
 **/
public class Limiter {

    private static final Random rnd = new Random();

    public static List<PlayerData> limitPlayerCount(long seed, int maxPlayers, List<PlayerData> players) {
        if(maxPlayers >= players.size()) {
            for(PlayerData pd: players) {
                if(!pd.guaranteed_slot) pd.accepted = true;
            }
            return new ArrayList<>(players);
        }

        LoggingUtil.info("Drawing random players");

        rnd.setSeed(seed);

        // pre-include all players with slot guarantee
        List<PlayerData> guaranteed = players.stream().filter(p -> p.guaranteed_slot).toList();

        List<PlayerData> winners = new ArrayList<>(guaranteed);

        // Collect remaining pool
        List<PlayerData> candidates = new ArrayList<>(players);
        candidates.removeAll(guaranteed);

        // Shuffle once and take the first N
        Collections.shuffle(candidates, rnd);

        winners.addAll(candidates.subList(0, Math.min(maxPlayers, candidates.size())));

        for(PlayerData pd: winners) {
            if(!pd.guaranteed_slot) pd.accepted = true;
        }

        LoggingUtil.info("Accepted " + guaranteed.size() + " players with guaranteed slots and " +
                (winners.size() - guaranteed.size()) + " random players (total: " + winners.size() + ')');

        return winners;
    }


}
