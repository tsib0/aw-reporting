// Copyright 2014 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.api.ads.adwords.awreporting.util;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Utility class for generating or formatting values for presentation within exported reports 
 * such as currencies, dates and other numeric values.
 *
 * @author joeltoby
 */
public final class TemplateStringsUtil {

  protected static final String DATE_FORMAT_FULL_MONTH_YEAR_TEXT = "MMMMM yyyy";
  private static final DateTimeFormatter dateFormatterFullMonthYear =
      DateTimeFormat.forPattern(DATE_FORMAT_FULL_MONTH_YEAR_TEXT);
  
  protected static final String DATE_FORMAT_ABRIEVIATED_MONTH_YEAR_TEXT = "MMM yyyy";
  private static final DateTimeFormatter dateFormatterAbrieviatedMonthYear =
      DateTimeFormat.forPattern(DATE_FORMAT_ABRIEVIATED_MONTH_YEAR_TEXT);

  /**
   * Private constructor.
   */
  private TemplateStringsUtil() {}
  
  /*
   * Date formating methods
   */
  
  /**
   * Convenience method to format a date range in monthly format (e.g. March 2015 - April 2016).
   * If start and end months are the same, just one month will is returned (March 2015)
   * 
   * @param startDate the date range start date as a {@code java.util.Date}
   * @param endDate the date range end date as a {@code java.util.Date}
   * @return
   */
  public static String formatFullMonthDateRange(Date startDate, Date endDate) {
	  return TemplateStringsUtil.formatFullMonthDateRange(new DateTime(startDate), new DateTime(endDate));
  }
  
  /**
   * Convenience method to format a date range in monthly format (e.g. March 2015 - April 2016).
   * If start and end months are the same, just one month will is returned (March 2015)
   * 
   * @param startDate the date range start date as a {@code org.jodatime.DateTime}
   * @param endDate the date range end date as a {@code org.jodatime.DateTime}
   * @return
   */
  public static String formatFullMonthDateRange(DateTime startDate, DateTime endDate) {
	  String formattedStartDate = formatDateFullMonthYear(startDate);
	  String formattedEndDate = formatDateFullMonthYear(endDate);
	  String formattedDateRange = "";
	  
	  if(formattedStartDate.equals(formattedEndDate)) {
		  formattedDateRange = formattedStartDate;
	  } else {
		  formattedDateRange = formattedStartDate + " - " + formattedEndDate;
	  }
	  
	  return formattedDateRange;
  }

  /**
   * Formats the date to the format: MMMM yyyy (e.g. 'March 2015')
   *
   * @param date the date as a {@code java.util.Date}
   * @return the {@code String} representing the formatted date
   */
  public static String formatDateFullMonthYear(Date date) {
    return TemplateStringsUtil.formatDateFullMonthYear(new DateTime(date));
  }

  /**
   * Formats the date to the format: MMMM yyyy (e.g. 'March 2015')
   *
   * @param date the date as a {@code org.jodatime.DateTime}
   * @return the {@code String} representing the formatted date
   */
  public static String formatDateFullMonthYear(DateTime date) {
    return TemplateStringsUtil.dateFormatterFullMonthYear.print(date);
  }
  
  /**
   * Formats the date to the format: MMM yyyy (e.g. 'Mar 2015')
   *
   * @param date the date as a {@code java.util.Date}
   * @return the {@code String} representing the formatted date
   */
  public static String formatDateAbrieviatedMonthYear(Date date) {
    return TemplateStringsUtil.formatDateAbrieviatedMonthYear(new DateTime(date));
  }

  /**
   * Formats the date to the format: MMMM yyyy (e.g. 'March 2015')
   *
   * @param date the date as a {@code org.jodatime.DateTime}
   * @return the {@code String} representing the formatted date
   */
  public static String formatDateAbrieviatedMonthYear(DateTime date) {
    return TemplateStringsUtil.dateFormatterAbrieviatedMonthYear.print(date);
  }
}