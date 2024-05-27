package edu.kit.datamanager.idoris.dao;

import edu.kit.datamanager.idoris.domain.entities.Standard;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "standards", path = "standards")
public interface IStandardsDao extends Neo4jRepository<Standard, String>, CrudRepository<Standard, String>, PagingAndSortingRepository<Standard, String> {
}
