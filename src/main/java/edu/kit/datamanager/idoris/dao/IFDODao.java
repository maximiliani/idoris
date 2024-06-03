package edu.kit.datamanager.idoris.dao;

import edu.kit.datamanager.idoris.domain.entities.FDO;
import edu.kit.datamanager.idoris.domain.entities.TypeProfile;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "fdos", path = "fdos")
public interface IFDODao extends IAbstractRepo<FDO, String> {
    Iterable<FDO> findAllByTypeProfilesContains(TypeProfile typeProfile);
}
