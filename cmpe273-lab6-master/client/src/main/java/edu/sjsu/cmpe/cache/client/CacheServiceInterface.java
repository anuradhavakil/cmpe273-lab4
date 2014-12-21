package edu.sjsu.cmpe.cache.client;

/**
 * Cache Service Interface
 * 
 */
public interface CacheServiceInterface {
    public int getRequestSuccessful();

    public String getAsync(long key);

    public String get(long key);

    public void put(long key, String value);

    public void putAsync(long key, String value) ;

    public void delete(long key);

}
