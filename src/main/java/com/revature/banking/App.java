package com.revature.banking;

import com.mongodb.client.MongoClients;
import org.bson.Document;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        System.out.println("Hello World!");

        // // Uncomment to test MongoDB connection
        // try (var client = MongoClients.create(System.getenv("MONGODB_URI"))) {
        // var database = client.getDatabase("banking_app");
        // database.runCommand(new Document("ping", 1));
        // System.out.println("Connected to MongoDB.");
        // } catch (Exception e) {
        // System.err.println("Failed to connect to MongoDB: " + e.getMessage());
        // }

    }
}
