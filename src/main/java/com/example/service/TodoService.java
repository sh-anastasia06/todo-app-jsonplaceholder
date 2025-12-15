package com.example.service;

import com.example.client.ApiClient;
import com.example.client.JsonPlaceholderClient;
import com.example.model.ApiException;
import com.example.model.Todo;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TodoService {
    private final ApiClient apiClient;
    private final AuthService authService;

    public TodoService(AuthService authService) {
        this.authService = authService != null ? authService : new AuthService();
        this.apiClient = new JsonPlaceholderClient();
    }

    public TodoService(AuthService authService, ApiClient apiClient) {
        this.authService = authService != null ? authService : new AuthService();
        this.apiClient = apiClient != null ? apiClient : new JsonPlaceholderClient();
    }

    public List<Todo> getCurrentUserTodos() throws ApiException {
        checkAuth();

        Long userId = authService.getCurrentUserId();
        return apiClient.getUserTodos(userId);
    }

    public Optional<Todo> getTodoById(Long id) throws ApiException{
        checkAuth();

        Optional<Todo> optionalTodo = apiClient.getTodoById(id);

        // check if todo belongs to user
        if (optionalTodo.isPresent()) {
            Todo todo = optionalTodo.get();
            Long currentUserId = authService.getCurrentUserId();

            if (todo.getUserId().equals(currentUserId))
                return Optional.of(todo);
        }

        return Optional.empty();
    }

    public Todo createTodo(String title, boolean completed) throws ApiException {
        checkAuth();

        if (title == null || title.trim().isEmpty())
            throw new IllegalArgumentException("Title cannot be empty");

        Long userId = authService.getCurrentUserId();
        Todo newTodo = new Todo(userId, title.trim(), completed);

        return apiClient.createTodo(newTodo);
    }

    public Todo updateTodo(Long id, String title, Boolean completed) throws ApiException {
        checkAuth();

        Optional<Todo> optionalTodo = getTodoById(id);
        if (optionalTodo.isEmpty())
            throw new ApiException("Todo not found or doesn't belong to current user");

        Todo todo = optionalTodo.get();

        if (title != null)
            todo.setTitle(title);
        if (completed != null)
            todo.setCompleted(completed);

        return apiClient.updateTodo(todo);
    }

    public Todo updateTodoCompletion(Long id) throws ApiException {
        checkAuth();

        Optional<Todo> optionalTodo = getTodoById(id);
        if (optionalTodo.isEmpty())
            throw new ApiException("Todo not found or doesn't belong to current user");

        Todo todo = optionalTodo.get();
        todo.setCompleted(!todo.getCompleted());

        return apiClient.updateTodo(todo);
    }

    public boolean deleteTodo(Long id) throws ApiException {
        checkAuth();

        Optional<Todo> optionalTodo = getTodoById(id);
        if (optionalTodo.isEmpty())
            return false;

        return apiClient.deleteTodo(id);
    }

    public List<Todo> getCompletedTodos() throws ApiException {
        checkAuth();

        Long userId = authService.getCurrentUserId();
        return apiClient.getTodosByCompletion(userId, true);
    }

    public List<Todo> getPendingTodos() throws ApiException {
        checkAuth();

        Long userId = authService.getCurrentUserId();
        return apiClient.getTodosByCompletion(userId, false);
    }

    public List<Todo> searchTodos(String searchStr) throws ApiException {
        checkAuth();

        if (searchStr == null || searchStr.trim().isEmpty())
            return getCompletedTodos();

        List<Todo> todos = getCurrentUserTodos();
        String finalSearchStr = searchStr.trim().toLowerCase();

        return todos.stream()
                .filter(todo -> todo.getTitle().toLowerCase().contains(finalSearchStr))
                .collect(Collectors.toList());
    }

    public TodoStats getTodoStats() throws ApiException {
        checkAuth();

        List<Todo> todos = getCurrentUserTodos();

        long total = todos.size();
        long completed = todos.stream().filter(todo -> todo.getCompleted() == true).count();
        long pending = total - completed;

        return new TodoStats(total, completed, pending);
    }

    public int deleteAllCompleted() throws ApiException {
        checkAuth();

        List<Todo> completedTodos = getCompletedTodos();
        int deletedCount = 0;

        for (Todo t : completedTodos) {
            if (apiClient.deleteTodo(t.getId()))
                deletedCount++;
        }

        return deletedCount;
    }

    public int markAllAsCompleted() throws ApiException {
        checkAuth();

        List<Todo> pendingTodos = getPendingTodos();
        int updatedCount = 0;

        for (Todo t : pendingTodos) {
            t.setCompleted(true);
            if (apiClient.updateTodo(t) != null)
                updatedCount++;
        }

        return updatedCount;
    }

    public ApiClient getApiClient() { return apiClient; }

    public AuthService getAuthService() { return authService; }

    // helper
    private void checkAuth() throws ApiException {
        if (!authService.isLoggedIn())
            throw new ApiException("User is not authenticated. Login first");
    }

    // inner class for statistics
    public static class TodoStats {
        private final long total;
        private final long completed;
        private final long pending;

        public TodoStats(long total, long completed, long pending) {
            this.total = total;
            this.completed = completed;
            this.pending = pending;
        }


        public long getTotal() { return total; }

        public long getCompleted() { return completed; }

        public long getPending() { return pending; }

        public double getCompletedPercentage() {
            return total > 0 ? (completed * 100.0 / total) : 0.0;
        }

        @Override
        public String toString() {
            return String.format("Total: %d, Completed: %d, Pending: %d (%.1f%%)",
                    total, completed, pending, getCompletedPercentage());
        }

        public String toShartString() {
            return String.format("%d/%d", completed, total);
        }
    }
}
