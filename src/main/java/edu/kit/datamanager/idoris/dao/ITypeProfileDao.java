package edu.kit.datamanager.idoris.dao;

import edu.kit.datamanager.idoris.domain.entities.TypeProfile;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "typeProfiles", path = "typeProfiles")
public interface ITypeProfileDao extends IAbstractRepo<TypeProfile, String> {
}
