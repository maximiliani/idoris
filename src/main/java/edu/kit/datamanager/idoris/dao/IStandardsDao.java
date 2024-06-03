package edu.kit.datamanager.idoris.dao;

import edu.kit.datamanager.idoris.domain.entities.Standard;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "standards", path = "standards")
public interface IStandardsDao extends IAbstractRepo<Standard, String> {
}
