package edu.kit.datamanager.idoris.dao;

import edu.kit.datamanager.idoris.domain.DataType;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.CrudRepository;

public interface IDataTypeDao extends Neo4jRepository<DataType, String>, CrudRepository<DataType, String> {
    Iterable<DataType> findAllByLicenseUrl(String licenseUrl);
}
