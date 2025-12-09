package com.example.client;

import com.example.model.ApiException;
import com.example.model.Todo;
import com.example.model.User;

import java.util.List;
import java.util.Optional;

// interface for working with JSONPlaceholder API
public interface ApiClient {
    // methods for todos
    List<Todo> getAllTodos() throws ApiException;
    List<Todo> getUserTodos(Long userId) throws ApiException;
    Optional<Todo> getTodoById(Long id) throws ApiException;
    Todo createTodo(Todo todo) throws ApiException; // returns created todo with id
    Todo updateTodo(Todo todo) throws ApiException; // update existing todo
    Todo patchTodo(Long id, Todo partialTodo) throws ApiException; // update a part of todo
    boolean deleteTodo(Long id) throws ApiException;
    List<Todo> getTodosByCompletion(Long userId, boolean completed) throws ApiException;

    // methods for users
    List<User> getAllUsers() throws ApiException;
    Optional<User> getUserById(Long id) throws ApiException;

    // utility methods
    boolean testConnection() throws ApiException;
    void setBaseUrl(String baseUrl);
    String getBaseUrl();
}
