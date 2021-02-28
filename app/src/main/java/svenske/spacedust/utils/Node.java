package svenske.spacedust.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

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
        read_node_recursively(node, data, 0, 0);
        return node;
    }

    // TODO: Read node from file (for loading save game data)

    /**
     * Recursively reads a Node from a given list of strings.
     * @param node the current Node in focus
     * @param file_contents the recursively static file contents
     * @param i the current line of file_contents in focus
     * @param indent the current indent in terms of number of characters
     * @return the node in focus and its recursively read children
     */
    private static int read_node_recursively(Node node, List<String> file_contents, int i, int indent) {

        // Format next line and find dividing point
        String nextLine = file_contents.get(i); //get line
        nextLine = nextLine.substring(indent); //remove indent
        int divider_location = -1; //location of the divider in line
        for (int j = 0; j < nextLine.length() && divider_location == -1; j++)
            if (nextLine.charAt(j) == Node.DIVIDER_CHAR) divider_location = j; //find divider

        // Throw error if no divider found
        if (divider_location == -1)
            throw new RuntimeException("[spdt/node] " +
                    "could not find divider in line '" + nextLine + "'");

        // Create node and set name
        Node curr = new Node();
        String possible_name = nextLine.substring(0, divider_location);
        if (!possible_name.equals("")) curr.set_name(nextLine.substring(0, divider_location)); //create node with name

        // Set node value if there is one
        String possible_value = nextLine.substring(divider_location + 1); //grab possible value
        if (!possible_value.equals(" ") && possible_value.length() > 0) { //if possible value has substance
            //set value (remove first space)
            curr.set_value(possible_value.substring(1));
            if (curr.value.endsWith("\r") || curr.value.endsWith("\n"))
                curr.value = curr.value.substring(0, curr.value.length() - 1);
        }

        // Check for more file
        if (i + 1 <= file_contents.size()) { //if not eof

            // Check for child nodes
            if (file_contents.get(i + 1).contains("{")) { //if the node has children
                i += 2; //iterate twice
                indent++; //iterate indent
                while (!file_contents.get(i).contains("}")) { //while there are more children

                    // Add child
                    Node child = new Node(); //create child node
                    i = read_node_recursively(child, file_contents, i, indent); //recursively read child, keep track of file position
                    curr.add_child(child); //add child

                    // Throw error if file suddenly stops
                    if ((i + 1) > file_contents.size())
                        throw new RuntimeException("[spdt/node] " +
                                "unexpected stop in file at line " + i);

                    i += 1;
                }
            }
        }

        // Set node, return current position in file
        node.set_name(curr.get_name());
        node.set_value(curr.get_value());
        node.add_children(curr.get_children());
        return i;
    }

    // TODO: Write node to file to save game data
}