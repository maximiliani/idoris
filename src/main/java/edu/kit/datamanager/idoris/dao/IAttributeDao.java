package edu.kit.datamanager.idoris.dao;

import edu.kit.datamanager.idoris.domain.entities.Attribute;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

@RepositoryRestResource(collectionResourceRel = "attributes", path = "attributes")
public interface IAttributeDao extends IAbstractRepo<Attribute, String> {
    @Query("MATCH (n:Attribute)" +
            " WHERE size([(n)-[:dataType]->() | 1]) = 1 AND NOT (n)<-[]-()" +
            " WITH n" +
            " MATCH (x)-[r]->()" +
            " WHERE type(r) <> \"dataType\"" +
            " WITH collect(DISTINCT x) as otherNodes, n" +
            " WHERE NOT n IN otherNodes" +
            " DETACH DELETE n")
    @RestResource(exported = false)
    void deleteOrphanedAttributes();
}
