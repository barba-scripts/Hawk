package com.fiap.hawk.config;

import com.fiap.hawk.domain.Role;
import com.fiap.hawk.domain.StrategyDocument;
import com.fiap.hawk.domain.UserDocument;
import com.fiap.hawk.repository.StrategyRepository;
import com.fiap.hawk.repository.UserRepository;
import com.fiap.hawk.security.JwtService;
import com.fiap.hawk.service.SequenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;

@Component
public class DataSeeder implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

	private final SeedProperties seedProperties;
	private final UserRepository userRepository;
	private final StrategyRepository strategyRepository;
	private final SequenceService sequenceService;
	private final JwtService jwtService;
	private final MongoTemplate mongoTemplate;
	private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

	public DataSeeder(SeedProperties seedProperties, UserRepository userRepository,
			StrategyRepository strategyRepository, SequenceService sequenceService, JwtService jwtService,
			MongoTemplate mongoTemplate,
			org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
		this.seedProperties = seedProperties;
		this.userRepository = userRepository;
		this.strategyRepository = strategyRepository;
		this.sequenceService = sequenceService;
		this.jwtService = jwtService;
		this.mongoTemplate = mongoTemplate;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(ApplicationArguments args) {
		jwtService.reloadKey();
		sequenceService.syncToMax("users", maxPublicId("users"));
		sequenceService.syncToMax("strategies", maxPublicId("strategies"));
		sequenceService.syncToMax("ideas", maxPublicId("ideas"));
		sequenceService.syncToMax("projects", maxPublicId("projects"));

		if (!seedProperties.isEnabled()) {
			return;
		}

		ensureUser("Carlos Mendes", "carlos.mendes@aguiabranca.com.br", "senha-segura", Role.OPERADOR, "Logística");
		ensureUser("Ana Gestora", "ana.gestora@aguiabranca.com.br", "senha-segura", Role.GESTOR, "Logística");
		ensureUser("Bruno Líder", "bruno.lider@aguiabranca.com.br", "senha-segura", Role.LIDER, "Logística");

		if (strategyRepository.count() == 0) {
			UserDocument lider = userRepository.findByEmailIgnoreCase("bruno.lider@aguiabranca.com.br").orElseThrow();
			Instant now = Instant.now();
			StrategyDocument strategy = new StrategyDocument();
			strategy.setPublicId(sequenceService.next("strategies"));
			strategy.setTitle("Redução de Custos Operacionais Q3");
			strategy.setCategory("Logística");
			strategy.setBody("Foco em otimizar rotas e reduzir o consumo de combustível.");
			strategy.setActive(true);
			strategy.setCreatedByUserId(lider.getPublicId());
			strategy.setDate(LocalDate.now());
			strategy.setCreatedAt(now);
			strategy.setUpdatedAt(now);
			strategyRepository.save(strategy);
			log.info("Seed: orientação estratégica inicial criada");
		}

		log.info("Seed concluído. Logins demo: carlos.mendes@ / ana.gestora@ / bruno.lider@ (senha-segura)");
	}

	private int maxPublicId(String collection) {
		Query query = new Query();
		query.fields().include("publicId");
		return mongoTemplate.find(query, org.bson.Document.class, collection).stream()
				.map(doc -> doc.get("publicId"))
				.filter(v -> v instanceof Number)
				.mapToInt(v -> ((Number) v).intValue())
				.max()
				.orElse(0);
	}

	private void ensureUser(String name, String email, String password, Role role, String division) {
		userRepository.findByEmailIgnoreCase(email).ifPresentOrElse(existing -> {
			boolean dirty = false;
			if (existing.getRole() != role) {
				existing.setRole(role);
				dirty = true;
			}
			if (!passwordEncoder.matches(password, existing.getPasswordHash())) {
				existing.setPasswordHash(passwordEncoder.encode(password));
				dirty = true;
			}
			if (dirty) {
				existing.setUpdatedAt(Instant.now());
				userRepository.save(existing);
				log.info("Seed: usuário {} atualizado", email);
			}
		}, () -> {
			Instant now = Instant.now();
			UserDocument user = new UserDocument();
			user.setPublicId(sequenceService.next("users"));
			user.setName(name);
			user.setEmail(email.toLowerCase());
			user.setPasswordHash(passwordEncoder.encode(password));
			user.setRole(role);
			user.setDivision(division);
			user.setActive(true);
			user.setCreatedAt(now);
			user.setUpdatedAt(now);
			userRepository.save(user);
			log.info("Seed: usuário {} ({}) criado", email, role.getValue());
		});
	}
}
