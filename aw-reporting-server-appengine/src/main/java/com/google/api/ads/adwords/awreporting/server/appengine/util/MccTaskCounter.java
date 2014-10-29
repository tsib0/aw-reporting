//Copyright 2014 Google Inc. All Rights Reserved.
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.google.api.ads.adwords.awreporting.server.appengine.util;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

/**
 * This class is to monitor Tasks progress for an MCC
 *
 * @author jtoledo@google.com (Julian Toledo)
 */
public class MccTaskCounter {

  public static final String PENDING_PROCESS_TASKS = "NumberOfPendingProcessTasks";

  public static final String PENDING_EXPORT_TASKS = "NumberOfPendingExportTasks";
  
  public static final String PENDING_REFRESH_ACCOUNTS_TASKS = "NumberOfPendingRefreshAccountsTasks";

  private static volatile MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();


  public static synchronized int getPendingProcessTaks(Long topAccountId) {
    return (int) (long) syncCache.increment(PENDING_PROCESS_TASKS + topAccountId, 0, 0L);
  }

  public static synchronized int increasePendingProcessTasks(Long topAccountId) {
    return (int) (long) syncCache.increment(PENDING_PROCESS_TASKS + topAccountId, 1L, 0L); 
  }

  public static synchronized int increasePendingProcessTasks(Long topAccountId, int increment) {
    return (int) (long) syncCache.increment(PENDING_PROCESS_TASKS + topAccountId, increment, 0L);
  }

  public static synchronized int decreasePendingProcessTasks(Long topAccountId) {
    return (int) (long) syncCache.increment(PENDING_PROCESS_TASKS + topAccountId, -1L, 0L); 
  }


  public static synchronized int getPendingExportTaks(Long topAccountId) {
    return (int) (long) syncCache.increment(PENDING_EXPORT_TASKS + topAccountId, 0, 0L);
  }

  public static synchronized int increasePendingExportTasks(Long topAccountId) {
    return (int) (long) syncCache.increment(PENDING_EXPORT_TASKS + topAccountId, 1L, 0L); 
  }

  public static synchronized int increasePendingExportTasks(Long topAccountId, int increment) {
    return (int) (long) syncCache.increment(PENDING_EXPORT_TASKS + topAccountId, increment, 0L);
  }

  public static synchronized int decreasePendingExportTasks(Long topAccountId) {
    return (int) (long) syncCache.increment(PENDING_EXPORT_TASKS + topAccountId, -1L, 0L); 
  }
  

  public static synchronized int getPendingProcessAccountsTaks(Long topAccountId) {
    return (int) (long) syncCache.increment(PENDING_REFRESH_ACCOUNTS_TASKS + topAccountId, 0, 0L);
  }

  public static synchronized int increasePendingProcessAccountsTasks(Long topAccountId) {
    return (int) (long) syncCache.increment(PENDING_REFRESH_ACCOUNTS_TASKS + topAccountId, 1L, 0L); 
  }

  public static synchronized int increasePendingProcessAccountsTasks(Long topAccountId, int increment) {
    return (int) (long) syncCache.increment(PENDING_REFRESH_ACCOUNTS_TASKS + topAccountId, increment, 0L);
  }

  public static synchronized int decreasePendingProcessAccountsTasks(Long topAccountId) {
    return (int) (long) syncCache.increment(PENDING_REFRESH_ACCOUNTS_TASKS + topAccountId, -1L, 0L); 
  }
}
