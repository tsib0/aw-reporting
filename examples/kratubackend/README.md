# AwReporting

## Overview

KratuBackend is an AdWords account portfolio analysis tool, it analyzes more than 30 key signals to discover and prioritize opportunities for optimizations across large sets of AdWords accounts for all of our managed customers using as a Frontend UI the open source Kratu Project. It offers a Rest backend for the Kratu project over MongoDB/MySql and it reuses the project AwReporting.


## Quick Start 

### Prerequisites

KratuBackend can be compiled using Maven by executing the following on the command line: 

$ mvn compile dependency:copy-dependencies package


## Usage

Download Reports:
java -Xmx4G -jar kratubackend.jar -startDate YYYYMMDD -endDate YYYYMMDD -file <file>
 
Process Kratus:
java -Xmx4G -jar kratubackend.jar -processKratus -startDate YYYYMMDD -endDate YYYYMMDD -file <file>

Start Rest Server:
java -Xmx4G -jar kratubackend.jar -startServer -file <file>

 -dateRange <DateRangeType>   ReportDefinitionDateRangeType
 -endDate <YYYMMDD>           End date for CUSTOM_DATE Reports (YYYYMMDD)
 -file <file>                 aw-report-sample.properties file (./aw-report-sample.properties by default if not
                              provided)
 -help                        print this message
 -processKratus               Process Kratus processes the 7 reports peraccount and creates a daily Kratu
 -startDate <YYYYMMDD>        Start date for CUSTOM_DATE Reports (YYYYMMDD)
 -startServer                 Starts the Rest Server. No dates required


## Server

Index web page, with more information:
  http://localhost:8081/index.html

Kratu Report:
  http://localhost:8081/kratureport/index.html?dateStart=20130101&dateEnd=20130331

Genereate Reports:
  http://localhost:8081/generatereports/?dateStart=20130101&dateEnd=20130331

Genereate Kratus:
  http://localhost:8081/generatekratus/?dateStart=20130101&dateEnd=20130331

### Fine print
Pull requests are very much appreciated. Please sign the [Google Code contributor license agreement](http://code.google.com/legal/individual-cla-v1.0.html) (There is a convenient online form) before submitting.

<dl>
  <dt>Authors</dt><dd><a href="https://plus.google.com/+JulianCToledo/">Julian Toledo (Google Inc.)
  <dt>Copyright</dt><dd>Copyright © 2013 Google, Inc.</dd>
  <dt>License</dt><dd>Apache 2.0</dd>
  <dt>Limitations</dt><dd>This is example software, use with caution under your own risk.</dd>
</dl>
