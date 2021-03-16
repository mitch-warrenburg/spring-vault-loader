package com.github.vault.springvaultloader.retry;

import lombok.extern.log4j.Log4j2;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;

@Log4j2
public class RetryableWrapper {

    private static final int MAX_RETRIES = 5;
    private static final long COOL_DOWN_TIME = 5000;

    private final AtomicInteger retryCount = new AtomicInteger(0);

    public <T> T withRetry(Callable<T> retryableCallable) throws InterruptedException {

        do {
            try {
                return retryableCallable.call();
            } catch (Throwable e) {
                retryCount.set(retryCount.get() + 1);
                log.error("Failed to execute service call. [tries]: {}", retryCount.get());
                sleep(COOL_DOWN_TIME);
            }
        } while (retryCount.get() < MAX_RETRIES);

        log.error("Reties exhausted for service call...");

        return null;
    }
}
