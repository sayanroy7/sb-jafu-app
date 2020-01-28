package sb.jafu.app.repository;

import org.springframework.data.mongodb.core.MongoOperations;
import sb.jafu.app.model.User;

import java.util.List;

/**
 * @author SAROY on 1/27/2020
 */
public class UserRepository {

    private MongoOperations mongo;

    public UserRepository(MongoOperations mongo) {
        this.mongo = mongo;
    }

    public List<User> findAll() {
        return mongo.findAll(User.class, "user");
    }

    public User findOne(String id) {
        return mongo.findById(id, User.class, "user");
    }

    public User save(User user) {
        return mongo.save(user, "user");
    }
}
