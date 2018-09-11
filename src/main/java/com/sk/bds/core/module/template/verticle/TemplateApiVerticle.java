package com.sk.bds.core.module.template.verticle;

import com.sk.bds.core.module.template.router.TemplateRouter;
import com.sk.bds.core.module.template.service.TemplateService;
import com.sk.bds.core.module.template.service.impl.TemplateServiceImpl;
import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.serviceproxy.ProxyHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by longhong on 2017. 7. 14..
 */
public class TemplateApiVerticle extends AbstractVerticle {
    private static final String REST_SERVICE_NAME = "template-rest-api";

    private static final Logger logger = LoggerFactory.getLogger(TemplateApiVerticle.class);

    private ServiceDiscovery discovery;

    private Set<Record> registeredRecords = new ConcurrentHashSet<>();

    @Override
    public void start(Future<Void> future) throws Exception {
        discovery = ServiceDiscovery.create(vertx);

        vertx.exceptionHandler((Throwable throwable) -> {
            logger.error("SEVERE: " + throwable.toString());
            StackTraceElement[] elements = throwable.getStackTrace();
            for (StackTraceElement element : elements) {
                if (element.getClassName().startsWith("com.sk.bds.core")) {
                    logger.debug("Stack Trace: " + element.toString());
                }
            }
        });

        // create rdbms batch service
        TemplateService templateService = new TemplateServiceImpl(vertx);
        ProxyHelper.registerService(TemplateService.class, vertx, templateService, TemplateService.SERVICE_ADDRESS);

        // publish rdbms batch eventBus service
        Record recordWorkflow = EventBusService.createRecord(
                TemplateService.SERVICE_NAME,
                TemplateService.SERVICE_ADDRESS,
                TemplateService.class
        );

        discovery.publish(recordWorkflow, ar -> {
            registeredRecords.add(recordWorkflow);
            if (ar.succeeded()) {
                logger.info("template service is published.");
            } else {
                logger.error("Failed to publish template service.");
            }
        });

        // create routing information
        Router router = Router.router(vertx);
        router.mountSubRouter("/template", new TemplateRouter(vertx, templateService).getRouter());

        // create HTTP server and publish REST service
        String httpHost = config().getString("http.host");
        int httpPort = Integer.parseInt(config().getString("http.port"));

        createHttpServer(router, httpPort)
                .compose(serverCreated -> publishHttpEndpoint(REST_SERVICE_NAME, httpHost, httpPort))
                .setHandler(future.completer());
    }

    @Override
    public void stop(Future<Void> future) throws Exception {
        logger.info("Unpublish services from discovery.");
        // In current design, the publisher is responsible for removing the service
        List<Future> futures = new ArrayList<>();
        registeredRecords.forEach(record -> {
            Future<Void> cleanupFuture = Future.future();
            futures.add(cleanupFuture);
            discovery.unpublish(record.getRegistration(), cleanupFuture.completer());
        });

        if (futures.isEmpty()) {
            discovery.close();
            future.complete();
        } else {
            CompositeFuture.all(futures)
                    .setHandler(ar -> {
                        discovery.close();
                        if (ar.failed()) {
                            future.fail(ar.cause());
                        } else {
                            future.complete();
                        }
                    });
        }
    }

    public Future<Void> createHttpServer(Router router, int port) {
        Future<HttpServer> httpServerFuture = Future.future();
        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(port, httpServerFuture.completer());
        return httpServerFuture.map(r -> null);
    }

    public Future<Void> publishHttpEndpoint(String name, String host, int port) {
        Record record = HttpEndpoint.createRecord(name, host, port, "/",
                new JsonObject().put("api.name", "template")
        );

        Future<Void> future = Future.future();
        discovery.publish(record, ar -> {
            if (ar.succeeded()) {
                registeredRecords.add(record);
                logger.info("Service <" + ar.result().getName() + "> published");
                future.complete();
            } else {
                future.fail(ar.cause());
            }
        });
        return future;
    }
}