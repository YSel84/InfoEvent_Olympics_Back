# InfoEvent Olympics Ticketing

This is a Java SpringBoot project build with maven.

**1/ how to clone**
Navigate to the local folder you want to use. 
Open your terminal. 
Do a git clone
Check the branch you want to use: git fetch origin then git branch
Choose your branch : git checkout branchname


**2/ how to run locally**
Go into your project folder. Using a terminal or an IDE, use the "build/rebuild project" feature or do a mvn compile.

To run: mvn spring-boot:run or use the dedicated run function in your IDE.

To compile a .jar : mvn clean package -DskipTests


**3/ Variables**
Here are the environment variables you will have to set. Use a .env or your IDE config tools.

Database related: 

DATABASE_URL -> where your DB is running, must include "jdbc:mysql: etc";

DB_USERNAME;

DB_PASSWORD;

Security related: 
JWT_SECRET;

JWT_EXPIRATION_MS;

ALLOWED_ORIGINS -> will default to local port 8081, set in app.properties


**3/Documentation**
Work in progress... Basic SWAGGER/OpenAPI support. 
Comments not yet Javadoc-y-fied. 

**4/Test**
To-do... 
