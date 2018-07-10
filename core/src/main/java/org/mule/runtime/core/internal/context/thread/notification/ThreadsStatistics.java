/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.internal.context.thread.notification;

import org.apache.commons.math3.stat.StatUtils;
import org.mule.runtime.api.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.Math.sqrt;

public class ThreadsStatistics {

  private Map<Pair<String, String>, List<Long>> times = new HashMap<>();

  public synchronized void addThreadNotificationElement(ThreadNotificationService.ThreadNotificationElement notification) {
    Pair<String, String> key = new Pair<>(notification.getFromThreadType(), notification.getToThreadType());
    if (!times.containsKey(key)) {
      times.put(key, new ArrayList<>());
    }
    times.get(key).add(notification.getTransitionTime());
  }

  public synchronized void addThreadNotificationElements(List<ThreadNotificationService.ThreadNotificationElement> notifications) {
    notifications.stream().forEach(notification -> addThreadNotificationElement(notification));
  }

  public Set<Pair<String, String>> getPossibleTransitions() {
    return times.keySet();
  }

  private double[] longToDoubleArray(Long[] org) {
    double dst[] = new double[org.length];
    for (int i = 0; i < org.length; i++) {
      dst[i] = org[i];
    }
    return dst;
  }

  private double[] getTimes(String from, String to) {
    return longToDoubleArray(times.get(new Pair<>(from, to)).toArray(new Long[0]));
  }

  public double getMean(String from, String to) {
    return StatUtils.mean(getTimes(from, to));
  }

  public double getStdDeviation(String from, String to) {
    return sqrt(StatUtils.variance(getTimes(from, to)));
  }

  public double percentile(String from, String to, double quantile) {
    return StatUtils.percentile(getTimes(from, to), quantile);
  }

  public int getCount(String from, String to) {
    return getTimes(from, to).length;
  }

  public int getCount(Pair<String, String> transition) {
    return times.get(transition).size();
  }

  public double getMean(Pair<String, String> transition) {
    return getMean(transition.getFirst(), transition.getSecond());
  }

  public double getStdDeviation(Pair<String, String> transition) {
    return getStdDeviation(transition.getFirst(), transition.getSecond());
  }

  public double percentile(Pair<String, String> transition, double quantile) {
    return percentile(transition.getFirst(), transition.getSecond(), quantile);
  }
}
