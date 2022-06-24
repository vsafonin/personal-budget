FROM openjdk:17-alpine3.14
RUN mkdir -p /var/app/conf
COPY src/main/resources/application.properties /var/app/conf/
ARG FILE_PROP=/var/app/conf/application.properties
ARG SSL_ENABLE=false
RUN if [ "$SSL_ENABLE" = "true" ] ; then echo -e "security.require-ssl = true\nserver.ssl.keyStoreType = PKCS12\nserver.ssl.keyAlias = tomcat" >> ${FILE_PROP} \ 
    && echo "server.ssl.key-store-password=\${SSL_PASSWORD}" >> ${FILE_PROP} \ 
    && echo "server.ssl.key-store=/var/app/ssl/keystore.p12" >> ${FILE_PROP} ; \ 
    else echo "only http" ; fi
COPY ssl/*.p12 /var/app/ssl/
ARG JAR_FILE=target/personalAccounter-0.0.1.jar
COPY ${JAR_FILE} /var/app/app.jar
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
ENV LANG ru_RU.UTF-8
ENV LANGUAGE ru_RU:ru
ENV LC_ALL ru_RU.UTF-8
EXPOSE 9443
ENTRYPOINT [ "java","-jar","/var/app/app.jar", "--spring.config.location=/var/app/conf/"]