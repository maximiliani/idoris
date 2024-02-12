package edu.kit.datamanager.idoris.dao;

import edu.kit.datamanager.idoris.domain.License;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.CrudRepository;

public interface ILicenseDao extends Neo4jRepository<License, String>, CrudRepository<License, String> {
    License findByName(String name);

    License findByUrl(String url);
}
