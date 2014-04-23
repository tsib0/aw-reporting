package com.google.api.ads.adwords.jaxws.extensions.kratu.data;

import com.google.api.ads.adwords.jaxws.extensions.processors.ReportProcessor;
import com.google.api.ads.adwords.jaxws.extensions.report.model.util.DateUtil;
import com.google.api.ads.adwords.lib.jaxb.v201402.ReportDefinitionDateRangeType;

import org.apache.log4j.Logger;

import java.util.Date;
import java.util.Properties;


public class RunnableReport implements Runnable {

  private static final Logger LOGGER = Logger.getLogger(RunnableReport.class);

  private Long topAccountId;
  private ReportProcessor processor;
  private Properties properties;
  private Date dateStart;
  private Date dateEnd;

  public RunnableReport(Long topAccountId, ReportProcessor processor, Properties properties, Date dateStart, Date dateEnd) {
    super();
    this.topAccountId = topAccountId;
    this.processor = processor;
    this.properties = properties;
    this.dateStart = dateStart;
    this.dateEnd = dateEnd;
  }

  public void run() {
    try {
      
      LOGGER.debug("Creating ReportProcessor bean...");
      
      /*
      String userId, String mccAccountId,
      ReportDefinitionDateRangeType dateRangeType, String dateStart,
      String dateEnd, Set<Long> accountIdsSet, Properties properties)
      */

      processor.generateReportsForMCC(null, String.valueOf(topAccountId), ReportDefinitionDateRangeType.CUSTOM_DATE,
          DateUtil.formatYearMonthDayNoDash(dateStart), DateUtil.formatYearMonthDayNoDash(dateEnd), null, properties);

      /*
      AwReporting.main(new String[]{"-file", file,
          "-startDate", DateUtil.formatYearMonthDayNoDash(dateStart),
          "-endDate", DateUtil.formatYearMonthDayNoDash(dateEnd)});
      */

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
