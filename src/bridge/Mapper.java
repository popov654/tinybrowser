package bridge;

import htmlparser.Node;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import render.Block;

/**
 *
 * @author Alex
 */
public class Mapper {

    public static void add(Node node, Block block) {
        map.put(node, block);
    }

    public static void remove(Node node) {
        map.remove(node);
    }

    public static void remove(Block block) {
        Set<Entry<Node, Block>> entries = map.entrySet();
        for (Entry<Node, Block> entry: entries) {
            if (entry.getValue() == block) {
                map.remove(entry.getKey());
            }
        }
    }

    public static HashMap<Node, Block> map = new HashMap<Node, Block>();

}
