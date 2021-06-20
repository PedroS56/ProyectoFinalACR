package proyectofinalacr.utils;


import java.util.Comparator;
import proyectofinalacr.clases.Node;

public class portSorter implements Comparator<Node> {
    @Override
    public int compare(Node o1, Node o2) {
        Integer i1 = new Integer(o1.getMyPort());
        Integer i2 = new Integer(o2.getMyPort());
        return i1.compareTo(i2);
    }
}
