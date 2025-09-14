package org.acme;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.acme.domain.User;
import org.acme.endpoints.UserResource;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
@TestHTTPEndpoint(UserResource.class)
class UserResourceTest {

	private Long createdId;

	@Inject
	@ConfigProperty(name = "quarkus.profile")
	String quarkusProfile;

	@BeforeEach
	void beforeEach(TestInfo testInfo) {
		System.out.println("Starting: " + testInfo.getDisplayName());
		System.out.println("-".repeat(50));

		System.out.println("Active Quarkus Profile: " + quarkusProfile);

		User userCreation = new User("John", "Boy", "john.boy@gmail.com");

		createdId = Long.parseLong(given().body(userCreation).contentType("application/json").when().post().then()
				.statusCode(201)
				.body("name", equalTo("John"), "surname", equalTo("Boy"), "email", equalTo("john.boy@gmail.com"))
				.extract().path("id").toString()); // Extract the object ID from the response

		assertThat(createdId).isNotNull();
	}

	@AfterEach
	void cleanup() {
		// Clean up the User created in setup
		if (createdId != null) {
			given().pathParams("userId", createdId).log().all().when().delete("/{userId}").then().log().all()
					.statusCode(204);
		}
	}

	@Test
	void testFindById() {

		given().get("/{userId}", createdId).then().assertThat().statusCode(200).body("name", equalTo("John"), "surname",
				equalTo("Boy"), "email", equalTo("john.boy@gmail.com"));
	}

}