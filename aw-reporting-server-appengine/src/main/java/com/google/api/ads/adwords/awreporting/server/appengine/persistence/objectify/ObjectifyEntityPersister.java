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

package com.google.api.ads.adwords.awreporting.server.appengine.persistence.objectify;

import static com.googlecode.objectify.ObjectifyService.ofy;

import com.google.api.ads.adwords.awreporting.model.entities.Report;
import com.google.api.ads.adwords.awreporting.model.entities.ReportBase;
import com.google.api.ads.adwords.awreporting.model.persistence.EntityPersister;
import com.google.api.ads.adwords.awreporting.server.appengine.model.UserToken;
import com.google.common.collect.Maps;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.googlecode.objectify.cmd.Query;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AppEngine (Objectify) implementation of EntityPersister
 *
 * @author jtoledo@google.com (Julian Toledo)
 */
@Component
@Qualifier("objectifyEntityPersister")
public class ObjectifyEntityPersister implements EntityPersister, Serializable {

  private static final long serialVersionUID = 1L;
  
  protected ObjectifyEntityPersister() {
  }

  /**
   * @see com.google.api.ads.adwords.awreporting.model.persistence.EntityPersister
   *      #get(java.lang.Class, java.util.Map, int, int)
   */
  @Override
  public <T, V> List<T> get(Class<T> classT, Map<String, V> keyValueList, int numToSkip, int limit) {
    Query<T> query = ofy().load().type(classT);
    if (keyValueList != null) {
      for (String key : keyValueList.keySet()) {
        query = query.filter(key, keyValueList.get(key));
      }
    }    
    if (limit > 0) {
      query = query.limit(limit);
    }
    if (numToSkip > 0) {
      query = query.offset(numToSkip);
    }
    return query.list();
  }

  /**
   * @see com.google.api.ads.adwords.awreporting.model.persistence.EntityPersister
   *      #get(java.lang.Class, java.util.Map)
   */
  @Override
  public <T, V> List<T> get(Class<T> t, Map<String, V> keyValueList) {
    return get(t, keyValueList, 0, 0);
  }

  /**
   * @see com.google.api.ads.adwords.awreporting.model.persistence.EntityPersister
   *      #get(java.lang.Class, java.lang.String, java.lang.Object, int, int)
   */
  @Override
  public <T, V> List<T> get(Class<T> t, String key, V value, int numToSkip, int limit) {
    Map<String, V> keyValueList = new HashMap<String, V>();
    if (key != null && value != null) {
      keyValueList.put(key, value);
    }
    return get(t, keyValueList, numToSkip, limit);
  }

  /**
   * @see com.google.api.ads.adwords.awreporting.model.persistence.EntityPersister
   *      #get(java.lang.Class, java.lang.String, java.lang.Object)
   */
  @Override
  public <T, V> List<T> get(Class<T> t, String key, V value) {
    return get(t, key, value, 0, 0);
  }

  /**
   * Gets the entity list for the given report class, with a key in those values
   *
   * @param classT the report class
   * @param key the name of the property
   * @param values the values that meet key
   */
  @Override
  public <T, V> List<T> get(Class<T> classT, String key, List<V> values) {
    Query<T> query = ofy().load().type(classT);
    query = query.filter(key + " in", values);   
    return query.list();
  }

  /**
   * @see com.google.api.ads.adwords.awreporting.model.persistence.EntityPersister
   *      #get(java.lang.Class)
   */
  @Override
  public <T> List<T> get(Class<T> t) {
    return get(t, null, 0, 0);
  }

  /**
   * @see com.google.api.ads.adwords.awreporting.model.persistence.EntityPersister
   *      #get(java.lang.Class, int, int)
   */
  @Override
  public <T> List<T> get(Class<T> t, int numToSkip, int limit) {
    return get(t, null, numToSkip, limit);
  }

  /**
   * @see com.google.api.ads.adwords.awreporting.model.persistence.EntityPersister
   *      #get(java.lang.Class, java.lang.String, java.lang.Object, java.lang.String,
   *      java.util.Date, java.util.Date)
   */
  @Override
  public <T> List<T> get(Class<T> t,
      String key,
      Object value,
      String dateKey,
      Date dateStart,
      Date dateEnd) {
    return get(t, key, value, dateKey, dateStart, dateEnd, 0, 0);
  }

  /**
   * @see com.google.api.ads.adwords.awreporting.model.persistence.EntityPersister
   *      #get(java.lang.Class, java.lang.String, java.lang.Object, java.lang.String,
   *      java.util.Date, java.util.Date, int, int)
   */
  @Override
  public <T> List<T> get(Class<T> classT,
      String key,
      Object value,
      String dateKey,
      Date dateStart,
      Date dateEnd,
      int numToSkip,
      int limit) {    

    Query<T> query = ofy().load().type(classT);
    
    if (key != null) {
      query = query.filter(key, value);
    } 
    if (dateKey != null && dateStart != null) {
      if (dateEnd == null) { // One day only
        query = query.filter(dateKey, dateStart);
      } else { // All within the date range
        query = query.filter(dateKey + " >=", dateStart);
        query = query.filter(dateKey + " <=", dateEnd);
      }
    }
    
    if (limit > 0) {
      query = query.limit(limit);
    }
    if (numToSkip > 0) {
      query = query.offset(numToSkip);
    }
    return query.list();
  }

  @Override
  public <T> List<T> get(Class<T> classT,
      String key,
      Object value,
      String dateKey,
      String dateStart,
      String dateEnd) {

    Query<T> query = ofy().load().type(classT);
    
    if (key != null) {
      query = query.filter(key, value);
    } 
    if (dateKey != null && dateStart != null) {

      if (dateEnd == null) { // One day only
        query = query.filter(dateKey, dateStart);
      } else { // All within the date range
        query = query.filter(dateKey + " >=", dateStart);
        query = query.filter(dateKey + " <=", dateEnd);
      }
    }
    return query.list();
  }

  /**
   * @see com.google.api.ads.adwords.awreporting.model.persistence.EntityPersister
   *      #save(java.lang.Object)
   */
  @Override
  public <T> T save(T t) {
    T newT = null;
    if (t != null) {
      newT = (T) ofy().load().key(ofy().save().entity(t).now()).now();
    }
    return newT;
  }

  /**
   * @see com.google.api.ads.adwords.awreporting.model.persistence.EntityPersister
   *      #save(java.util.List)
   */
  @Override
  public <T> void save(List<T> listT) {
    if (listT != null && listT.size() > 0) {
      ofy().save().entities(listT).now();
    }
  }

  /**
   * @see com.google.api.ads.adwords.awreporting.model.persistence.EntityPersister
   *      #remove(java.lang.Object)
   */
  @Override
  public <T> void remove(T t) {
    if (t != null) {
      ofy().delete().entity(t);
    }
  }

  /**
   * @see com.google.api.ads.adwords.awreporting.model.persistence.EntityPersister
   *      #remove(java.util.Collection)
   */
  @Override
  public <T> void remove(Collection<T> listT) {
    if (listT != null && listT.size() > 0) {
      ofy().delete().entities(listT);
    }
  }

  /**
   * Removes the collection of entities by key,value
   *
   * @param classT the entity T class
   * @param key the property name
   * @param value the property value
   */
  @Override
  public <T, V> void remove(Class<T> classT, String key, V value) {
    remove(get(classT, key, value));
  }

  /**
   * Removes the collection of entities by key,values
   *
   * @param classT the entity T class
   * @param key the property name
   * @param a list of values
   */
  @Override
  public <T, V> void remove(Class<T> classT, String key, List<V> values) {
    remove(get(classT, key, values));
  }

  /**
   * Adds a field as a DB index
   *
   * @param classT the entity T class
   */
  @Override
  public <T> void createIndex(Class<T> classT, String key) {
    //TODO: check indexes
  }

  /**
   * Adds a list of fields as DB indexes
   *
   * @param classT the entity T class
   */
  @Override
  public <T> void createIndex(Class<T> classT, List<String> keys) {
    //TODO: check indexes
  }

  /**
   * @see com.google.api.ads.adwords.awreporting.model.persistence.EntityPersister#
   * persistReportEntities(java.util.List)
   */
  @Override
  public void persistReportEntities(List<? extends Report> reportEntities) {
    this.save(reportEntities);
  }

  /**
   * @see com.google.api.ads.adwords.awreporting.model.persistence.EntityPersister#
   * listReports(java.lang.Class)
   */
  @Override
  public <T extends Report> List<T> listReports(Class<T> classT) {
    return this.get(classT);
  }

  /**
   * @see com.google.api.ads.adwords.awreporting.model.persistence.EntityPersister#
   * removeReportEntities(java.util.Collection)
   */
  @Override
  public void removeReportEntities(Collection<? extends Report> reportEntities) {
    this.remove(reportEntities);
  }

  /**
   *
   * @see com.google.api.ads.adwords.awreporting.model.persistence.
   *      EntityPersister#listMonthReports(Class, long, DateTime, DateTime)
   */
  @Override
  public <T extends Report> List<T> listMonthReports(
      Class<T> classT, long accountId, DateTime startDate, DateTime endDate) {

   Query<T> query = ofy().load().type(classT);
   query = query.filter(Report.ACCOUNT_ID, accountId);
   query = query.order(ReportBase.MONTH); 

   query = query.filter(ReportBase.MONTH + " >=", startDate.toDate());
   query = query.filter(ReportBase.MONTH + " <=", endDate.toDate());
   
    return query.list();
  }

  /**
   * @see com.google.api.ads.adwords.awreporting.model.persistence.
   *      EntityPersister#listMonthReports(Class, long, DateTime, DateTime, int, int)
   */
  @Override
  public <T extends Report> List<T> listMonthReports(Class<T> classT,
      long accountId, DateTime startDate, DateTime endDate, int numToSkip, int limit) {

    Query<T> query = ofy().load().type(classT);
    query = query.filter(Report.ACCOUNT_ID, accountId);
    query = query.order(ReportBase.MONTH); 

    query = query.filter(ReportBase.MONTH + " >=", startDate.toDate());
    query = query.filter(ReportBase.MONTH + " <=", endDate.toDate());

     if (limit > 0) {
       query = query.limit(limit);
     }
     if (numToSkip > 0) {
       query = query.offset(numToSkip);
     }
     return query.list();
  }

  public <T extends ReportBase> Map<String, Object> getReportDataAvailableByDate(Class<T> classT, long topAccountId, String dateKey) {
    Map<String, Object> map = Maps.newHashMap();

    if (!accountExists(topAccountId)) {      
      map.put("error", "invalid_params");
      map.put("message", "The requested MCC does not exist in the database.");

    } else {

      T tMin = ofy().load().type(classT).order(dateKey).filter(ReportBase.TOP_ACCOUNT_ID, topAccountId).first().now();

      T tMax = ofy().load().type(classT).order("-" + dateKey).filter(ReportBase.TOP_ACCOUNT_ID, topAccountId).first().now();

      if (tMax != null && tMin != null) {
        map.put("ReportType", classT.getSimpleName());

        if (dateKey.equalsIgnoreCase(ReportBase.MONTH)) {
          map.put("startMonth", tMin.getMonth());
          map.put("endMonth", tMax.getMonth());
        }
        if (dateKey.equalsIgnoreCase(ReportBase.DAY)) {
          map.put("startMonth", tMin.getDay());
          map.put("endMonth", tMax.getDay());
        }
      }
    }
    return map;
  }

  /**
   * Checks if the account exists in the datastore.  This method does NOT validate an CID against AdWords.
   * @param topAccountId
   * @return true if account exists in datastore
   */
  private boolean accountExists(long topAccountId) {
    List<UserToken> list = get(UserToken.class, UserToken.TOP_ACCOUNT_ID, topAccountId);
    if(list != null && !list.isEmpty()) {
      return true;
    }
    else {
      return false;
    }
  }
}
