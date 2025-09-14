package org.acme.services;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
public class VirtualThreadProvider {

    public VirtualThreadProvider() {
        super();
    }

    @Produces
    @Singleton
    @Named("virtualThreadExecutor")
    public Executor virtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
