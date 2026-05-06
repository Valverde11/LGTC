package logistec.util;

/**
 * Generic LIFO Stack backed by {@link LinkedList}.
 * Used by DFS and other algorithms.
 *
 * <p>All operations O(1).
 *
 * @param <T> element type
 * @author LogísTEC Team
 * @version 1.0
 */
public class Stack<T> {

    private final LinkedList<T> list = new LinkedList<>();

    /** Push element onto the top. O(1). */
    public void push(T item) { list.addFirst(item); }

    /** Pop and return element from the top. O(1). */
    public T pop() { return list.removeFirst(); }

    /** Peek at the top element without removing. O(1). */
    public T peek() { return list.peekFirst(); }

    public boolean isEmpty() { return list.isEmpty(); }
    public int size()        { return list.size(); }
    public void clear()      { list.clear(); }

    @Override
    public String toString() { return list.toString(); }
}
