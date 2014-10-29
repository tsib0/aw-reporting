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

import org.joda.time.DateTime;

/**
 * Utility methods for validating and processing rest requests & responses
 * 
 * @author joeltoby.com (Joel Toby)
 */
public class DateUtility {

  /**
   * Get a DateTime for the first day of the previous month.
   * @return DateTime
   */
  public static DateTime firstDayPreviousMonth() {
    return new DateTime().minusMonths(1).dayOfMonth().withMinimumValue();
  }

  /**
   * Get a DateTime for the last day of the previous month.
   * @return DateTime
   */
  public static DateTime lastDayPreviousMonth() {
    return new DateTime().minusMonths(1).dayOfMonth().withMaximumValue();
  }

  /**
   * Get a DateTime for the first day of a month.
   * @return DateTime
   */
  public static DateTime firstDayMonth(DateTime datetime) {
    return datetime.dayOfMonth().withMinimumValue();
  }

  /**
   * Get a DateTime for the last day of a month.
   * @return DateTime
   */
  public static DateTime lastDayMonth(DateTime datetime) {
    return datetime.dayOfMonth().withMaximumValue();
  }
}

