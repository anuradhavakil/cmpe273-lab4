package edu.sjsu.cmpe.cache.client;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Distributed cache service
 *
 */
public class DistributedCacheService implements CacheServiceInterface {
    private final String cacheServerUrl;
    private int requestSuccessful;
    private CountDownLatch lock;

    public DistributedCacheService(String serverUrl) {
        this.cacheServerUrl = serverUrl;
        requestSuccessful = 0;
        lock = new CountDownLatch(1);
    }

    /**
     * @see edu.sjsu.cmpe.cache.client.CacheServiceInterface#get(long)
     */
    @Override
    public String get(long key) {
        HttpResponse<JsonNode> response = null;
        try {
            response = Unirest.get(this.cacheServerUrl + "/cache/{key}")
                    .header("accept", "application/json")
                    .routeParam("key", Long.toString(key)).asJson();
        } catch (UnirestException e) {
            System.err.println(e);
        }
        String value = response.getBody().getObject().getString("value");

        return value;
    }

    /**
     * @see edu.sjsu.cmpe.cache.client.CacheServiceInterface#put(long,
     *      java.lang.String)
     */
    @Override
    public void put(long key, String value) {
        HttpResponse<JsonNode> response = null;
        try {
            response = Unirest
                    .put(this.cacheServerUrl + "/cache/{key}/{value}")
                    .header("accept", "application/json")
                    .routeParam("key", Long.toString(key))
                    .routeParam("value", value).asJson();
        } catch (UnirestException e) {
            System.err.println(e);
        }

        if (response == null || response.getCode() != 200) {
            System.out.println("Failed to add to the cache.");
        } else {
            System.out.println("Added to cache successfully");
        }
    }

    @Override
    public void putAsync(long key, final String value) {
        try{


        Future<HttpResponse<JsonNode>> future = Unirest.put(this.cacheServerUrl + "/cache/{key}/{value}")
                .header("accept", "application/json")
                .routeParam("key", Long.toString(key))
                .routeParam("value", value)
                .asJsonAsync(new Callback<JsonNode>() {

                    public void failed(UnirestException e) {
                        System.out.println("The request has failed for " + cacheServerUrl);

                    }

                    public void completed(HttpResponse<JsonNode> response) {
                        int code = response.getCode();
                        JsonNode body = response.getBody();
                        InputStream rawBody = response.getRawBody();

                        requestSuccessful = (response.getCode() == 200) ? 1 : 0;
                        lock.countDown();
                        System.out.println("Response Code:" + response.getCode() +" - The async put was successful for " + cacheServerUrl);

                    }

                    public void cancelled() {
                        System.out.println("The request has been cancelled for " + cacheServerUrl);
                    }

                });

         lock.await(10, TimeUnit.SECONDS);
        }  catch (InterruptedException e) {
            System.err.println(e);
        }

    }

    @Override
    public void delete(long key) {
        HttpResponse<JsonNode> response = null;
        try {
            response = Unirest
                    .delete(this.cacheServerUrl + "/cache/{key}")
                    .header("accept", "application/json")
                    .routeParam("key", Long.toString(key)).asJson();

        } catch (UnirestException e) {
            System.err.println(e);
        }

        if (response == null || response.getCode() != 200) {
            System.out.println("Failed to delete from cache for " + cacheServerUrl);
        } else {
            System.out.println("Delete completed successfully for cache " + cacheServerUrl);
        }

    }

    @Override
    public int getRequestSuccessful(){
        return requestSuccessful;
    }

    @Override
    public String getAsync(long key)  {
        HttpResponse<JsonNode> response = null;
        String value = "";
        try {
            response = Unirest.get(this.cacheServerUrl + "/cache/{key}")
                    .header("accept", "application/json")
                    .routeParam("key", Long.toString(key)).asJsonAsync().get();

            if (response.getCode() != 200) {
                System.out.println("Error retrieving value for GetAsync - " + cacheServerUrl);
            } else {
                System.out.println("Get Async successful for " + cacheServerUrl);
                value = response.getBody().getObject().getString("value");
            }
        } catch (InterruptedException e){
            System.err.println(e);

        } catch (ExecutionException e){
            System.err.println(e);
        }

        return value;
    }
}