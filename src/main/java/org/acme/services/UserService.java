package org.acme.services;

import org.acme.domain.User;
import org.acme.repositories.UserRepository;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class UserService {

	@Inject
	PersistenceOrmAssistantService persistenceAssistantService;

	@Inject
	UserRepository repository;

	public Uni<User> createUser(User pUser) {

		return persistenceAssistantService.create(pUser, repository);
	}

	public Uni<Void> deleteUserById(Long pUserId) {
		return persistenceAssistantService.deleteById(pUserId, repository);
	}

	public Uni<User> findAndMapObjectOfEntityById(Long pId) {
		return persistenceAssistantService.findById(pId, repository);
	}
}
