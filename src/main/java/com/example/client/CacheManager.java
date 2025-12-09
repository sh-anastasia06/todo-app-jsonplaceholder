package com.example.client;

import com.example.model.Todo;

import java.util.List;
import java.util.Optional;

public interface CacheManager {
    // users todos
    void cacheUserTodos(Long userId, List<Todo> todos);
    Optional<List<Todo>> getCachedUserTodos(Long userId);

    // all todos
    void cacheAllTodos(List<Todo> todos);
    Optional<List<Todo>> getCachedAllTodos();

    // specific todo
    void cacheTodo(Todo todo);
    Optional<Todo> getCachedTodo(Long id);
    void removeFromCache(Long id);

    // all cache
    void clearCache();
    void clearUserCache(Long userId);
    // get cache statistics
    CacheStats getCacheStats();

    // nested class for statistics
    class CacheStats {
        private final int cachedUsersCount;
        private final int cachedTodosCount;
        private final long cacheSizeBytes;

        public CacheStats(int cachedUsersCount, int cachedTodosCount, long cacheSizeBytes) {
            this.cachedUsersCount = cachedUsersCount;
            this.cachedTodosCount = cachedTodosCount;
            this.cacheSizeBytes = cacheSizeBytes;
        }

        public int getCachedUsersCount() { return cachedUsersCount; }
        public int getCachedTodosCount() { return cachedTodosCount; }
        public long getCacheSizeBytes() { return cacheSizeBytes; }

        @Override
        public String toString() {
            return String.format("CacheStats{users=%d, todos=%d, size=%.2f KB",
                    cachedUsersCount, cachedTodosCount, cacheSizeBytes / 1024.0);
        }
    }
}
