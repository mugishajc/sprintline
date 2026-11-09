package io.github.mugishajc.tasks.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import java.time.Clock;

/**
 * Exposes the clock as a bean so "today" can be controlled in tests.
 */
@ApplicationScoped
public class ClockProducer {

    @Produces
    @ApplicationScoped
    Clock clock() {
        return Clock.systemDefaultZone();
    }
}
