package de.frachtwerk.essencium.backend.test.integration.repository;

import de.frachtwerk.essencium.backend.repository.BaseUserRepository;
import de.frachtwerk.essencium.backend.test.integration.model.TestUser;
import org.springframework.stereotype.Repository;

@Repository
public interface TestBaseUserRepository extends BaseUserRepository<TestUser, Long> {}
