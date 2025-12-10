package com.example.client;

import com.example.model.Todo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class JsonPlaceholderClientTest {
    private JsonPlaceholderClient client;

    @BeforeEach
    void setUp() {
        client = new JsonPlaceholderClient();
    }

    @Test
    void testConnection() {
        try {
            boolean connected = client.testConnection();
            assertTrue(connected, "Should be able to connect API");
        } catch (Exception e) {
            fail("Connection test failed: " + e.getMessage());
        }
    }

    @Test
    void testGetUserTodos() {
        try {
            var todos = client.getUserTodos(1L);
            assertNotNull(todos);
            assertFalse(todos.isEmpty());

            // check the structure of the 1st todo
            Todo firstTodo = todos.getFirst();
            assertNotNull(firstTodo.getId());
            assertNotNull(firstTodo.getTitle());
            assertNotNull(firstTodo.getCompleted());
            assertEquals(1L, firstTodo.getUserId());
        } catch (Exception e) {
            fail("Failed to get user todos: " + e.getMessage());
        }
    }

    @Test
    void testGetTodoById() {
        try {
            var todoOptional = client.getTodoById(1L);
            assertTrue(todoOptional.isPresent());

            Todo todo = todoOptional.get();
            assertEquals(1L, todo.getId());
            assertNotNull(todo.getTitle());
        } catch (Exception e) {
            fail("Failed to get todo by ID: " + e.getMessage());
        }
    }

    @Test
    void testGetNonExistentTodo() {
        try {
            var todoOptional = client.getTodoById(99999L);
            assertFalse(todoOptional.isPresent(),
                    "Non-existent todo should return an empty Optional");
        } catch (Exception e) {
            fail("Should handle non-existent todo gracefully");
        }
    }
}
