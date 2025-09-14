package org.acme.services;

import java.io.Serializable;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jboss.logging.Logger;

import io.smallrye.mutiny.Uni;
import jakarta.data.exceptions.EntityExistsException;
import jakarta.data.exceptions.OptimisticLockingFailureException;
import jakarta.data.repository.CrudRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class PersistenceOrmAssistantService {

	private final Executor executor;

	private Logger logger;

	@Inject
	public PersistenceOrmAssistantService(Logger logger,
			@Named("virtualThreadExecutor") Instance<Executor> pExecutorInstance) {
		this.logger = logger;
		this.executor = pExecutorInstance.get();
	}

	public <K extends Serializable, E, R extends CrudRepository<E, K>> Uni<E> create(final E pEntityCreation,
			R pRepository) {

		return runOnWorkThread(() -> {

			if (logger.isDebugEnabled()) {
				logger.debugf("Creating %s", pRepository.getClass().getName());
			}

			try {
				return pRepository.insert(pEntityCreation);
			} catch (EntityExistsException e) {
				throw e;
			}
		});
	}

	public <K extends Serializable, E, R extends CrudRepository<E, K>> Uni<Void> deleteById(K pKey, R pRepository) {

		return runOnWorkThread(() -> {

			try {

				if (logger.isDebugEnabled()) {
					logger.debugf("Deleting from %s", pRepository.getClass().getName());
				}

				pRepository.deleteById(pKey);
				return null;
			} catch (OptimisticLockingFailureException e) {
				throw e;
			}

		}).onFailure().invoke(e -> logger.error("DB error", e)).replaceWithVoid().replaceWithVoid();

	}

	public <E, K extends Serializable, R extends CrudRepository<E, K>> Uni<E> findById(K pKey, R pRepository) {

		return runOnWorkThread(() -> {

			if (logger.isDebugEnabled()) {
				logger.debugf("Finding %s", pRepository.getClass().getName());
			}

			return pRepository.findById(pKey).get();
		});
	}

	public <E> Uni<E> runOnWorkThread(Supplier<E> pWorkSupplier) {
		return Uni.createFrom().item(() -> withTransaction(pWorkSupplier)).runSubscriptionOn(executor);

	}

	public <R, E> Uni<E> runOnWorkThread(Supplier<R> pEventLoopSupplier, Function<R, E> pWorkFunction) {
		return Uni.createFrom().item(pEventLoopSupplier) // event-loop supplier
				.emitOn(executor) // switch to worker thread
				.map(r -> withTransaction(() -> pWorkFunction.apply(r)));
	}

	@Transactional
	protected <E> E withTransaction(Supplier<E> pWorkSupplier) {
		return pWorkSupplier.get();
	}
}
