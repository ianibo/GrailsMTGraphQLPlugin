package sptt

import javax.servlet.http.HttpServletRequest

import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.RequestAttributes
import org.grails.orm.hibernate.HibernateDatastore
import org.grails.datastore.mapping.core.exceptions.ConfigurationException
import groovy.sql.Sql
import javax.sql.DataSource
import org.grails.datastore.mapping.multitenancy.web.SessionTenantResolver

class TenantIdInterceptor {

  // Inject hibernate datastore
  HibernateDatastore hibernateDatastore
  // Inject the datasource
  DataSource dataSource

  int order = HIGHEST_PRECEDENCE + 100

  public TenantIdInterceptor() {
    matchAll()
  }

  boolean before() {
    log.debug("TenantIdInterceptor::before ${request.getHeader('X-TENANT')}");

    // See if this request has an X-OKAPI-TENANT header
    // If so, see if we have a hibernateDatastore for that tenant yet

    // HttpServletRequest httpServletRequest = getRequest()
    String tenantId = request.getHeader('X-TENANT')?.toLowerCase()?.trim()
    if ( tenantId ) {
      log.debug("Set gorm.tenantId attribute to ${tenantId}");
      // RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes()
      // requestAttributes.setAttribute('gorm.tenantId',tenantId,RequestAttributes.SCOPE_SESSION);
      // The page at https://guides.grails.org/database-per-tenant/guide/index.html suggests
      session.setAttribute(SessionTenantResolver.ATTRIBUTE, tenantId);

      // See if we have a datasource that corresponds to this tenantid
      validateTenant(tenantId);
    }
    else {
      log.warn("No X-TENANT header");
    }

    true
  }

  private validateTenant(String tenant) {
    try {
      log.debug("See if we already have a datastore for ${tenant}")
      hibernateDatastore.getDatastoreForConnection(tenant)
      log.debug("Got datastore already.. good");
    }
    catch ( ConfigurationException ce ) {
      // Create schema
      log.debug("Looks like ${tenant} does not exist yet as a schema - create it (${ce.message})");
      Sql sql = new Sql(dataSource as DataSource)
      sql.withTransaction {
          log.debug("Execute -- create schema ${tenant}");
          sql.execute("create schema if not exists ${tenant}" as String)
          log.debug("All done");
      }
      log.debug("Schema created ${tenant}");
      sql.close();

      log.debug("register datastore for schema (${tenant})");
      hibernateDatastore.addTenantForSchema(tenant)
    }
  }


  boolean after() { 
    true 
  }

  void afterView() {
    // no-op
  }
}
