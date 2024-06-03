package edu.kit.datamanager.idoris.dao;

import edu.kit.datamanager.idoris.domain.entities.OperationTypeProfile;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "operationTypeProfiles", path = "operationTypeProfiles")
public interface IOperationTypeProfileDao extends IAbstractRepo<OperationTypeProfile, String> {
}
