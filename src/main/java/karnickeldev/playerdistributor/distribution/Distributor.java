package karnickeldev.playerdistributor.distribution;

import karnickeldev.playerdistributor.config.ConfigManager;
import karnickeldev.playerdistributor.util.LoggingUtil;

import java.util.*;

/**
 * @author : KarnickelDev
 * @since : 30.08.2025
 **/
public class Distributor {

    public static class DistributionResult {

        public final List<PlayerData> distributedPlayers;
        public final List<Faction> factions;

        public DistributionResult(List<Faction> factions, List<PlayerData> distributedPlayers) {
            this.factions = new ArrayList<>(factions);
            this.distributedPlayers = new ArrayList<>(distributedPlayers);
        }
    }


    public static DistributionResult distribute(ConfigManager configManager, List<PlayerGroup> groups, List<PlayerData> validPlayerList) {
        List<Faction> factions = new LinkedList<>();
        Map<String, Faction> factionByName = new HashMap<>();
        List<PlayerData> finalPlayerList = new ArrayList<>();

        for(String faction : configManager.getFactions()) {
            Faction f = new Faction(faction, configManager.getRoles());
            factions.add(f);
            factionByName.put(faction, f);
            LoggingUtil.info("created faction " + faction);
        }

        // distribute factioned players first
        for(PlayerData pd: validPlayerList) {
            if(pd.faction.isEmpty()) continue;
            factionByName.get(pd.faction).addPlayer(pd);
            finalPlayerList.add(pd);
        }


        // distribute unfactioned players
        for (PlayerGroup group : groups) {
            if (!groupHasNoFaction(group.getMembers())) {
                throw new IllegalStateException("Groups should not contain members with a Faction");
            }

            Faction targetFaction = getSmallestFaction(factions);
            targetFaction.addGroup(group);
            finalPlayerList.addAll(group.getMembers());
        }

        return new DistributionResult(factions, finalPlayerList);
    }

    private static boolean groupHasNoFaction(List<PlayerData> players) {
        for(PlayerData pd: players) {
            if(!pd.faction.isEmpty()) return false;
        }
        return true;
    }

    private static Faction getSmallestFaction(List<Faction> factions) {
        Faction r = factions.get(0);
        for(Faction f: factions) {
            if(f.playerCount() < r.playerCount()) {
                r = f;
            }
        }
        return r;
    }

}
