/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.internal.processor.strategy;

import static java.util.Objects.requireNonNull;
import static org.mule.runtime.core.api.processor.ReactiveProcessor.ProcessingType.BLOCKING;
import static org.mule.runtime.core.api.processor.ReactiveProcessor.ProcessingType.CPU_LITE_ASYNC;
import static reactor.core.publisher.Flux.from;
import static reactor.core.scheduler.Schedulers.fromExecutorService;

import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.Startable;
import org.mule.runtime.api.lifecycle.Stoppable;
import org.mule.runtime.api.scheduler.Scheduler;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.construct.FlowConstruct;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.processor.ReactiveProcessor;
import org.mule.runtime.core.api.processor.Sink;
import org.mule.runtime.core.api.processor.strategy.ProcessingStrategy;
import org.mule.runtime.core.internal.context.thread.notification.ThreadNotificationLogger;
import org.mule.runtime.core.internal.context.thread.notification.ThreadNotificationService;
import org.mule.runtime.core.internal.registry.DefaultRegistry;
import reactor.core.publisher.Flux;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Creates {@link WorkQueueProcessingStrategy} instances. This processing strategy dispatches incoming events to a work queue
 * which is served by a pool of worker threads from the applications IO {@link Scheduler}.
 * <p/>
 * This processing strategy is not suitable for transactional flows and will fail if used with an active transaction.
 *
 * @since 4.0
 */
public class WorkQueueProcessingStrategyFactory extends AbstractProcessingStrategyFactory {


  @Override
  public ProcessingStrategy create(MuleContext muleContext, String schedulersNamePrefix) {
    return new WorkQueueProcessingStrategy(() -> muleContext.getSchedulerService()
        .ioScheduler(createSchedulerConfig(muleContext, schedulersNamePrefix, BLOCKING)),
                                           getThreadNotificationLogger(muleContext));
  }

  @Override
  public Class<? extends ProcessingStrategy> getProcessingStrategyType() {
    return WorkQueueProcessingStrategy.class;
  }

  static class WorkQueueProcessingStrategy extends AbstractProcessingStrategy implements Startable, Stoppable {

    private final Supplier<Scheduler> ioSchedulerSupplier;
    private Scheduler ioScheduler;

    public WorkQueueProcessingStrategy(Supplier<Scheduler> ioSchedulerSupplier, ThreadNotificationLogger logger) {
      super(logger);
      this.ioSchedulerSupplier = requireNonNull(ioSchedulerSupplier);
    }

    public WorkQueueProcessingStrategy(Supplier<Scheduler> ioSchedulerSupplier) {
      this.ioSchedulerSupplier = requireNonNull(ioSchedulerSupplier);
    }

    @Override
    public Sink createSink(FlowConstruct flowConstruct, ReactiveProcessor pipeline) {
      return new StreamPerEventSink(pipeline, createOnEventConsumer());
    }

    @Override
    public ReactiveProcessor onPipeline(ReactiveProcessor pipeline) {
      return publisher -> {
        Flux<CoreEvent> p = from(publisher);
        p = threadNotificationLogger.addStartTransitionLogging(p);
        p = p.publishOn(fromExecutorService(decorateScheduler(ioScheduler)));
        p = threadNotificationLogger.addFinishTransitionLogging(p);
        return p.transform(pipeline)
            .subscriberContext(ctx -> ctx.put(PROCESSOR_SCHEDULER_CONTEXT_KEY, ioScheduler));
      };
    }

    @Override
    public ReactiveProcessor onProcessor(ReactiveProcessor processor) {
      if (processor.getProcessingType() == CPU_LITE_ASYNC) {
        return publisher -> {
          Flux<CoreEvent> p = from(publisher);
          p = p.transform(processor);
          p = threadNotificationLogger.addStartTransitionLogging(p);
          p = p.publishOn(fromExecutorService(decorateScheduler(ioScheduler)));
          p = threadNotificationLogger.addFinishTransitionLogging(p);
          return p;
        };
      } else {
        return super.onProcessor(processor);
      }
    }

    @Override
    public void start() throws MuleException {
      this.ioScheduler = ioSchedulerSupplier.get();
    }

    @Override
    public void stop() throws MuleException {
      if (ioScheduler != null) {
        ioScheduler.stop();
      }
    }

  }


}
