package com.fiap.hawk;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
class HawkApplicationTests {

	@DynamicPropertySource
	static void mongoProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.mongodb.uri",
				() -> System.getenv().getOrDefault("MONGODB_URI",
						"mongodb+srv://hawk_secret_user:Hawk3000@cluster0.4dh4jnp.mongodb.net/hawk_test?retryWrites=true&w=majority"));
		registry.add("hawk.seed.enabled", () -> "false");
	}

	@Test
	void contextLoads() {
	}
}
