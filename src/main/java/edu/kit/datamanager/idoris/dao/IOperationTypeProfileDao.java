package edu.kit.datamanager.idoris.dao;

import edu.kit.datamanager.idoris.domain.entities.OperationTypeProfile;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "operationTypeProfiles", path = "operationTypeProfiles")
public interface IOperationTypeProfileDao extends Neo4jRepository<OperationTypeProfile, String>, CrudRepository<OperationTypeProfile, String> {
}
