package edu.kit.datamanager.idoris.dao;

import edu.kit.datamanager.idoris.domain.entities.User;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "users", path = "users")
public interface IUserDao extends IAbstractRepo<User, String> {
    @Query("MATCH (u:ORCiDUser) RETURN u")
    Iterable<User> findAllORCiDUsers();

    @Query("MATCH (u:ORCiDUser) WHERE u.orcid = $orcid RETURN u")
    User findORCiDUserByORCiD(String orcid);

    @Query("MATCH (u:TextUser) RETURN u")
    Iterable<User> findAllTextUsers();

    @Query("MATCH (u:TextUser) WHERE u.email = $email RETURN u")
    User findTextUserByEmail(String email);
}
