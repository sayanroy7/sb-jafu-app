package org.springframework.boot.autoconfigure.data.mongo;

import com.mongodb.MongoClient;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoDbFactorySupport;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

/**
 * @author SAROY on 1/28/2020
 */
public class MongoDataAutoInitializer implements ApplicationContextInitializer<GenericApplicationContext> {

    private MongoProperties properties;

    public MongoDataAutoInitializer(MongoProperties properties) {
        this.properties = properties;
    }

    @Override
    public void initialize(GenericApplicationContext applicationContext) {
        MongoDbFactoryConfiguration factoryConfiguration = new MongoDbFactoryConfiguration();
        MongoDbFactoryDependentConfiguration dependentConfiguration = new MongoDbFactoryDependentConfiguration(properties);
        /*factoryConfiguration.mongoDbFactory(applicationContext.getBeanProvider(MongoClient.class)
                , applicationContext.getBeanProvider(com.mongodb.client.MongoClient.class)
                , properties);*/


        /*MongoDbFactorySupport mongoDbFactory = mongoDbFactory(applicationContext.getBeanProvider(MongoClient.class)
                , applicationContext.getBeanProvider(com.mongodb.client.MongoClient.class)
                , properties);*/
        applicationContext.registerBean(MappingMongoConverter.class, () -> dependentConfiguration.mappingMongoConverter(applicationContext.getBean(MongoDbFactorySupport.class)
                , applicationContext.getBean(MongoMappingContext.class)
                , applicationContext.getBean(MongoCustomConversions.class)));
        applicationContext.registerBean(MongoDbFactorySupport.class, () -> factoryConfiguration.mongoDbFactory(applicationContext.getBeanProvider(MongoClient.class)
                , applicationContext.getBeanProvider(com.mongodb.client.MongoClient.class)
                , properties));
        applicationContext.registerBean(MongoTemplate.class, () -> dependentConfiguration.mongoTemplate(applicationContext.getBean(MongoDbFactory.class)
                , applicationContext.getBean(MongoConverter.class)));
        /*MappingMongoConverter converter = mappingMongoConverter(applicationContext.getBean(MongoDbFactorySupport.class)
                , applicationContext.getBean(MongoMappingContext.class)
                , applicationContext.getBean(MongoCustomConversions.class));*/
        //applicationContext.registerBean(MappingMongoConverter.class, () -> converter);
        /*MongoTemplate mongoTemplate =  new MongoTemplate(mongoDbFactory, converter);
        applicationContext.registerBean(MongoTemplate.class, () -> mongoTemplate);*/

    }
}
