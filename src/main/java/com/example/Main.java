package com.example;

import com.example.client.JsonPlaceholderClient;
import com.example.model.Todo;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("Testing JsonPlaceholderClient\n");

        JsonPlaceholderClient client = new JsonPlaceholderClient();

        try {
            // 1. Connection test
            System.out.println("1. Connection test");
            boolean connected = client.testConnection();
            System.out.println("\tConnection: " + (connected ? "OK" : "FAILED"));

            // 2. Get all todos of the 1st user
            System.out.println("\n2. Getting todos for the 1st user");
            List<Todo> todos = client.getUserTodos(1L);
            System.out.println("\tFound: " + todos.size() + " todos");

            if (!todos.isEmpty()) {
                Todo firstTodo = todos.getFirst();
                System.out.println("\t1st todo: " + firstTodo.getTitle());
                System.out.println("\tStatus: " + firstTodo.getDisplayStatus());
            }

            // 3. Get todo by ID
            System.out.println("\n3. Getting todo by ID 3");
            var todoOptional = client.getTodoById(3L);
            todoOptional.ifPresentOrElse(
                    todo -> System.out.println("\tFound: " + todo.getTitle()),
                    () -> System.out.println("\tNot found")
            );

            // 4. Imitation of creating a todo
            System.out.println("\n4. Creating new todo");
            Todo newTodo = new Todo(1L, "New todo to test HTTP Client", false);
            Todo createdTodo = client.createTodo(newTodo);
            System.out.println("\tCreated todo with ID: " + createdTodo.getId());
            System.out.println("\tTitle: " + createdTodo.getTitle());
            System.out.println("\tUser ID in response: " + createdTodo.getUserId());

            // 5. Update todo
            System.out.println("\n5. Updating todo");
            createdTodo.setCompleted(true);
            Todo updatedTodo = client.updateTodo(createdTodo);
            System.out.println("\tCompleted: " + updatedTodo.getTitle());

            // 6. Cache statistics
            System.out.println("\n6. Cache statistics");
            var stats = client.getCacheManager().getCacheStats();
            System.out.println("\t" + stats);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\nDone");
    }
}
