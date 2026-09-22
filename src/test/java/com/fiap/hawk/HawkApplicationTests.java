package com.fiap.hawk;

import com.fiap.hawk.config.DataSeeder;
import com.fiap.hawk.security.JwtService;
import com.mongodb.client.MongoClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
		"spring.mongodb.uri=mongodb://localhost:27017/hawk_test",
		"hawk.seed.enabled=false",
		"hawk.gemini.api-key="
})
class HawkApplicationTests {

	@MockitoBean
	private MongoClient mongoClient;
	@MockitoBean
	private JwtService jwtService;
	@MockitoBean
	private DataSeeder dataSeeder;

	@Test
	void contextLoadsWithoutGeminiKeyOrExternalConnections() {
	}
}
