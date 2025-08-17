package com.javajedis.legalconnect.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

/**
 * Comprehensive unit tests for ApplicationContextProvider.
 * Tests application context management, static methods, and proper context setting.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ApplicationContextProvider Tests")
class ApplicationContextProviderTest {

    private ApplicationContextProvider contextProvider;
    private ApplicationContext mockApplicationContext;

    @BeforeEach
    void setUp() {
        contextProvider = new ApplicationContextProvider();
        mockApplicationContext = mock(ApplicationContext.class);
        
        // Reset static context before each test
        ApplicationContextProvider.setStaticApplicationContext(null);
    }

    // APPLICATION CONTEXT AWARE TESTS

    @Test
    @DisplayName("Should set application context through setApplicationContext method")
    void testSetApplicationContext() {
        // Act
        contextProvider.setApplicationContext(mockApplicationContext);

        // Assert
        assertEquals(mockApplicationContext, ApplicationContextProvider.getApplicationContext());
    }

    @Test
    @DisplayName("Should handle null application context in setApplicationContext")
    void testSetApplicationContextWithNull() {
        // Act
        contextProvider.setApplicationContext(null);

        // Assert
        assertNull(ApplicationContextProvider.getApplicationContext());
    }

    // STATIC METHODS TESTS

    @Test
    @DisplayName("Should set static application context correctly")
    void testSetStaticApplicationContext() {
        // Act
        ApplicationContextProvider.setStaticApplicationContext(mockApplicationContext);

        // Assert
        assertEquals(mockApplicationContext, ApplicationContextProvider.getApplicationContext());
    }

    @Test
    @DisplayName("Should get static application context correctly")
    void testGetStaticApplicationContext() {
        // Arrange
        ApplicationContextProvider.setStaticApplicationContext(mockApplicationContext);

        // Act
        ApplicationContext retrievedContext = ApplicationContextProvider.getApplicationContext();

        // Assert
        assertSame(mockApplicationContext, retrievedContext);
    }

    @Test
    @DisplayName("Should return null when no application context is set")
    void testGetApplicationContextWhenNotSet() {
        // Act
        ApplicationContext retrievedContext = ApplicationContextProvider.getApplicationContext();

        // Assert
        assertNull(retrievedContext);
    }

    @Test
    @DisplayName("Should handle setting null static application context")
    void testSetStaticApplicationContextWithNull() {
        // Arrange - First set a context
        ApplicationContextProvider.setStaticApplicationContext(mockApplicationContext);
        
        // Act - Then set it to null
        ApplicationContextProvider.setStaticApplicationContext(null);

        // Assert
        assertNull(ApplicationContextProvider.getApplicationContext());
    }

    // CONTEXT REPLACEMENT TESTS

    @Test
    @DisplayName("Should replace existing application context")
    void testReplaceApplicationContext() {
        // Arrange
        ApplicationContext firstContext = mock(ApplicationContext.class);
        ApplicationContext secondContext = mock(ApplicationContext.class);
        
        ApplicationContextProvider.setStaticApplicationContext(firstContext);

        // Act
        ApplicationContextProvider.setStaticApplicationContext(secondContext);

        // Assert
        assertEquals(secondContext, ApplicationContextProvider.getApplicationContext());
    }

    @Test
    @DisplayName("Should replace context through setApplicationContext method")
    void testReplaceContextThroughSetApplicationContext() {
        // Arrange
        ApplicationContext firstContext = mock(ApplicationContext.class);
        ApplicationContextProvider.setStaticApplicationContext(firstContext);

        // Act
        contextProvider.setApplicationContext(mockApplicationContext);

        // Assert
        assertEquals(mockApplicationContext, ApplicationContextProvider.getApplicationContext());
    }

    // MULTIPLE INSTANCE TESTS

    @Test
    @DisplayName("Should share same static context across multiple instances")
    void testMultipleInstancesShareSameContext() {
        // Arrange
        ApplicationContextProvider firstProvider = new ApplicationContextProvider();
        new ApplicationContextProvider(); // Second provider not used, just created to test independence

        // Act
        firstProvider.setApplicationContext(mockApplicationContext);

        // Assert
        assertEquals(mockApplicationContext, ApplicationContextProvider.getApplicationContext());
        
        // Act - Get context through static access after setting via instance
        ApplicationContext retrievedFromStatic = ApplicationContextProvider.getApplicationContext();
        
        // Assert
        assertSame(mockApplicationContext, retrievedFromStatic);
    }

    @Test
    @DisplayName("Should maintain context consistency across different access methods")
    void testContextConsistencyAcrossAccessMethods() {
        // Arrange & Act
        ApplicationContextProvider.setStaticApplicationContext(mockApplicationContext);

        // Assert - Both access methods should return the same context
        ApplicationContext staticAccess = ApplicationContextProvider.getApplicationContext();
        
        contextProvider.setApplicationContext(mockApplicationContext);
        ApplicationContext instanceAccess = ApplicationContextProvider.getApplicationContext();

        assertSame(staticAccess, instanceAccess);
        assertSame(mockApplicationContext, staticAccess);
    }

    // OBJECT CREATION TESTS

    @Test
    @DisplayName("Should create ApplicationContextProvider instance successfully")
    void testObjectCreation() {
        // Act
        ApplicationContextProvider provider = new ApplicationContextProvider();

        // Assert
        assertNotNull(provider);
    }

    @Test
    @DisplayName("Should create multiple instances independently")
    void testMultipleInstanceCreation() {
        // Act
        ApplicationContextProvider provider1 = new ApplicationContextProvider();
        ApplicationContextProvider provider2 = new ApplicationContextProvider();

        // Assert
        assertNotNull(provider1);
        assertNotNull(provider2);
        // They should be different instances but work with same static context
    }

    // INTEGRATION TESTS

    @Test
    @DisplayName("Should work correctly with sequence of operations")
    void testSequenceOfOperations() {
        // Arrange
        ApplicationContext context1 = mock(ApplicationContext.class);
        ApplicationContext context2 = mock(ApplicationContext.class);

        // Act & Assert - Initial state
        assertNull(ApplicationContextProvider.getApplicationContext());

        // Set first context via static method
        ApplicationContextProvider.setStaticApplicationContext(context1);
        assertEquals(context1, ApplicationContextProvider.getApplicationContext());

        // Replace via instance method
        contextProvider.setApplicationContext(context2);
        assertEquals(context2, ApplicationContextProvider.getApplicationContext());

        // Clear context
        ApplicationContextProvider.setStaticApplicationContext(null);
        assertNull(ApplicationContextProvider.getApplicationContext());
    }

    @Test
    @DisplayName("Should handle rapid context switches correctly")
    void testRapidContextSwitches() {
        // Arrange
        ApplicationContext[] contexts = {
            mock(ApplicationContext.class),
            mock(ApplicationContext.class),
            mock(ApplicationContext.class)
        };

        // Act & Assert - Rapid switches
        for (ApplicationContext context : contexts) {
            ApplicationContextProvider.setStaticApplicationContext(context);
            assertEquals(context, ApplicationContextProvider.getApplicationContext());
        }

        // Final verification
        assertEquals(contexts[contexts.length - 1], ApplicationContextProvider.getApplicationContext());
    }

    // THREAD SAFETY SIMULATION TESTS

    @Test
    @DisplayName("Should maintain context state in sequential operations")
    void testSequentialOperations() {
        // Arrange
        ApplicationContext testContext = mock(ApplicationContext.class);

        // Act - Simulate multiple sequential calls
        ApplicationContextProvider.setStaticApplicationContext(testContext);
        ApplicationContext result1 = ApplicationContextProvider.getApplicationContext();
        ApplicationContext result2 = ApplicationContextProvider.getApplicationContext();
        ApplicationContext result3 = ApplicationContextProvider.getApplicationContext();

        // Assert - All calls should return the same context
        assertSame(testContext, result1);
        assertSame(testContext, result2);
        assertSame(testContext, result3);
        assertSame(result1, result2);
        assertSame(result2, result3);
    }
} 