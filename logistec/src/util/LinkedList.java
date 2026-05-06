package logistec.util;

import java.util.Iterator;

/**
 * Generic singly linked list (TDA Lista Enlazada).
 * Used throughout the project as the base collection —
 * replaces java.util.ArrayList / LinkedList.
 *
 * <p>Complexity:
 * <ul>
 *   <li>addFirst / addLast / removeFirst – O(1)</li>
 *   <li>get(i) / remove(i) – O(n)</li>
 *   <li>contains / indexOf – O(n)</li>
 * </ul>
 *
 * @param <T> element type
 * @author LogísTEC Team
 * @version 1.0
 */
public class LinkedList<T> implements Iterable<T> {

    // ── Node ────────────────────────────────────────────────────────────────

    private static class Node<T> {
        T data;
        Node<T> next;
        Node(T data) { this.data = data; }
    }

    // ── Fields ───────────────────────────────────────────────────────────────

    private Node<T> head;
    private Node<T> tail;
    private int size;

    // ── Core operations ──────────────────────────────────────────────────────

    /** Add element at the end. O(1). */
    public void addLast(T item) {
        Node<T> node = new Node<>(item);
        if (head == null) { head = tail = node; }
        else              { tail.next = node; tail = node; }
        size++;
    }

    /** Add element at the front. O(1). */
    public void addFirst(T item) {
        Node<T> node = new Node<>(item);
        node.next = head;
        head = node;
        if (tail == null) tail = node;
        size++;
    }

    /** Remove and return the first element. O(1). */
    public T removeFirst() {
        if (head == null) throw new RuntimeException("List is empty");
        T data = head.data;
        head = head.next;
        if (head == null) tail = null;
        size--;
        return data;
    }

    /** Return the first element without removing. O(1). */
    public T peekFirst() {
        if (head == null) throw new RuntimeException("List is empty");
        return head.data;
    }

    /** Return element at index i. O(n). */
    public T get(int i) {
        rangeCheck(i);
        Node<T> cur = head;
        for (int j = 0; j < i; j++) cur = cur.next;
        return cur.data;
    }

    /** Set element at index i. O(n). */
    public void set(int i, T item) {
        rangeCheck(i);
        Node<T> cur = head;
        for (int j = 0; j < i; j++) cur = cur.next;
        cur.data = item;
    }

    /** Remove element at index i and return it. O(n). */
    public T remove(int i) {
        rangeCheck(i);
        if (i == 0) return removeFirst();
        Node<T> prev = head;
        for (int j = 0; j < i - 1; j++) prev = prev.next;
        T data = prev.next.data;
        prev.next = prev.next.next;
        if (prev.next == null) tail = prev;
        size--;
        return data;
    }

    /** Remove first occurrence of item. O(n). Returns true if found. */
    public boolean remove(T item) {
        Node<T> prev = null, cur = head;
        while (cur != null) {
            if (cur.data.equals(item)) {
                if (prev == null) head = cur.next;
                else              prev.next = cur.next;
                if (cur.next == null) tail = prev;
                size--;
                return true;
            }
            prev = cur;
            cur = cur.next;
        }
        return false;
    }

    /** True if list contains item. O(n). */
    public boolean contains(T item) {
        return indexOf(item) >= 0;
    }

    /** Index of first occurrence of item, or -1. O(n). */
    public int indexOf(T item) {
        Node<T> cur = head;
        for (int i = 0; i < size; i++) {
            if (cur.data.equals(item)) return i;
            cur = cur.next;
        }
        return -1;
    }

    public int size()    { return size; }
    public boolean isEmpty() { return size == 0; }

    /** Clear all elements. O(1). */
    public void clear() { head = tail = null; size = 0; }

    // ── Conversion ───────────────────────────────────────────────────────────

    /** Return array snapshot of elements. */
    @SuppressWarnings("unchecked")
    public T[] toArray() {
        Object[] arr = new Object[size];
        Node<T> cur = head;
        for (int i = 0; i < size; i++) { arr[i] = cur.data; cur = cur.next; }
        return (T[]) arr;
    }

    // ── Iterable ─────────────────────────────────────────────────────────────

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            Node<T> cur = head;
            public boolean hasNext() { return cur != null; }
            public T next() { T d = cur.data; cur = cur.next; return d; }
        };
    }

    // ── toString ─────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        Node<T> cur = head;
        while (cur != null) {
            sb.append(cur.data);
            if (cur.next != null) sb.append(", ");
            cur = cur.next;
        }
        return sb.append("]").toString();
    }

    // ── Private ──────────────────────────────────────────────────────────────

    private void rangeCheck(int i) {
        if (i < 0 || i >= size)
            throw new IndexOutOfBoundsException("Index " + i + " out of bounds for size " + size);
    }
}
