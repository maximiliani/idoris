package edu.kit.datamanager.idoris.dao;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

@NoRepositoryBean
public interface IAbstractRepo<T, ID> extends Neo4jRepository<T, ID>, ListCrudRepository<T, ID>, PagingAndSortingRepository<T, ID> {
}
