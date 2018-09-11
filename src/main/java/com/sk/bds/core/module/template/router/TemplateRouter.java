package com.sk.bds.core.module.template.router;

import com.sk.bds.core.module.template.handler.TemplateHandler;
import com.sk.bds.core.module.template.service.TemplateService;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * Created by longhong on 2017. 7. 24..
 */
public class TemplateRouter {
    private final Vertx vertx;

    private final TemplateService templateService;

    public TemplateRouter(Vertx vertx, TemplateService templateService) {
        this.templateService = templateService;
        this.vertx = vertx;
    }

    public Router getRouter() {
        // Register routing handlers.
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.get("/test").handler(new TemplateHandler(templateService));

        return router;
    }
}