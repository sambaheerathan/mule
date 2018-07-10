/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.internal.context.thread.notification;

import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.privileged.event.BaseEventContext;
import reactor.core.publisher.Flux;

import java.util.Optional;

import static org.mule.runtime.core.internal.context.thread.notification.ThreadNotificationService.THREAD_LOGGING;

public class ThreadNotificationLogger {

  private DefaultThreadNotificationElement.Builder threadNotificationBuilder = new DefaultThreadNotificationElement.Builder();
  private Optional<ThreadNotificationService> threadNotificationService;

  public ThreadNotificationLogger(Optional<ThreadNotificationService> threadNotificationService) {
    this.threadNotificationService = threadNotificationService;
  }

  private Flux<CoreEvent> addLogging(Flux<CoreEvent> publisher, boolean checkFrom, boolean checkTo) {
    if (!THREAD_LOGGING || !threadNotificationService.isPresent()) {
      return publisher;
    }
    return publisher.doOnNext(coreEvent -> {
      if (checkTo) {
        threadNotificationBuilder.toThread(Thread.currentThread());
        threadNotificationService.get().addThreadNotificationElement(threadNotificationBuilder.build());
      }
      if (checkFrom) {
        threadNotificationBuilder.fromThread(Thread.currentThread());
      }
    });
  }

  public Flux<CoreEvent> addStartTransitionLogging(Flux<CoreEvent> publisher) {
    return addLogging(publisher, true, false);
  }

  public Flux<CoreEvent> addFinishTransitionLogging(Flux<CoreEvent> publisher) {
    return addLogging(publisher, false, true);
  }
}
