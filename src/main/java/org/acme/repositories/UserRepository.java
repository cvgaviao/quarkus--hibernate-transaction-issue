package org.acme.repositories;

import org.acme.domain.User;

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

}
