package com.twasyl.slideshowfx.server;

import com.twasyl.slideshowfx.server.service.ISlideshowFXServices;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;

public class ServiceDeploymentHandler implements Handler<AsyncResult<String>> {
    private static final Logger LOGGER = Logger.getLogger(ServiceDeploymentHandler.class.getName());

    private CompletableFuture<Void> deploymentFuture;
    private Class<? extends ISlideshowFXServices> service;

    public ServiceDeploymentHandler(Class<? extends ISlideshowFXServices> service) {
        this.service = service;
        this.deploymentFuture = new CompletableFuture<>();
    }

    @Override
    public void handle(AsyncResult<String> event) {
        if (event.succeeded()) {
            LOGGER.fine("Service " + service.getName() + " has been deployed properly");
            this.deploymentFuture.complete(null);
        } else if (event.failed()) {
            LOGGER.log(WARNING, "Service " + service.getName() + " can't be deployed", event.cause());
            this.deploymentFuture.completeExceptionally(event.cause());
        }
    }

    public CompletableFuture<Void> future() {
        return this.deploymentFuture;
    }
}
