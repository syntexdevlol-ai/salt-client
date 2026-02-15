package com.saltclient.util;

import java.util.ArrayDeque;
import java.util.Deque;

public final class ClickTracker {
    private final Deque<Long> left = new ArrayDeque<>();
    private final Deque<Long> right = new ArrayDeque<>();

    public void recordLeft() {
        long now = System.currentTimeMillis();
        left.addLast(now);
        trim(left, now);
    }

    public void recordRight() {
        long now = System.currentTimeMillis();
        right.addLast(now);
        trim(right, now);
    }

    public int getLeftCps() {
        long now = System.currentTimeMillis();
        trim(left, now);
        return left.size();
    }

    public int getRightCps() {
        long now = System.currentTimeMillis();
        trim(right, now);
        return right.size();
    }

    private static void trim(Deque<Long> q, long now) {
        long cutoff = now - 1000L;
        while (!q.isEmpty() && q.peekFirst() < cutoff) {
            q.removeFirst();
        }
    }
}

