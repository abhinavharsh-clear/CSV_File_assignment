package com.example.demo.service;

import com.example.demo.model.CsvFile;
import com.example.demo.model.User;
import com.example.demo.repository.CsvFileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceComprehensiveTest {

    @Mock
    private CsvFileRepository csvFileRepository;

    @InjectMocks
    private UserService userService;

    private static final String FILENAME = "users.csv";
    private CsvFile existingCsvFile;

    @BeforeEach
    void setUp() {
        List<User> users = new ArrayList<>();
        users.add(new User(1, "alice@example.com", "Alice"));
        users.add(new User(2, "bob@example.com", "Bob"));

        existingCsvFile = new CsvFile(FILENAME, users,
                "id=1,email=alice@example.com,name=Alice\nid=2,email=bob@example.com,name=Bob");
        existingCsvFile.setId("mongo-id-123");
    }

    @Nested
    @DisplayName("parseCSVFile & getAllUsers Tests")
    class ParseAndReadTests {

        @Test
        @DisplayName("Should successfully parse valid CSV file and save if new")
        void testGetAllUsers_NewFile() {
            String csvContent = "id=1,email=test@a.com,name=A\nid=2,email=test@b.com,name=B";
            MockMultipartFile file = new MockMultipartFile("file", FILENAME, "text/csv", csvContent.getBytes());

            when(csvFileRepository.findByFilename(FILENAME)).thenReturn(Optional.empty());
            when(csvFileRepository.save(any(CsvFile.class))).thenAnswer(invocation -> {
                CsvFile saved = invocation.getArgument(0);
                saved.setId("new-mongo-id");
                return saved;
            });

            Map<String, Object> result = userService.getAllUsers(file);

            assertThat(result)
                    .containsEntry("filename", FILENAME)
                    .containsEntry("fileId", "new-mongo-id")
                    .containsKey("users");

            @SuppressWarnings("unchecked")
            List<User> users = (List<User>) result.get("users");
            assertThat(users).hasSize(2)
                    .extracting(User::getEmail)
                    .containsExactly("test@a.com", "test@b.com");

            verify(csvFileRepository).findByFilename(FILENAME);
            verify(csvFileRepository).save(any(CsvFile.class));
        }

        @Test
        @DisplayName("Should update existing file content if file already exists")
        void testGetAllUsers_ExistingFile() {
            String newContent = "id=3,email=charlie@c.com,name=Charlie"; // Different content
            MockMultipartFile file = new MockMultipartFile("file", FILENAME, "text/csv", newContent.getBytes());

            when(csvFileRepository.findByFilename(FILENAME)).thenReturn(Optional.of(existingCsvFile));
            when(csvFileRepository.save(any(CsvFile.class))).thenReturn(existingCsvFile);

            Map<String, Object> result = userService.getAllUsers(file);

            @SuppressWarnings("unchecked")
            List<User> users = (List<User>) result.get("users");
            assertThat(users).hasSize(1);
            assertThat(users.get(0).getName()).isEqualTo("Charlie");

            // Verify the existing object was updated
            assertThat(existingCsvFile.getUsers()).hasSize(1);
            assertThat(existingCsvFile.getCsvContent()).isEqualTo(newContent);
            verify(csvFileRepository).save(existingCsvFile);
        }

        @Test
        @DisplayName("Should ignore empty lines in CSV")
        void testParseCSVFile_EmptyLines() {
            String csvContent = "id=1,email=a@a.com,name=A\n\n   \nid=2,email=b@b.com,name=B\n";
            MockMultipartFile file = new MockMultipartFile("file", FILENAME, "text/csv", csvContent.getBytes());

            List<User> users = userService.parseCSVFile(file);

            assertThat(users).hasSize(2);
        }

        @Test
        @DisplayName("Should throw RuntimeException for malformed CSV lines")
        void testParseCSVFile_Malformed() {
            String csvContent = "id=1,email=a@a.com,name=A\nMALFORMED_LINE_HERE";
            MockMultipartFile file = new MockMultipartFile("file", FILENAME, "text/csv", csvContent.getBytes());

            assertThatThrownBy(() -> userService.parseCSVFile(file))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid CSV format");
        }
    }

    @Nested
    @DisplayName("createUser Tests")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user successfully when ID is unique")
        void testCreateUser_Success() {
            when(csvFileRepository.findByFilename(FILENAME)).thenReturn(Optional.of(existingCsvFile));

            String result = userService.createUser(FILENAME, 3, "charlie@example.com", "Charlie");

            assertThat(result).contains("User created successfully");
            assertThat(existingCsvFile.getUsers()).hasSize(3);
            assertThat(existingCsvFile.getUsers().get(2).getName()).isEqualTo("Charlie");

            // Verify content string update
            assertThat(existingCsvFile.getCsvContent()).contains("name=Charlie");
            verify(csvFileRepository).save(existingCsvFile);
        }

        @Test
        @DisplayName("Should throw exception when creating user with existing ID")
        void testCreateUser_DuplicateId() {
            when(csvFileRepository.findByFilename(FILENAME)).thenReturn(Optional.of(existingCsvFile));

            assertThatThrownBy(() -> userService.createUser(FILENAME, 1, "new@mail.com", "New"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("User with ID 1 already exists");

            verify(csvFileRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when file not found")
        void testCreateUser_FileNotFound() {
            when(csvFileRepository.findByFilename("missing.csv")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.createUser("missing.csv", 3, "c@c.com", "C"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("File not found");
        }
    }

    @Nested
    @DisplayName("updateUser Tests (PUT)")
    class UpdateUserTests {

        @Test
        @DisplayName("Should fully update existing user")
        void testUpdateUser_Success() {
            when(csvFileRepository.findByFilename(FILENAME)).thenReturn(Optional.of(existingCsvFile));

            String result = userService.updateUser(FILENAME, 1, "alice_new@example.com", "Alice New");

            assertThat(result).contains("User with ID 1 updated successfully");
            User updatedUser = existingCsvFile.getUsers().stream().filter(u -> u.getId() == 1).findFirst()
                    .orElseThrow();
            assertThat(updatedUser.getEmail()).isEqualTo("alice_new@example.com");
            assertThat(updatedUser.getName()).isEqualTo("Alice New");

            verify(csvFileRepository).save(existingCsvFile);
        }

        @Test
        @DisplayName("Should throw exception if user ID does not exist")
        void testUpdateUser_UserNotFound() {
            when(csvFileRepository.findByFilename(FILENAME)).thenReturn(Optional.of(existingCsvFile));

            assertThatThrownBy(() -> userService.updateUser(FILENAME, 99, "x@x.com", "X"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("User with ID 99 not found");
        }
    }

    @Nested
    @DisplayName("patchUser Tests (PATCH)")
    class PatchUserTests {

        @Test
        @DisplayName("Should update only email")
        void testPatchUser_EmailOnly() {
            when(csvFileRepository.findByFilename(FILENAME)).thenReturn(Optional.of(existingCsvFile));

            userService.patchUser(FILENAME, 1, "patched@example.com", null);

            User user = existingCsvFile.getUsers().get(0);
            assertThat(user.getEmail()).isEqualTo("patched@example.com");
            assertThat(user.getName()).isEqualTo("Alice"); // Unchanged
        }

        @Test
        @DisplayName("Should update only name")
        void testPatchUser_NameOnly() {
            when(csvFileRepository.findByFilename(FILENAME)).thenReturn(Optional.of(existingCsvFile));

            userService.patchUser(FILENAME, 1, null, "Alice Patched");

            User user = existingCsvFile.getUsers().get(0);
            assertThat(user.getEmail()).isEqualTo("alice@example.com"); // Unchanged
            assertThat(user.getName()).isEqualTo("Alice Patched");
        }

        @Test
        @DisplayName("Should do nothing if fields are null/empty")
        void testPatchUser_NoOp() {
            when(csvFileRepository.findByFilename(FILENAME)).thenReturn(Optional.of(existingCsvFile));

            userService.patchUser(FILENAME, 1, "", null);

            User user = existingCsvFile.getUsers().get(0);
            assertThat(user.getEmail()).isEqualTo("alice@example.com");
            assertThat(user.getName()).isEqualTo("Alice");

            // Still saves because logic doesn't strictly prevent save if no dirty check
            verify(csvFileRepository).save(existingCsvFile);
        }
    }

    @Nested
    @DisplayName("deleteUser Tests")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user successfully")
        void testDeleteUser_Success() {
            when(csvFileRepository.findByFilename(FILENAME)).thenReturn(Optional.of(existingCsvFile));

            String result = userService.deleteUser(FILENAME, 1);

            assertThat(result).contains("deleted successfully");
            assertThat(existingCsvFile.getUsers()).hasSize(1);
            assertThat(existingCsvFile.getUsers().get(0).getId()).isEqualTo(2);

            verify(csvFileRepository).save(existingCsvFile);
        }

        @Test
        @DisplayName("Should throw exception if trying to delete non-existent user")
        void testDeleteUser_UserNotFound() {
            when(csvFileRepository.findByFilename(FILENAME)).thenReturn(Optional.of(existingCsvFile));

            assertThatThrownBy(() -> userService.deleteUser(FILENAME, 99))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("User with ID 99 not found");
        }
    }

    @Nested
    @DisplayName("getFileInfo Tests")
    class GetFileInfoTests {

        @Test
        @DisplayName("Should return correct file metadata")
        void testGetFileInfo_Success() {
            when(csvFileRepository.findByFilename(FILENAME)).thenReturn(Optional.of(existingCsvFile));

            Map<String, Object> info = userService.getFileInfo(FILENAME);

            assertThat(info)
                    .containsEntry("filename", FILENAME)
                    .containsEntry("userCount", 2)
                    .containsKey("uploadedAt");
        }

        @Test
        @DisplayName("Should throw exception if file not found")
        void testGetFileInfo_NotFound() {
            when(csvFileRepository.findByFilename("missing.csv")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getFileInfo("missing.csv"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("File not found");
        }
    }
}
