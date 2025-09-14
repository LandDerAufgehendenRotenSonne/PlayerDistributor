package karnickeldev.playerdistributor.distribution;

import karnickeldev.playerdistributor.core.PlayerDistributor;
import karnickeldev.playerdistributor.util.LoggingUtil;

import java.util.*;

/**
 * @author : KarnickelDev
 * @since : 30.08.2025
 **/
public class GroupBuilder {

    public enum LinkMode {
        ANY_LINK,
        MUTUAL
    }

    private static class DSU {
        private final int[] parent;
        private final byte[] depth;
        private final int[] groupSize;

        private DSU(int initialSize) {
            parent = new int[initialSize];
            depth = new byte[initialSize];
            groupSize = new int[initialSize];
            for (int i = 0; i < initialSize; i++) {
                parent[i] = i;
                depth[i] = 0;
                groupSize[i] = 1;
            }
        }

        public int findRoot(int p) {
            if (parent[p] != p) {
                // traverse to root, also change all parent pointers to root for faster lookup
                // this conceptually "flattens" the tree to a depth of 1
                /*
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

            // optional: max group size check
            int sizeA = groupSize[rootA];
            int sizeB = groupSize[rootB];
            if(sizeA + sizeB > PlayerDistributor.GROUP_LIMIT) return; // skip merge

            // attach smaller tree under bigger tree
            if (depth[rootA] < depth[rootB]) {
                parent[rootA] = rootB;
                groupSize[rootB] += sizeA;
            } else if (depth[rootA] > depth[rootB]) {
                parent[rootB] = rootA;
                groupSize[rootA] += sizeB;
            } else {
                parent[rootB] = rootA;
                depth[rootA]++;
                groupSize[rootA] += sizeB;
            }
        }
    }


    public static List<PlayerGroup> buildGroups(List<PlayerData> playerData) {
        Map<String, Integer> idx = new HashMap<>(playerData.size());
        for(int i = 0; i < playerData.size(); i++) {
            idx.put(playerData.get(i).name.toLowerCase(), i);
            idx.put(playerData.get(i).discordId.toLowerCase(), i);
        }

        DSU dsu = new DSU(playerData.size());

        // normalize name matching & union according to mode
        for(int i = 0; i < playerData.size(); i++) {
            PlayerData a = playerData.get(i);
            for(String friendName : a.friends) {
                Integer j = idx.get(friendName.toLowerCase());
                if(j == null) {
                    LoggingUtil.warn("Unknown name " + friendName);
                    continue;    // ignore unknown names
                }
                if(j == i) continue;       // ignore self

                // Decide whether to union based on mode
                if(PlayerDistributor.LINK_MODE == LinkMode.MUTUAL) {
                    PlayerData b = playerData.get(j);
                    if(contains(b.friends, a.name.toLowerCase()) || contains(b.friends, a.discordId.toLowerCase())) {  // mutual check
                        dsu.union(i, j);
                    }
                } else {
                    dsu.union(i, j);
                }
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

    private static boolean contains(List<String> list, String value) {
        for(String s: list) {
            if(s.equalsIgnoreCase(value)) return true;
        }
        return false;
    }

}
