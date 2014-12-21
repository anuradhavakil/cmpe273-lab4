package edu.sjsu.cmpe.cache.client;

/**
 * Created by Anu on 12/17/2014.
 */
public class CRDTClient {
    private CacheServiceInterface cache1;
    private CacheServiceInterface cache2;
    private CacheServiceInterface cache3;

    public CRDTClient(){
        System.out.println("Starting Cache Client...");
        cache1 = new DistributedCacheService(
                "http://localhost:3000");
        cache2 = new DistributedCacheService(
                "http://localhost:3001");
        cache3 = new DistributedCacheService(
                "http://localhost:3002");
    }

    public void putAsync(long key, String value) {
        cache1.putAsync(key,value);

        cache2.putAsync(key, value);

        cache3.putAsync(key,value);


        if(cache1.getRequestSuccessful() + cache2.getRequestSuccessful() + cache3.getRequestSuccessful() < 2) {
            if(cache1.getRequestSuccessful() == 1){
                cache1.delete(key);
            }

            if(cache2.getRequestSuccessful() == 1){
                cache2.delete(key);
            }

            if(cache3.getRequestSuccessful() == 1){
                cache3.delete(key);
            }
        }

    }

    public void put(long key, String value){
        /** delete is required since concurrent hashmap
         *  Entry set in server does putIfAbsent
         */
        cache1.delete(key);
        cache2.delete(key);
        cache3.delete(key);
        cache1.put(key,value);
        cache2.put(key,value);
        cache3.put(key,value);

    }

    public CacheServiceInterface getInConsistentCache(long key){
       String cache1Value = cache1.getAsync(key);
       String cache2Value = cache2.getAsync(key);
       String cache3Value = cache3.getAsync(key);

        if(!cache1Value.equals(cache2Value)) {
            if(cache1Value.equals(cache3Value)){
                return cache2;
            }
            else if (cache2Value.equals(cache3Value)){
                return cache1;
            }
        } else if (!cache1Value.equals( cache3Value)){
            return cache3;
        }

        return null; // all entries are either same or all are different
    }

    public String get(long key) {
       String value="";
       CacheServiceInterface invalidCache = getInConsistentCache(key);
        if(invalidCache != null) {
            if(invalidCache == cache1) {
                value = cache2.get(key);
                System.out.println("Repairing cache1 ..");
                cache1.delete(key);
                cache1.put(key,value);
            }

            if(invalidCache == cache2) {
                value = cache1.get(key);
                System.out.println("Repairing cache2 ..");
                cache2.delete(key);
                cache2.put(key,value);
            }

            if(invalidCache == cache3) {
                value =  cache1.get(key);
                System.out.println("Repairing cache3 ..");
                cache3.delete(key);
                cache3.put(key,value);
            }
        } else {
            System.out.println("All Caches are valid ...");
            value = cache1.get(key);
        }
        return value;
    }

}
