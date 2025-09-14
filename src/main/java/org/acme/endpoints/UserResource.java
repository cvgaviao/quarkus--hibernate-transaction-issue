package org.acme.endpoints;

import org.acme.domain.User;
import org.acme.services.UserService;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.Status;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transactional
public class UserResource {

	@Inject
	UserService userService;

	@POST
	public Uni<RestResponse<User>> create(User pUser) {
		return userService.createUser(pUser).onItem()
				.transform(created -> RestResponse.status(Status.CREATED, created));
	}

	@DELETE
	@Path("{userId}")
	public Uni<RestResponse<Void>> deleteById(@RestPath("userId") Long pUserId) {
		return userService.deleteUserById(pUserId).onItem().transform(r -> RestResponse.noContent());
	}

	@GET
	@Path("{userId}")
	public Uni<RestResponse<User>> findById(@RestPath("userId") Long pUserId) {
		return userService.findAndMapObjectOfEntityById(pUserId).onItem().transform(RestResponse::ok);

	}
}
