package edu.kit.datamanager.idoris.dao;

import edu.kit.datamanager.idoris.domain.entities.BasicDataType;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "basicDataTypes", path = "basicDataTypes")
public interface IBasicDataTypeDao extends IAbstractRepo<BasicDataType, String> {
}
