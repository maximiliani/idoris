package edu.kit.datamanager.idoris.dao;

import edu.kit.datamanager.idoris.domain.entities.Attribute;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "attributes", path = "attributes")
public interface IAttributeDao extends IAbstractRepo<Attribute, String> {
}
