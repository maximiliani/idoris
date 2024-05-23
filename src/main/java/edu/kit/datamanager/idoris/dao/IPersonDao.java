package edu.kit.datamanager.idoris.dao;

import edu.kit.datamanager.idoris.domain.Person;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.CrudRepository;

public interface IPersonDao extends Neo4jRepository<Person, String>, CrudRepository<Person, String> {
    Iterable<Person> findAllByName(String name);

    Iterable<Person> findAllByEmail(String email);

    Iterable<Person> findAllByUrl(String url);
}
