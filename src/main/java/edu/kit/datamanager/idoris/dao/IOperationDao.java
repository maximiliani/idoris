package edu.kit.datamanager.idoris.dao;

import edu.kit.datamanager.idoris.domain.entities.Operation;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "operations", path = "operations")
public interface IOperationDao extends Neo4jRepository<Operation, String>, CrudRepository<Operation, String> {
}
