package common;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * <p>
 * Provides an enhanced Iterator interface that allows users to peek at the next element in the
 * iteration without advancing the iterator.
 * </p>
 * 
 * <p>
 * This implementation was inspired by another source. For more information, please visit
 * <a href="https://medium.com/@harycane/peeking-iterator-ef69ce9ef788">this site</a>.
 * </p>
 * 
 * <p>
 * <b>Supported Platforms:</b>
 * </p>
 * 
 * <ul>
 * <li>Windows</li>
 * <li>*NIX operating systems</li>
 * </ul>
 * 
 * <p>
 * <b>Change logs:</b>
 * </p>
 * 
 * <ul>
 * <li>Created by Trevor Maggs on October 12, 2021</li>
 * </ul>
 * 
 * @author Trevor Maggs
 * @version 0.1
 * @since October 12, 2021
 */
public class PeekingIterator<T> implements Iterator<T>
{
    private T cursor;
    private Iterator<T> iter;
    private boolean noSuchElement;

    /**
     * Constructs a new peeking iterator, caching the first element in advance.
     * 
     * @param itr
     *        the Iterator object
     */
    public PeekingIterator(Iterator<T> itr)
    {
        iter = itr;
        advanceIterator();
    }

    /**
     * Constructs a new peeking iterator from the specified Collection object, caching the first
     * element in advance.
     *
     * @param coll
     *        the specified Collection object
     */
    public PeekingIterator(Collection<T> coll)
    {
        this(coll.iterator());
    }

    /**
     * Constructs a new peeking iterator from the specified Map's collection of values, caching the
     * first element in advance.
     * 
     * @param <K>
     *        Key component of the specified map instance
     * @param map
     *        the Map object
     */
    public <K> PeekingIterator(Map<K, T> map)
    {
        this(map.values().iterator());
    }

    /**
     * Constructs a new peeking iterator from the specified generic array, caching the first element
     * in advance.
     * 
     * @param arr
     *        the generic array
     */
    public PeekingIterator(T[] arr)
    {
        this(Arrays.asList(arr));
    }

    /**
     * Advances the iterator to cache the next element, allowing either the {@link #next} or
     * {@link #peek} method to be called without throwing an exception.
     */
    private void advanceIterator()
    {
        if (iter.hasNext())
        {
            // Cache the next element
            cursor = iter.next();
        }

        else
        {
            noSuchElement = true;
        }
    }

    /**
     * Returns the next element without advancing the iterator.
     * 
     * @return the cached next element
     * 
     * @throws NoSuchElementException
     *         if there are no more elements in the iteration
     */
    public T peek()
    {
        return cursor;
    }

    /**
     * Returns the cached item from the last element in the iteration and advances the iterator.
     * 
     * @return the cached item from the last element in the iteration
     * @throws NoSuchElementException
     *         if the iteration contains no more elements
     */
    @Override
    public T next()
    {
        T result = cursor;
        advanceIterator();

        return result;
    }

    /**
     * Returns whether the iteration has more elements to retrieve.
     *
     * @return boolean true if the iteration has more elements
     */
    @Override
    public boolean hasNext()
    {
        return !noSuchElement;
    }
}