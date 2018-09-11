package com.sk.bds.core.module.template.handler;

import com.sk.bds.core.module.template.service.TemplateService;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class TemplateHandler implements Handler<RoutingContext> {
    private final TemplateService templateService;

    public TemplateHandler(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "application/json");

        templateService.test(ar -> {
            if (ar.succeeded()) {
                JsonObject resBody = ar.result();
                response.setStatusCode(200).end(resBody.encode());
            } else {
                ReplyException result = (ReplyException) ar.cause();
                JsonObject errorMsg = new JsonObject();
                errorMsg.put("statusCode", result.failureCode());
                errorMsg.put("message", result.getMessage());
                response.setStatusCode(result.failureCode()).end(errorMsg.encode());
            }
        });
    }
}