
package mn.edu.must.sqat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseRegistrationServiceTest {

    @Mock
    StudentRepository repository;

    @Mock
    EmailSender emailSender;

    CourseRegistrationService service;

    @BeforeEach
    void setUp() {
        service = new CourseRegistrationService(repository, emailSender);
    }

    @Test
    void activeStudent_validCourse_returnsOK() {

        // 1. Arrange — prepare test data
        Student student = new Student(
                "S001",
                "student@example.com",
                true,
                List.of("CS101")
        );

        // 2. Stub — simulate database responses
        when(repository.findStudent("S001")).thenReturn(student);
        when(repository.courseExists("CS201")).thenReturn(true);
        when(repository.prerequisites("CS201"))
                .thenReturn(List.of("CS101"));

        // 3. Act — call the method being tested
        String result = service.register("S001", "CS201");

        // 4. Assert — check the returned value
        assertEquals("OK", result);

        // 5. Verify — check interactions
        verify(repository).saveRegistration("S001", "CS201");
        verify(emailSender).send(
                eq("student@example.com"),
                anyString(),
                contains("CS201")
        );
    }
    
@Test
void nonexistentStudent_returnsErrorWithoutSavingOrEmail() {

    // Arrange: simulate a student who doesn't exist
    when(repository.findStudent("S999")).thenReturn(null);

    // Act
    String result = service.register("S999", "CS201");

    // Assert
    assertEquals("ERROR_NO_STUDENT", result);

    // Verify: registration must NOT be saved
    verify(repository, never())
            .saveRegistration(anyString(), anyString());

    // Verify: no email must be sent
    verify(emailSender, never())
            .send(anyString(), anyString(), anyString());
}

@Test
void inactiveStudent_returnsErrorWithoutEmailInteraction() {

    // Arrange: student exists but is inactive
    Student student = new Student(
            "S002",
            "inactive@example.com",
            false,
            List.of()
    );

    when(repository.findStudent("S002")).thenReturn(student);

    // Act
    String result = service.register("S002", "CS201");

    // Assert
    assertEquals("ERROR_INACTIVE_STUDENT", result);

    // Verify: registration was not saved
    verify(repository, never())
            .saveRegistration(anyString(), anyString());

    // Verify: email sender was never used
    verifyNoInteractions(emailSender);
}

@Test
void missingPrerequisites_returnsErrorWithoutSaving() {

    // Arrange: student has completed CS101, but not CS201
    Student student = new Student(
            "S003",
            "student3@example.com",
            true,
            List.of("CS101")
    );

    when(repository.findStudent("S003")).thenReturn(student);
    when(repository.courseExists("CS301")).thenReturn(true);
    when(repository.prerequisites("CS301"))
            .thenReturn(List.of("CS201"));

    // Act
    String result = service.register("S003", "CS301");

    // Assert
    assertEquals("ERROR_PREREQUISITES", result);

    // Verify: no registration was saved
    verify(repository, never())
            .saveRegistration(anyString(), anyString());

    // Verify: no email was sent
    verifyNoInteractions(emailSender);
}

@Test
void emailFailure_registrationStillSucceeds() {

    // Arrange: valid student and course
    Student student = new Student(
            "S004",
            "student4@example.com",
            true,
            List.of("CS101")
    );

    when(repository.findStudent("S004")).thenReturn(student);
    when(repository.courseExists("CS201")).thenReturn(true);
    when(repository.prerequisites("CS201"))
            .thenReturn(List.of("CS101"));

    // Simulate the email service crashing
    doThrow(new RuntimeException("Email server unavailable"))
            .when(emailSender)
            .send(anyString(), anyString(), anyString());

    // Act
    String result = service.register("S004", "CS201");

    // Assert: registration still succeeds
    assertEquals("OK", result);

    // Verify: registration was saved despite the email failure
    verify(repository).saveRegistration("S004", "CS201");

    // Verify: the service attempted to send the email
    verify(emailSender).send(
            eq("student4@example.com"),
            anyString(),
            contains("CS201")
    );
}

@Test
void nullInputs_throwIllegalArgumentException() {

    // Case 1: studentId is null
    assertThrows(
            IllegalArgumentException.class,
            () -> service.register(null, "CS201")
    );

    // Case 2: courseId is null
    assertThrows(
            IllegalArgumentException.class,
            () -> service.register("S001", null)
    );

    // Neither dependency should have been used
    verifyNoInteractions(repository, emailSender);
}

@Test
void successfulRegistration_emailContainsCourseCode() {

    // Arrange
    Student student = new Student(
            "S005",
            "student5@example.com",
            true,
            List.of("CS101")
    );

    when(repository.findStudent("S005")).thenReturn(student);
    when(repository.courseExists("CS201")).thenReturn(true);
    when(repository.prerequisites("CS201"))
            .thenReturn(List.of("CS101"));

    // Act
    String result = service.register("S005", "CS201");

    // Capture the email message
    ArgumentCaptor<String> messageCaptor =
            ArgumentCaptor.forClass(String.class);

    verify(emailSender).send(
            eq("student5@example.com"),
            anyString(),
            messageCaptor.capture()
    );

    // Assert
    assertEquals("OK", result);
    assertTrue(messageCaptor.getValue().contains("CS201"));
}

@ParameterizedTest
@CsvSource({
        "NO_STUDENT, ERROR_NO_STUDENT",
        "INACTIVE_STUDENT, ERROR_INACTIVE_STUDENT",
        "NO_COURSE, ERROR_NO_COURSE"
})
void invalidRegistration_returnsExpectedError(
        String scenario, String expectedResult) {

    Student activeStudent = new Student(
            "S001", "student@example.com", true, List.of()
    );

    Student inactiveStudent = new Student(
            "S001", "student@example.com", false, List.of()
    );

    // Arrange: configure the mock for each scenario
    switch (scenario) {
        case "NO_STUDENT":
            when(repository.findStudent("S001")).thenReturn(null);
            break;

        case "INACTIVE_STUDENT":
            when(repository.findStudent("S001"))
                    .thenReturn(inactiveStudent);
            break;

        case "NO_COURSE":
            when(repository.findStudent("S001"))
                    .thenReturn(activeStudent);
            when(repository.courseExists("CS999"))
                    .thenReturn(false);
            break;
    }

    // Act
    String result = service.register("S001", "CS999");

    // Assert
    assertEquals(expectedResult, result);

    // No failed registration should be saved
    verify(repository, never())
            .saveRegistration(anyString(), anyString());
}

}
