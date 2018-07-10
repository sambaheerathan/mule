/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.internal.context.thread.notification;

import java.util.List;

import static org.mule.runtime.core.api.config.MuleProperties.MULE_ACTIVATE_THREAD_CHANGE_LOGGING;

public interface ThreadNotificationService {

  String REGISTRY_KEY = "_muleThreadNotificationService";
  boolean THREAD_LOGGING = Boolean.getBoolean(MULE_ACTIVATE_THREAD_CHANGE_LOGGING);

  void addThreadNotificationElement(ThreadNotificationElement notification);

  void addThreadNotificationElements(List<ThreadNotificationElement> notifications);

  interface ThreadNotificationElement {

    long getTransitionTime();

    String getFromThreadType();

    String getToThreadType();

  }

}
