package edu.kit.datamanager.idoris.dao;

import edu.kit.datamanager.idoris.domain.entities.DataType;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "dataTypes", path = "dataTypes")
public interface IDataTypeDao extends Neo4jRepository<DataType, String>, CrudRepository<DataType, String> {
    Iterable<DataType> findAllByLicenseUrl(String licenseUrl);
}
