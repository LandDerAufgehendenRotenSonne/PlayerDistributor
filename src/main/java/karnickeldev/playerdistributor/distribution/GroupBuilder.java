package karnickeldev.playerdistributor.distribution;

import karnickeldev.playerdistributor.util.LoggingUtil;

import java.util.*;

/**
 * @author : KarnickelDev
 * @since : 30.08.2025
 **/
public class GroupBuilder {

    private static class DSU {
        private final int[] parent;
        private final byte[] depth;

        private DSU(int initialSize) {
            parent = new int[initialSize];
            depth = new byte[initialSize];
            for (int i = 0; i < initialSize; i++) {
                parent[i] = i;
                depth[i] = 0;
            }
        }

        public int findRoot(int p) {
            if (parent[p] != p) {
                // traverse to root, also change all parent pointers to root for faster lookup
                // this conceptually "flattens" the tree to a depth of 1
                /**
                 *      p     |      p
                 *    /  \    | / / | | \ \
                 *   o    o   | o o o o o o
                 *  /\   /\   |
                 * o o  o o   |
                 */
                parent[p] = findRoot(parent[p]);
            }
            return parent[p];
        }

        public void union(int a, int b) {
            int rootA = findRoot(a);
            int rootB = findRoot(b);

            // already in a union
            if (rootA == rootB) return;

            // attach smaller tree under bigger tree
            if (depth[rootA] < depth[rootB]) {
                parent[rootA] = rootB;
            } else if (depth[rootA] > depth[rootB]) {
                parent[rootB] = rootA;
            } else {
                parent[rootB] = rootA;
                depth[rootA]++;
            }
        }
    }


    public static List<PlayerGroup> buildGroups(List<PlayerData> playerData) {
        Map<String, Integer> idx = new HashMap<>(playerData.size());
        for(int i = 0; i < playerData.size(); i++) {
            idx.put(playerData.get(i).name, i);
        }

        DSU dsu = new DSU(playerData.size());

        // normalize name matching & union according to mode
        for (int i = 0; i < playerData.size(); i++) {
            PlayerData a = playerData.get(i);
            for (String friendName : a.friends) {
                Integer j = idx.get(friendName);
                if (j == null) continue;    // ignore unknown names
                if (j == i) continue;       // ignore self

                dsu.union(i, j);
            }
            if(i % 10 == 0) {
                LoggingUtil.printProgressBar("unionizing", (byte)20, i / (float) playerData.size());
            }
        }

        // collect groups by DSU root
        Map<Integer, PlayerGroup> byRoot = new HashMap<>();
        for (int i = 0; i < playerData.size(); i++) {
            int root = dsu.findRoot(i);
            byRoot.computeIfAbsent(root, r -> new PlayerGroup(null)).addMember(playerData.get(i));
        }

        // optional: sort groups largest-first
        List<PlayerGroup> groups = new ArrayList<>(byRoot.values());
        groups.sort(Comparator.comparingInt(PlayerGroup::size).reversed());
        return groups;
    }

}
