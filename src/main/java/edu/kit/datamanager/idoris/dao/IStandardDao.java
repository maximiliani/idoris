package edu.kit.datamanager.idoris.dao;

import edu.kit.datamanager.idoris.domain.Standard;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.CrudRepository;

public interface IStandardDao extends Neo4jRepository<Standard, String>, CrudRepository<Standard, String> {
    Iterable<Standard> findAllByIssuer(Standard.Issuer issuer);

    Iterable<Standard> findAllByName(String name);
}
