package com.example.demo.service;

import com.example.demo.model.CsvFile;
import com.example.demo.model.User;
import com.example.demo.repository.CsvFileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.mock.web.MockMultipartFile;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
@Tag("integration")
@Disabled("Requires running Docker environment")
class UserServiceMongoIntegrationTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");

    @Autowired
    private UserService userService;

    @Autowired
    private CsvFileRepository csvFileRepository;

    private static final String FILENAME = "integration-test23.csv";

    @BeforeEach
    void cleanDb() {
        csvFileRepository.deleteAll();
    }

    @Test
    @DisplayName("Full Cycle: Upload -> Create -> Read -> Update -> Delete -> Verification")
    void testFullLifecycle() {
        // 1. Upload File
        String initialContent = "id=1,email=initial@test.com,name=Initial User";
        MockMultipartFile file = new MockMultipartFile("file", FILENAME, "text/csv", initialContent.getBytes());
        userService.getAllUsers(file);

        Optional<CsvFile> savedFile = csvFileRepository.findByFilename(FILENAME);
        assertThat(savedFile).isPresent();
        assertThat(savedFile.get().getUsers()).hasSize(1);
        assertThat(savedFile.get().getUsers().get(0).getName()).isEqualTo("Initial User");

        // 2. Create New User
        userService.createUser(FILENAME, 3, "second12@test.com", "Second User");

        savedFile = csvFileRepository.findByFilename(FILENAME);
        assertThat(savedFile.get().getUsers()).hasSize(2);

        // 3. Update User
        userService.updateUser(FILENAME, 1, "updated@test.com", "Updated Name");

        savedFile = csvFileRepository.findByFilename(FILENAME);
        User user1 = savedFile.get().getUsers().stream().filter(u -> u.getId() == 1).findFirst().orElseThrow();
        assertThat(user1.getEmail()).isEqualTo("updated@test.com");
        assertThat(user1.getName()).isEqualTo("Updated Name");

        // 4. Delete User
        userService.deleteUser(FILENAME, 3);

        savedFile = csvFileRepository.findByFilename(FILENAME);
        assertThat(savedFile.get().getUsers()).hasSize(1);
        assertThat(savedFile.get().getUsers().get(0).getId()).isEqualTo(1);

        // 5. Verify CSV Content String was updated (Persistence check)
        String csvContent = savedFile.get().getCsvContent();
        assertThat(csvContent).contains("name=Updated Name");
        assertThat(csvContent).doesNotContain("name=Second User");
    }

    @Test
    @DisplayName("Should handle missing file gracefully in DB lookup")
    void testMissingFile() {
        assertThrows(RuntimeException.class, () -> userService.createUser("non-existent.csv", 1, "a@a.com", "A"));
    }
}
