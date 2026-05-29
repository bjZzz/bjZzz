package com.nanda.common.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple snowflake-style ID for framework bootstrap (replace with proper snowflake in production).
 */
public final class IdGenerator {

    private static final AtomicLong SEQUENCE = new AtomicLong(System.currentTimeMillis());

    private IdGenerator() {
    }

    public static long nextId() {
        return SEQUENCE.incrementAndGet();
    }
}
