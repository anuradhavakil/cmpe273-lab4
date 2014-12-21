package edu.sjsu.cmpe.cache.client;

public class Client {

    public static void main(String[] args) throws Exception {
       CRDTClient crdtClient = new CRDTClient();
       write(crdtClient);
       readOnRepair(crdtClient);
    }

    /**
     * Implementing exercise part-1 Write.  At least two of three servers
     * will have 10=>foo10.  If only one server has the value and other two
     * don't then we will initiate delete for this server as well.
     * @param crdtClient
     */
    protected static void write(CRDTClient crdtClient) {
        long key = 10;
        String value = "foo10";
        crdtClient.putAsync(key,value);

    }

    /**
     * readOnRepair fixes one of the caches that has incorrect value
     * during the get.  Since the concurrentHashmap on server uses
     * putIfAbsent - in order to correctly update the cache - the entry
     * needs to be deleted first before executing put call.
     * @param crdtClient
     */
    protected static void readOnRepair(CRDTClient crdtClient) {
        long key = 21;
        String value = "foo21";
        String valueUpdated = "foo21updated";

        crdtClient.put(key, value);
        System.out.println("Sleeping for 30 seconds...");
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        crdtClient.put(key, valueUpdated);
        System.out.println("Sleeping for 30 seconds...");
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("The value in all the cacHes is - " + crdtClient.get(key));

    }
}
