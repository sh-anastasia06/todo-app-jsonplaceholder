package com.example;

import com.example.model.Todo;
import com.example.model.User;

public class Main {
    public static void main(String[] args) {
        System.out.println("Todo App - Testing models");

        Todo todo = new Todo(1L, 1L, "Learn Java", false);
        User user = new User(1L, "John Doe", "johndoe", "johndoe@example.com");

        System.out.println("\n=== Debug info ===");
        System.out.println("Todo: " + todo);
        System.out.println("User: " + user);

        System.out.println("\n=== UI info ===");
        System.out.println("Todo status: " + todo.getDisplayStatus());
        System.out.println("User display: " + user.getDisplayName());

        Todo todo1 = new Todo(1L, 1L, "Task 1", false);
        Todo todo2 = new Todo(1L, 1L, "Task 1 modified", true);

        System.out.println("\n=== Testing equals ===");
        System.out.println("todo1.equals(todo2): " + todo1.equals(todo2)); // true (equal id)
        System.out.println("todo1 hashCode: " + todo1.hashCode());
        System.out.println("todo2 hashCode: " + todo2.hashCode());
    }
}
