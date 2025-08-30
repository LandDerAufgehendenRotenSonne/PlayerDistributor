package karnickeldev.playerdistributor.distribution;

import karnickeldev.playerdistributor.config.ConfigManager;

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


    public static DistributionResult distribute(ConfigManager configManager, List<PlayerGroup> groups, PlayerList playerList) {
        List<Faction> factions = new LinkedList<>();
        Map<String, Faction> factionByName = new HashMap<>();
        List<PlayerData> finalPlayerList = new ArrayList<>();

        for(String faction : configManager.getFactions()) {
            Faction f = new Faction(faction, configManager.getRoles());
            factions.add(f);
            factionByName.put(faction, f);
            System.out.println("created faction " + faction);
        }

        // distribute factioned players first
        for(PlayerData pd: playerList.getFactionedPlayers()) {
            if(pd.faction.isEmpty()) throw new IllegalStateException("Factioned Player with no Faction");
            factionByName.get(pd.faction).addPlayer(pd);
            finalPlayerList.add(pd);
        }


        // distribute unfactioned players
        for (int i = 0; i < groups.size(); i++) {
            PlayerGroup group = groups.get(i);
            Faction targetFaction = getSmallestFaction(factions);
            targetFaction.addGroup(group);
            finalPlayerList.addAll(group.getMembers());
        }

        return new DistributionResult(factions, finalPlayerList);
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
