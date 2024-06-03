package edu.kit.datamanager.idoris.dao;

import edu.kit.datamanager.idoris.domain.entities.License;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "licenses", path = "licenses")
public interface ILicenseDao extends IAbstractRepo<License, String> {
    License findByName(String name);

    License findByUrl(String url);
}
