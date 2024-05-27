package edu.kit.datamanager.idoris.dao;

import edu.kit.datamanager.idoris.domain.entities.License;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "licenses", path = "licenses")
public interface ILicenseDao extends Neo4jRepository<License, String>, CrudRepository<License, String>, PagingAndSortingRepository<License, String> {
    License findByName(String name);

    License findByUrl(String url);
}
