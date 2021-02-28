package svenske.spacedust.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;

/**
 * Serves as a medium of data transfer using a tree-like data structure.
 */
public class Node {

    //Static Data
    private static char DIVIDER_CHAR = ':';
    private static char INDENT_CHAR = ' ';

    //Data
    private List<Node> children;
    private String name;
    private String value;

    /**
     * Constructs this Node by giving it all of its properties upfront.
     */
    public Node(String name, String value, List<Node> children) {
        this.name = name;
        this.value = value;
        this.children = children;
    }

    /**
     * Constructs this Node by giving it a name, its data, and a single child.
     */
    public Node(String name, String value, Node child) {
        this(name, value, new ArrayList<Node>());
        this.children.add(child);
    }

    /**
     * Constructs this Node without giving it any children.
     */
    public Node(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Constructs this Node by solely giving it a name.
     */
    public Node(String name) {
        this.name = name;
    }

    /**
     * Constructs this Node without setting any of its properties initially.
     */
    public Node() {}

    /**
     * @return the list of this Node's children
     */
    public List<Node> get_children() { return this.children; }

    /**
     * @return the amount of children this Node has
     */
    public int get_child_count() { return this.children.size(); }

    /**
     * Adds a child to this Node.
     */
    public void add_child(Node child) {
        if (this.children == null) this.children = new ArrayList<>();
        this.children.add(child);
    }

    /**
     * Adds a child Node to this Node with just a name and a value
     */
    public void add_child(String name, String value) {
        this.add_child(new Node(name, value));
    }

    /**
     * Adds multiple children to this Node.
     */
    public void add_children(List<Node> children) {
        if (this.children == null) this.children = new ArrayList<>();
        if (children == null) return;
        this.children.addAll(children);
    }

    /**
     * Retrieves a child of this Node at the given index.
     */
    public Node get_child(int index) {
        if (index > this.children.size())
            throw new RuntimeException("[spdt/node" +
                    "Out of bounds: " + index + " length: " + this.children.size());
        return this.children.get(index);
    }

    /**
     * Retrieves a child of this Node with the given name.
     */
    public Node get_child(String name) {
        for (Node child : this.children) if (child.get_name().equals(name)) return child;
        return null;
    }

    /**
     * @return whether or not this Node has any children
     */
    public boolean has_children() {
        if (this.children == null) return false;
        return this.children.size() >= 1;
    }

    // Accessors
    public String get_name() { return this.name; }
    public String get_value() { return this.value; }
    public boolean has_name() { return this.name != null; }
    public boolean has_value() { return this.value != null; }

    // Mutators
    public void set_value(String value) { this.value = value; }
    public void set_name(String name) { this.name = name; }

    /**
     * Reads a Node from a given resource.
     */
    public static Node read_node(int resource_id) {
        Node node = new Node();
        List<String> data = Utils.read_resource(resource_id);
        read_node_recursively(node, data, 0);
        return node.get_child(0);
    }

    // TODO: Read node from file (for loading save game data)

    /**
     * Parses a given line of a node file using regex
     * @param line_number a line number corresponding to the line in the node folder, reported if
     *                    unable to match
     * @return an Object array [(String) name, (String) value, (Boolean) has children]
     */
    public static Object[] match_line(String line, int line_number) {

        // Check for actual data line
        Pattern p = Pattern.compile("^\\s*([!-~&&[^:]]+)\\s*:\\s*([!-~&&[^:\\{]]*)\\s*(\\{?)\\s*$");
        Matcher m = p.matcher(line);

        if (m.find()) {
            return new Object[] { m.group(1), m.group(2), m.group(3).equals("{") ? Boolean.TRUE : Boolean.FALSE };
        } else
            throw new RuntimeException("[spdt/node] " +
                    "Unable to match line (" + line_number + "): " + line);
    }

    public static boolean ending_bracket(String line) {
        // Check for ending bracket first
        Pattern p = Pattern.compile("^\\s*\\}\\s*$");
        Matcher m = p.matcher(line);
        return m.find();
    }

    /**
     * Recursively reads a Node from a given list of strings.
     * @param node the current Node in focus
     * @param file_contents the recursively static file contents
     * @param i the current line of file_contents in focus
     * @return the node in focus and its recursively read children
     */
    private static int read_node_recursively(Node node, List<String> file_contents, int i) {

        Object[] match = match_line(file_contents.get(i), i);

        // Set name and value if there is one
        Node curr = new Node((String)match[0]);
        if (((String)match[1]).length() > 0) curr.set_value((String)match[1]);

        // Check children
        if ((Boolean)match[2]) {

            if (i + 1 >= file_contents.size())
                throw new RuntimeException("[spdt/node] " +
                        "Unexpected EOF after opening bracket");
            boolean end_of_children = ending_bracket(file_contents.get(i + 1));
            while (!end_of_children) {
                i = read_node_recursively(curr, file_contents, i + 1);
                if (i + 1 >= file_contents.size())
                    throw new RuntimeException("[spdt/node] " +
                            "Unexpected EOF within children. Missing ending bracket line");
                end_of_children = ending_bracket(file_contents.get(i + 1));
            }
        }

        // Add child and return new file position
        node.add_child(curr);
        return i;
    }

    // TODO: Write node to file to save game data
}