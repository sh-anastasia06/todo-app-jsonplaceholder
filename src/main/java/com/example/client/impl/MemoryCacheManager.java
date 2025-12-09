package com.example.client.impl;

import com.example.client.CacheManager;
import com.example.model.Todo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/*
    Implementation of an in-memory cache manager with Cache Lifetime (TTL) support
 */
public class MemoryCacheManager implements CacheManager {
    // todos cache by user id
    private final Map<Long, List<Todo>> userTodosCache = new ConcurrentHashMap<>();
    // all todos cache
    private List<Todo> allTodosCache = null;
    // todo cache by id
    private final Map<Long, Todo> todoCache = new ConcurrentHashMap<>();

    // Cache creation time
    private final Map<Long, Long> cacheTimestamps = new ConcurrentHashMap<>();
    private Long allTodosTimestamp = null;

    // cache lifetime (5 min in ms)
    private static final long DEFAULT_TTL = TimeUnit.MINUTES.toMillis(5);
    private long ttl = DEFAULT_TTL;

    @Override
    public void cacheUserTodos(Long userId, List<Todo> todos) {
        if (userId == null || todos == null) return;

        userTodosCache.put(userId, new ArrayList<>(todos));
        cacheTimestamps.put(userId, System.currentTimeMillis());

        // cache each todo
        for (Todo t : todos) {
            cacheTodo(t);
        }
    }

    @Override
    public Optional<List<Todo>> getCachedUserTodos(Long userId) {
        if (userId == null) return Optional.empty();

        Long timestamp = cacheTimestamps.get(userId);
        if (timestamp != null && isExpired(timestamp)) {
            // cache is outdated -> delete it
            userTodosCache.remove(userId);
            cacheTimestamps.remove(userId);
            return Optional.empty();
        }

        List<Todo> cached = userTodosCache.get(userId);
        return cached != null ? Optional.of(new ArrayList<>(cached)) : Optional.empty();
    }

    @Override
    public void cacheAllTodos(List<Todo> todos) {
        if (todos == null) return;

        this.allTodosCache = new ArrayList<>(todos);
        this.allTodosTimestamp = System.currentTimeMillis();

        // cache each todo
        for (Todo t : todos) {
            cacheTodo(t);
        }
    }

    @Override
    public Optional<List<Todo>> getCachedAllTodos() {
        if (allTodosTimestamp != null && isExpired(allTodosTimestamp)) {
            // cache is outdated -> delete it
            allTodosCache = null;
            allTodosTimestamp = null;
            return Optional.empty();
        }

        return allTodosCache != null ? Optional.of(new ArrayList<>(allTodosCache)) : Optional.empty();
    }

    @Override
    public void cacheTodo(Todo todo) {
        if (todo != null && todo.getId() != null) {
            todoCache.put(todo.getId(), todo);
        }
    }

    @Override
    public Optional<Todo> getCachedTodo(Long id) {
        if (id == null) return Optional.empty();

        return Optional.ofNullable(todoCache.get(id));
    }

    @Override
    public void removeFromCache(Long id) {
        if (id == null) return;

        // remove from todos cache
        todoCache.remove(id);
        // remove from users cache
        for (Map.Entry<Long, List<Todo>> entry : userTodosCache.entrySet()) {
            entry.getValue().removeIf(todo -> id.equals(todo.getId()));
        }
    }

    @Override
    public void clearCache() {
        userTodosCache.clear();
        todoCache.clear();
        cacheTimestamps.clear();
        allTodosCache = null;
        allTodosTimestamp = null;
    }

    @Override
    public void clearUserCache(Long userId) {
        if (userId == null) return;

        userTodosCache.remove(userId);
        cacheTimestamps.remove(userId);
    }

    @Override
    public CacheStats getCacheStats() {
        int usersCount = userTodosCache.size();
        int todosCount = todoCache.size();

        long sizeBytes = estimateSize();

        return new CacheStats(usersCount, todosCount, sizeBytes);
    }

    // helpers
    private boolean isExpired(Long timestamp) {
        return (System.currentTimeMillis() - timestamp) > ttl;
    }

    private long estimateSize() {
        long size = 0;

        // Estimating the size of todos cache
        for (Todo t : todoCache.values()) {
            size += estimateTodoSize(t);
        }

        // Estimating the size of data structures
        size += userTodosCache.size() * 50L; // Approximately
        size += cacheTimestamps.size() * 16L;

        return size;
    }

    private long estimateTodoSize(Todo todo) {
        if (todo == null) return 0;

        long size = 0;
        size += 16; // object title
        size += 16; // long fields (2*8)

        if (todo.getTitle() != null) {
            size += 36 + todo.getTitle().length() * 2L; // string overhead + chars
        }

        size += 1; // boolean

        return size;
    }

    public void setTtl(long ttlMillis) { this.ttl = ttlMillis; }

    public long getTtl() { return ttl; }
}
