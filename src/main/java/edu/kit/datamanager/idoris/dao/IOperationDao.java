package edu.kit.datamanager.idoris.dao;

import edu.kit.datamanager.idoris.domain.entities.Operation;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "operations", path = "operations")
public interface IOperationDao extends IAbstractRepo<Operation, String> {
}
