package edu.kit.datamanager.idoris.dao;

import edu.kit.datamanager.idoris.domain.OperationTypeProfile;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.CrudRepository;

public interface IOperationTypeProfileDao extends Neo4jRepository<OperationTypeProfile, String>, CrudRepository<OperationTypeProfile, String> {
}
