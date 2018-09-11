package com.sk.bds.core.module.template.service.impl;

import com.sk.bds.core.module.template.service.TemplateService;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by longhong on 2017. 8. 17..
 */
public class TemplateServiceImpl implements TemplateService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private WorkerExecutor workerExecutor;

    private JsonObject moduleConfig;

    public TemplateServiceImpl(Vertx vertx) {
        this.moduleConfig = vertx.getOrCreateContext().config();
        this.workerExecutor = vertx.createSharedWorkerExecutor(
                WORKER_THREAD_POOL_NAME,
                Integer.parseInt(moduleConfig.getString("worker.thread.pool", WORKER_THREAD_POOL_SIZE)));
    }

    @Override
    public void test(Handler<AsyncResult<JsonObject>> resultHandler) {
        workerExecutor.executeBlocking(future -> {
            logger.info("Worker thread: " + Thread.currentThread().getName() + ", function: test()");
            future.complete();
        }, false, null);
    }
}