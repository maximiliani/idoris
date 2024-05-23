package edu.kit.datamanager.idoris.dao;

import edu.kit.datamanager.idoris.domain.User;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.CrudRepository;

public interface IUserDao extends Neo4jRepository<User, String>, CrudRepository<User, String> {
    Iterable<User> findAllByName(String name);

    Iterable<User> findAllByEmail(String email);

    Iterable<User> findAllByUrl(String url);
}
