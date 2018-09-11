package com.sk.bds.core.module.template.service;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * Created by longhong on 2017. 8. 17..
 */
@ProxyGen
public interface TemplateService {
    String SERVICE_NAME = "template-service";

    String SERVICE_ADDRESS = "service.template";

    String WORKER_THREAD_POOL_NAME = "template-worker-pool";

    String WORKER_THREAD_POOL_SIZE = "20";

    void test(Handler<AsyncResult<JsonObject>> resultHandler);
}