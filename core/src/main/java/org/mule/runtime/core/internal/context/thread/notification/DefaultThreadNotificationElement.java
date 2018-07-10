/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.internal.context.thread.notification;

public final class DefaultThreadNotificationElement implements ThreadNotificationService.ThreadNotificationElement {

  private Thread fromThread;
  private Thread toThread;
  private long transition;

  private DefaultThreadNotificationElement(Thread from, Thread to, long time) {
    this.fromThread = from;
    this.toThread = to;
    this.transition = time;
  }

  @Override
  public long getTransitionTime() {
    return transition;
  }

  private String getThreadType(Thread thread) {
    return thread.getThreadGroup().getName();
  }

  @Override
  public String getFromThreadType() {
    return getThreadType(fromThread);
  }

  @Override
  public String getToThreadType() {
    return getThreadType(toThread);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Thread from, to;
    private long startTime, stopTime;

    public ThreadNotificationService.ThreadNotificationElement build() {
      return new DefaultThreadNotificationElement(from, to, stopTime - startTime);
    }

    public Builder fromThread(Thread thread) {
      this.from = thread;
      this.startTime = System.nanoTime();
      return this;
    }

    public Builder toThread(Thread thread) {
      this.to = thread;
      this.stopTime = System.nanoTime();
      return this;
    }

    public Builder withStartingTime(int time) {
      this.startTime = time;
      return this;
    }

    public Builder withStopTime(int time) {
      this.stopTime = time;
      return this;
    }
  }

}
