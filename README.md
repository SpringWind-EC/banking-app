# Banking App

## MongoDB setup

MongoDB stores employee account notes and activity logs. PostgreSQL stores core banking data, including balances and transactions. Use a local MongoDB server or a hosted MongoDB Atlas cluster. The Java app uses the MongoDB driver already declared in `pom.xml` and selects the server through the `MONGODB_URI` environment variable.

You'll need Java 21, Maven, and PowerShell for these instructions. Run the Java connection test from the repository root, which contains `pom.xml`. MongoDB Compass is optional and lets you browse the same database from a desktop app.


### Option A — Use a hosted MongoDB Atlas cluster

1. Sign in to Atlas and open or create a project. On the **Clusters** page, select **Create** and choose the **Free (`M0`)** tier if you need a new cluster. Wait for the cluster to become available. [Deploy a free cluster](https://www.mongodb.com/docs/atlas/tutorial/deploy-free-tier-cluster/)
2. Create a database user with read/write access to `banking_app`. Add your current IP address to the project's IP access list. Atlas uses this separate database user to authenticate client connections. [Atlas connection prerequisites](https://www.mongodb.com/docs/atlas/driver-connection/)
3. On the cluster, choose **Connect → Drivers → Java (Sync)** and copy the URI that starts with `mongodb+srv://`. Replace any username placeholder with your database username and replace the entire `<db_password>` placeholder, including the angle brackets, with the database user's password. Keep the URI options supplied by Atlas. If the password contains a reserved character, percent-encode it in the URI. For example, use `%40` for an `@` inside the password and keep the `@` before the cluster hostname. [Atlas connection string formatting](https://www.mongodb.com/docs/atlas/troubleshoot-connection/)
4. At the normal `PS ...>` prompt, type this command exactly as shown and press **Enter**:

   ```powershell
   $env:MONGODB_URI = Read-Host 'Paste your Atlas connection URI'
   ```

   PowerShell displays `Paste your Atlas connection URI:` and waits for input. **At that prompt, paste the full URI from step 3** without surrounding quotes and press **Enter** again. When `PS ...>` returns, the variable is set in that window. If you see the URI itself followed by a colon as the prompt, press **Ctrl+C** and rerun the command as shown.

5. Check the prefix without displaying credentials:

   ```powershell
   $env:MONGODB_URI -like 'mongodb+srv://*'
   ```

   `True` confirms the expected prefix. Keep the same PowerShell window open for the Java test. Keep credential-bearing URIs out of source files, Git, and screenshots.

### Verify the Java connection

From the repository root, run this command in the **same PowerShell window** where you set `MONGODB_URI`:

```powershell
mvn compile exec:java "-Dexec.mainClass=com.revature.banking.App"
```

Look for `Connected to MongoDB.` The current `App` runs a `ping` against `banking_app`. PowerShell passes `MONGODB_URI` to Maven and Java when you run this command. Set the variable again if you open a new window. [MongoDB Java connection guide](https://www.mongodb.com/docs/drivers/java/sync/current/connection/mongoclient/)

### Inspect or initialize the database with Compass

1. In Compass, select **Add New Connection**. For local MongoDB, use `mongodb://127.0.0.1:27017` or the URI for your local configuration. For Atlas, use the URI from **Connect → Compass**, with your database user credentials filled in. Click **Connect**. Use the same MongoDB deployment for Compass and Java. [Connect MongoDB Compass](https://www.mongodb.com/docs/compass/connect/)

