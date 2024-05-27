package edu.kit.datamanager.idoris.dao;

import edu.kit.datamanager.idoris.domain.TypeProfile;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "typeProfiles", path = "typeProfiles")
public interface ITypeProfileDao extends Neo4jRepository<TypeProfile, String>, CrudRepository<TypeProfile, String> {
}
