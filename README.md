# Personal budget

This is a simple project for calculate how much i spending money. To see what it looks lke, you can go to the https://personal-budget.tk/. By default you logged as demo user, you can:

1. view and add purchase transactions.
   
   ![](https://media.giphy.com/media/eyR984gXuELjpMTYE6/giphy.gif)

2. view and add debt transactions.
   
   ![](https://media.giphy.com/media/JhMzzsKDMjCqqbns35/giphy.gif)

3. view and add salary transactions.
   
   ![](https://media.giphy.com/media/MmmpZ8NIYbufFQg8bu/giphy.gif)

4. view reports. 
   
   ![](https://media.giphy.com/media/aZcFHZIiXKJxNK2Cbn/giphy.gif)

To make process of adding a new purchase or salary or debt transaction easier, also created [Telegram bot](https://github.com/vsafonin/personal-budget-bot), for use it:

1. Registration on application. 

2. Go to UserName -> Personal Area -> Generate Token
   
   ![](https://media.giphy.com/media/5VhbtfuFz6LCOA2MNQ/giphy.gif)

3. Go to Telegram app and find https://t.me/The_personal_budget_bot

4. press /start , and after hello message send token in response.
   
   ![](https://media.giphy.com/media/IBT9e49jadXXeHdjTX/giphy.gif)

# Requirments

1. Java jre 17

2. Mysql server

# Run on your PC

For start this app on your pc, you have multiple options:

## Run via maven
   
   - Install jre 17 on your pc. 
   
   - install mysql server and create a database "personal_budget"
   
   - set MYSQL_USER and MYSQL_PASSWORD parameters on ENV or you can edit src/main/resources/application.properties
   
   - set email parametrs on ENV (or you can edit src/main/resources/mail/emailconfig.properties):
      
      - MAIL_FROM
      
      - MAIL_HOST
      
      - MAIL_PASSWORD
      
      - MAIL_PORT
      
      - MAIL_PROTOCOL
      
      - MAIL_USER
   
   - go to path where you extracted this app
   
   - open the console and run: 
      
      ```bash
      ./mvnw spring-boot:run
      ```

## Run via java -jar
   
   - folow the steps 1-5 in [Run via maven](https://github.com/vsafonin/personal-budget#run-via-maven)
   
   - open the console and run:
      
      ```bash
      ./mvn package -DskipTests
      ```
   
   - run in the console (if you want to change the app name - edit pom.xml file):
      
      ```bash
      java -jar java -jar target/personalAccounter-0.0.1.jar
      ```

## Run via Docker-compose
   
   - build the mysql docker image
      
      ```bash
      cd mysql-docker
      docker build . -t mysql-personal-app
      cd ..
      ```
   
   - build the personal-budget-app docker image
      
      ```bash
      docker build . -t personal-budget-app    
      ```

      if you want to use ssl, and you have the certificate
      put your *.p12 file to ssl/keystore.p12

      ```bash
      docker build . -t personal-budget-app --build-arg SSL_ENABLE=true
      ```
   
   - go to path where you extracted sources files of the personal-budget-bot
      
      - clone [vsafonin/personal-budget-bot · GitHub](https://github.com/vsafonin/personal-budget-bot.git)
      
      - build the personal-budget-bot docker image
         
         ```bash
         docker build . -t personal-budget-bot
         ```
   
   - create the docker-compose.yml file
      
      ```yml
      version: '3'
      services:
        personal-budget-app:
          container_name: personal-budget-app
          image: personal-budget-app
          restart: always
          ports:
           - 443:9443
          environment:
            - MYSQL_HOST=mysqldb
            - MYSQL_PORT=3306
            - MYSQL_USER=root
            - MYSQL_PASSWORD=password
            - ROOT_ADDRESS_APP=http://localhost:9443
            - JWT_TOKEN_PASS=some_string_pass
            - SSL_PASSWORD=if_not_use_delete_it
            - MAIL_FROM=your_mail@mail.ru
            - MAIL_HOST=smtp.mail.ru
            - MAIL_PASSWORD=password
            - MAIL_PORT=465
            - MAIL_PROTOCOL=smtp
            - MAIL_USER=your_mail_user
        mysqldb:
          container_name: mysqldb
          image: mysql-personal-app
          restart: always
          volumes:
            - /var/app/personal-app-db:/var/lib/mysql
          ports:
            - 3307:3306
          environment:
            - MYSQL_DATABASE=personal_budget
            - MYSQL_ROOT_PASSWORD=password
        personal-budget-bot:
          container_name: personal-budget-bot
          image: personal-budget-bot
          restart: always
          environment:
            - MYSQL_HOST=mysqldb
            - MYSQL_PORT=3306
            - MYSQL_USER=root
            - MYSQL_PASSWORD=password
            - BASE_URL=localhost
            - BASE_PORT=9443
            - BASE_SCHEME=http
            - NAME_TG_BOT=The_personal_budget_bot
            - TG_TOKEN=TGTOKEN_FROM_TG
      ```

  - run it
   
   ```bash
   docker-compose up
   ```
