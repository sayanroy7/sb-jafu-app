package org.springframework.boot.autoconfigure.data.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

/**
 * @author SAROY on 1/28/2020
 */
public class MongoAutoInitializer implements ApplicationContextInitializer<GenericApplicationContext> {

    private MongoProperties mongoProperties;

    public MongoAutoInitializer(MongoProperties properties) {
        this.mongoProperties = properties;
    }

    @Override
    public void initialize(GenericApplicationContext context) {
        MongoAutoConfiguration mongoAutoConfiguration = new MongoAutoConfiguration();
        context.registerBean(MongoClient.class, () -> mongoAutoConfiguration.mongo(mongoProperties,context.getBeanProvider(MongoClientOptions.class), context.getEnvironment()));


    }
}
