package edu.kit.datamanager.idoris.dao;

import edu.kit.datamanager.idoris.domain.FDO;
import edu.kit.datamanager.idoris.domain.TypeProfile;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "fdos", path = "fdos")
public interface IFDODao extends Neo4jRepository<FDO, String>, CrudRepository<FDO, String> {
    Iterable<FDO> findAllByTypeProfilesContains(TypeProfile typeProfile);
}
