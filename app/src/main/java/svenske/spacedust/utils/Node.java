package svenske.spacedust.utils;

import java.util.List;

/**
 * A generic data storage class that has a name, some data, and a list of child Nodes.
 */
public class Node {
    public String name;
    public Object data;
    public List<Node> children;
}
