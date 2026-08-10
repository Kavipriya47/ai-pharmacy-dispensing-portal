package com.pharmacy.dispensing.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables Spring's scheduling infrastructure.
 * Required for {@link com.pharmacy.dispensing.inventory.service.ExpiryCheckScheduler}
 * and any future scheduled tasks.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // No beans required — @EnableScheduling is sufficient
}
