package edu.kit.datamanager.idoris.dao;

import edu.kit.datamanager.idoris.domain.Operation;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.CrudRepository;

public interface IOperationDao extends Neo4jRepository<Operation, String>, CrudRepository<Operation, String> {
}
