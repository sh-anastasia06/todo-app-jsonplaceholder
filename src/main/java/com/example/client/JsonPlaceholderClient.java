package com.example.client;

import com.example.client.impl.MemoryCacheManager;
import com.example.model.ApiException;
import com.example.model.Todo;
import com.example.model.User;
import com.google.gson.reflect.TypeToken;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

// client for JSONPlaceholder API
public class JsonPlaceholderClient extends AbstractHttpClient{
    private final CacheManager cacheManager;

    // constants for API endpoints
    private static final String TODOS_ENDPOINT = "/todos";
    private static final String USERS_ENDPOINT = "/users";

    // timeouts
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);

    public JsonPlaceholderClient() {
        super();
        this.cacheManager = new MemoryCacheManager();
    }

    public JsonPlaceholderClient(CacheManager cacheManager) {
        super();
        this.cacheManager = cacheManager != null ? cacheManager : new MemoryCacheManager();
    }

    public JsonPlaceholderClient(HttpClient httpClient, CacheManager cacheManager) {
        super(httpClient);
        this.cacheManager = cacheManager != null ? cacheManager : new MemoryCacheManager();
    }

    @Override
    public List<Todo> getAllTodos() throws ApiException {
        // check cache
        Optional<List<Todo>> cached = cacheManager.getCachedAllTodos();
        if (cached.isPresent()) {
            System.out.println("Using cached todos");
            return new ArrayList<>(cached.get());
        }

        try {
            HttpRequest request = buildGetRequest(buildUrl(TODOS_ENDPOINT));
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            handleResponseError(response);

            List<Todo> todos = GSON.fromJson(response.body(), new TypeToken<List<Todo>>(){}.getType());
            // save in cache
            cacheManager.cacheAllTodos(todos);

            return todos != null ? todos : new ArrayList<>();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Request was interrupted", e);
        } catch (Exception e) {
            throw new ApiException("Failed to get all todos: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Todo> getUserTodos(Long userId) throws ApiException {
        validateUserId(userId);

        // checking cache
        Optional<List<Todo>> cached = cacheManager.getCachedUserTodos(userId);
        if (cached.isPresent()) {
            System.out.println("Using cached todos for user " + userId);
            return new ArrayList<>(cached.get());
        }

        // if it's not in cache -> request it from API
        try {
            String url = buildUrl(TODOS_ENDPOINT) + "?userId=" + userId;
            HttpRequest request = buildGetRequest(url);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            handleResponseError(response);

            List<Todo> todos = GSON.fromJson(response.body(),
                    new TypeToken<List<Todo>>(){}.getType());

            // save in cache
            cacheManager.cacheUserTodos(userId, todos);

            return todos != null ? todos : new ArrayList<>();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Request was interrupted", e);
        } catch (Exception e) {
            throw new ApiException("Failed to get users todos: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Todo> getTodoById(Long id) throws ApiException {
        validateId(id, "Todo ID");

        // checking cache
        Optional<Todo> cached = cacheManager.getCachedTodo(id);
        if (cached.isPresent()) {
            System.out.println("Using cached todo " + id);
            return cached;
        }

        try {
            String url = buildUrl(TODOS_ENDPOINT + "/" + id);
            HttpRequest request = buildGetRequest(url);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // 404 is okay for getById, do not throw exception
            if (response.statusCode() == 404) {
                return Optional.empty();
            }

            handleResponseError(response);

            Todo todo = GSON.fromJson(response.body(), Todo.class);
            // save in cache
            if (todo != null)
                cacheManager.cacheTodo(todo);

            return Optional.ofNullable(todo);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (Exception e) {
            throw new ApiException("Failed to get todo by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public Todo createTodo(Todo todo) throws ApiException {
        validateTodo(todo);

        try {
            String json = GSON.toJson(todo);
            HttpRequest request = buildPostRequest(buildUrl(TODOS_ENDPOINT), json);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            handleResponseError(response);

            Todo createdTodo = GSON.fromJson(response.body(), Todo.class);

            // update cache
            if (createdTodo != null) {
                cacheManager.cacheTodo(createdTodo);
                cacheManager.clearUserCache(todo.getUserId());
            }

            return createdTodo;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Request was interrupted", e);
        } catch (Exception e) {
            throw new ApiException("Failed to create todo: " + e.getMessage(), e);
        }
    }

    @Override
    public Todo updateTodo(Todo todo) throws ApiException {
        validateTodo(todo);
        validateId(todo.getId(), "Todo ID");

        try {
            String json = GSON.toJson(todo);
            String url = buildUrl(TODOS_ENDPOINT + "/" + todo.getId());
            HttpRequest request = buildPutRequest(url, json);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            handleResponseError(response);

            Todo updatedTodo = GSON.fromJson(response.body(), Todo.class);

            // update cache
            if (updatedTodo != null) {
                cacheManager.cacheTodo(updatedTodo);
                cacheManager.clearUserCache(todo.getUserId());
            }

            return updatedTodo;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Request was interrupted", e);
        } catch (Exception e) {
            throw new ApiException("Failed to create todo: " + e.getMessage(), e);
        }
    }

    @Override
    public Todo patchTodo(Long id, Todo partialTodo) throws ApiException {
        validateId(id, "Todo ID");
        if (partialTodo == null)
            throw new IllegalArgumentException("Partial todo cannot be null");

        try {
            String json = GSON.toJson(partialTodo);
            String url = buildUrl(TODOS_ENDPOINT + "/" + id);
            HttpRequest request = buildPatchRequest(url, json);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            handleResponseError(response);

            Todo patchedTodo = GSON.fromJson(response.body(), Todo.class);

            // update cache
            if (patchedTodo != null) {
                cacheManager.cacheTodo(patchedTodo);
                if (patchedTodo.getUserId() != null)
                    cacheManager.clearUserCache(patchedTodo.getUserId());
            }

            return patchedTodo;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Request was interrupted", e);
        } catch (Exception e) {
            throw new ApiException("Failed to patch todo: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteTodo(Long id) throws ApiException {
        validateId(id, "Todo ID");

        try {
            String url = buildUrl(TODOS_ENDPOINT + "/" + id);
            HttpRequest request = buildDeleteRequest(url);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // 404 means it's already deleted
            if (response.statusCode() == 404) {
                cacheManager.removeFromCache(id);
                return true;
            }

            handleResponseError(response);

            // remove from cache
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                cacheManager.removeFromCache(id);
                return true;
            }

            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Request was interrupted", e);
        } catch (Exception e) {
            throw new ApiException("Failed to delete todo: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Todo> getTodosByCompletion(Long userId, boolean completed) throws ApiException {
        validateUserId(userId);

        try {
            String url = buildUrl(TODOS_ENDPOINT +
                    "?userId=" + userId +
                    "%completed=" + completed);
            HttpRequest request = buildGetRequest(url);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            handleResponseError(response);

            List<Todo> todos = GSON.fromJson(response.body(), new TypeToken<List<Todo>>(){}.getType());

            return todos != null ? todos : new ArrayList<>();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Request was interrupted", e);
        } catch (Exception e) {
            throw new ApiException("Failed to get todos by completion: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> getAllUsers() throws ApiException {
        try {
            HttpRequest request = buildGetRequest(buildUrl(USERS_ENDPOINT));
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            handleResponseError(response);

            List<User> users = GSON.fromJson(response.body(), new TypeToken<List<User>>(){}.getType());

            return users != null ? users : new ArrayList<>();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Request was interrupted", e);
        } catch (Exception e) {
            throw new ApiException("Failed to get all users: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> getUserById(Long id) throws ApiException {
        validateId(id, "User ID");

        try {
            String url = buildUrl(USERS_ENDPOINT + "/" + id);
            HttpRequest request = buildGetRequest(url);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 400)
                return Optional.empty();

            handleResponseError(response);

            User user = GSON.fromJson(response.body(), User.class);
            return Optional.ofNullable(user);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (Exception e) {
            throw new ApiException("Failed to get user by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean testConnection() throws ApiException {
        try {
            HttpRequest request = buildGetRequest(buildUrl(TODOS_ENDPOINT + "/1"));

            // async request with timer
            CompletableFuture<HttpResponse<String>> future =
                    httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> response = future.get(5, TimeUnit.SECONDS);

            return response.statusCode() == 200;
        } catch (TimeoutException e) {
            throw new ApiException("Connection timeout: API is not responding", e);
        } catch (ExecutionException e) {
            throw new ApiException("Connection failed: " + e.getCause().getMessage(), e.getCause());
        } catch (Exception e) {
            throw new ApiException("Connection test failed: " + e.getMessage(), e);
        }
    }

    // helpers

    private HttpRequest buildGetRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("Content-Type", "application-json")
                .timeout(REQUEST_TIMEOUT)
                .GET().build();
    }

    private HttpRequest buildPostRequest(String url, String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("Content-Type", "application-json")
                .timeout(REQUEST_TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    private HttpRequest buildPutRequest(String utl, String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(utl))
                .header("Accept", "application-json")
                .header("Content-Type", "application-json")
                .timeout(REQUEST_TIMEOUT)
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    private HttpRequest buildPatchRequest(String url, String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application-json")
                .header("Content-Type", "application-json")
                .timeout(REQUEST_TIMEOUT)
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    private HttpRequest buildDeleteRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application-json")
                .header("Content-Type", "application-json")
                .timeout(REQUEST_TIMEOUT)
                .DELETE().build();
    }

    // validation & error handling

    private void handleResponseError(HttpResponse<String> response) throws ApiException {
        int statusCode = response.statusCode();

        if (statusCode >= 200 && statusCode < 300)
            return;

        String message = "HTTP Error " + statusCode;
        if (response.body() != null && !response.body().isEmpty()) {
            try {
                var errorObj = GSON.fromJson(response.body(), java.util.Map.class);
                if (errorObj != null && errorObj.containsKey("message"))
                    message += ": " + errorObj.get("message");
            } catch (Exception e) {
                message += ": " + response.body();
            }
        }

        throw new ApiException(message, statusCode);
    }

    private void validateId(Long id, String fieldName) {
        if (id == null || id <= 0)
            throw new IllegalArgumentException(fieldName + " must be positive: " + id);
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0)
            throw new IllegalArgumentException("Invalid user ID: " + userId);
    }

    private void validateTodo(Todo todo) {
        if (todo == null)
            throw new IllegalArgumentException("Todo cannot be null");
        if (todo.getUserId() == null || todo.getUserId() <= 0)
            throw new IllegalArgumentException("Invalid user ID in todo");
        if (todo.getTitle() == null || todo.getTitle().trim().isEmpty())
            throw new IllegalArgumentException("Todo title cannot be empty");
    }

    // getter
    public CacheManager getCacheManager() { return cacheManager; }
}
