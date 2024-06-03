package edu.kit.datamanager.idoris.dao;

import edu.kit.datamanager.idoris.domain.entities.DataType;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "dataTypes", path = "dataTypes")
public interface IDataTypeDao extends IAbstractRepo<DataType, String> {
    Iterable<DataType> findAllByLicenseUrl(String licenseUrl);
}
