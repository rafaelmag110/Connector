package com.microsoft.dagx.spi.transfer;

/**
 * Implements a wait strategy for the {@link TransferProcessManager}.
 *
 * Implementations may choose to to enforce an incremental backoff period when no processing occurs over a successive number of iterations.
 */
@FunctionalInterface
public interface TransferWaitStrategy {

    /**
     * Returns the number of milliseconds to pause for the current iteration.
     */
    long waitForMillis();

    /**
     * Marks the iteration as successful.
     */
    default void success() {
    }

    /**
     * Returns the number of milliseconds to wait before retrying.
     */
    default long retryInMillis() {
        return waitForMillis();
    }

}
