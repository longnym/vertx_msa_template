package com.sk.bds.core.module.template;

import com.sk.bds.core.module.template.util.PropertiesUtil;
import com.sk.bds.core.module.template.verticle.TemplateApiVerticle;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by longhong on 2017. 7. 14..
 */
public class TemplateApiLauncher {
    private static final String DEFAULT_HOST = "0.0.0.0";
    private static final String DEFAULT_PORT = "7070";
    private static final Logger logger = LoggerFactory.getLogger(TemplateApiLauncher.class);

    public static void main(String[] args) {
        String host = DEFAULT_HOST;
        try {
            InetAddress addr = InetAddress.getLocalHost();
            host = addr.getHostAddress();
        } catch (UnknownHostException e) {
            logger.error(e.getMessage());
        }
        logger.info("Host Address: " + host);

        JsonObject dConfig = new JsonObject()
                .put("http.host", host)
                .put("http.port", DEFAULT_PORT)
                .mergeIn(PropertiesUtil.getJsonProperties());

        EventBusOptions eOptions = new EventBusOptions()
                .setHost(host);

        VertxOptions vOptions = new VertxOptions()
                .setMaxEventLoopExecuteTime(10 * 1000 * 1000)   // ns
                .setBlockedThreadCheckInterval(60 * 1000)   // ms
                .setHAEnabled(true)
                .setHAGroup("template-api")
                .setClusterHost(host)
                .setEventBusOptions(eOptions);

        logger.info("HA Group: " + vOptions.getHAGroup());

        Vertx.clusteredVertx(vOptions, cRes -> {
            if (cRes.succeeded()) {
                // usage) java -Dinstance.bare=true -jar ...
                String bareInstance = System.getProperty("instance.bare", "false").toLowerCase();
                if (bareInstance.equals("true")) {
                    logger.info("Bare instance is launched.");
                } else {
                    Vertx vertx = cRes.result();

                    DeploymentOptions dOptions = new DeploymentOptions()
                            .setHa(true)
                            .setConfig(dConfig);

                    String verticleName = TemplateApiVerticle.class.getName();
                    vertx.deployVerticle(verticleName, dOptions, dRes -> {
                        if (dRes.succeeded()) {
                            logger.info(verticleName + " is deployed. (Deployment ID: " + dRes.result() + ")");
                        } else {
                            logger.error(dRes.cause().getMessage());
                        }
                    });

                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        System.out.print("Waiting for shutdown template service.");
                        vertx.deploymentIDs().forEach(vertx::undeploy);
                        try {
                            Thread.sleep(5000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        System.out.print("template service is shutdown.");
                    }));
                }
            } else {
                logger.error("Failed Clustering: " + cRes.cause());
            }
        });
    }
}