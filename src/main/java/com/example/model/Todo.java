package com.example.model;

import com.google.gson.annotations.SerializedName;

public class Todo {
    private Long id;
    @SerializedName("userId")
    private Long userId;
    private String title;
    private Boolean completed;

    public Todo() {} // for Gson

    public Todo(Long userId, String title, Boolean completed) {
        this.userId = userId;
        this.title = title;
        this.completed = completed;
    }

    public Todo(Long id, Long userId, String title, Boolean completed) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.completed = completed;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    // for UI
    public String getDisplayStatus() {
        return completed ? "✓ Completed" : "○ Pending";
    }

    @Override
    public String toString() {
        return "Todo{" +
                "id=" + id +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", completed=" + completed + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // if it's the same object
        if (o == null || getClass() != o.getClass()) return false;

        Todo todo = (Todo) o;
        return id != null ? id.equals(todo.id) : todo.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
