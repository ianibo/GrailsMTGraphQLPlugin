package sptt

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.*
import spock.lang.Specification
import spock.lang.Stepwise
import geb.spock.GebSpec
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import groovyx.net.http.HttpBuilder
import groovyx.net.http.FromServer
import static groovyx.net.http.ContentTypes.JSON
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import grails.gorm.multitenancy.Tenants


// See http://grails.github.io/grails-http/latest/

@Integration
@Rollback
@Stepwise
class LifecycleSpec extends Specification {

  final static String baseUrl = 'http://localhost:8080';
  final static Logger logger = LoggerFactory.getLogger(LifecycleSpec.class);

 @Value('${local.server.port}')
 Integer serverPort

  def setup() {
  }

  def cleanup() {
  }

  void "test tenant creation"(tenantid,name) {

    when:"We post a new tenant request to the admin controller"

      logger.debug("Post new tenant request for ${tenantid} to ${baseUrl}/admin/createTenant");

      String status = null;

      def httpBin = HttpBuilder.configure {
        request.uri = 'http://localhost:'+serverPort
      }

      def result = httpBin.get {
        request.uri.path = '/admin/createTenant'
        request.uri.query = [tenantId:tenantid]
        response.when(200) { FromServer fs, Object body ->
          logger.debug("createTenant returns 200 ${body}");
          status='OK'
        }
      }
      logger.debug("Result: ${result}");

    then:"The response is correct"
      status=='OK'

    where:
      tenantid | name
      'TestTenantG' | 'TestTenantG'


  }

  void "test widget creation"(tenantid, widgetName) {
    when:"We post a new tenant request to the admin controller"

      logger.debug("Post new widget (${widgetName}) for tenant ${tenantid}");

      String status = null;

      def httpBin = HttpBuilder.configure {
        request.uri = 'http://localhost:'+serverPort
        request.headers['X-TENANT'] = tenantid
      }

      def result = httpBin.get {
        request.uri.path = '/widget/createWidget'
        request.uri.query = [name:widgetName]
        response.when(200) { FromServer fs, Object body ->
          logger.debug("createWidget returns 200 ${body}");
          status='OK'
        }
      }
      logger.debug("Result: ${result}");

    then:"The response is correct"
      status=='OK'

    where:
      tenantid | widgetName
      'TestTenantG' | 'Test Widget A Tenant G'
      'TestTenantG' | 'Test Widget B Tenant G'
      'TestTenantG' | 'Test Widget C Tenant G'
      'TestTenantG' | 'Test Widget D Tenant G'
      'TestTenantG' | 'Test Widget E Tenant G'
      'TestTenantF' | 'Test Widget A Tenant F'
  }

  void "test Graphql Widget"(tenantid, qry) {
    when:"We post a new tenant request to the admin controller"

      logger.debug("graphql query (${qry}) for tenant ${tenantid}");

      String status = null;

      def httpBin = HttpBuilder.configure {
        request.uri = 'http://localhost:'+serverPort
        request.headers['X-TENANT'] = tenantid
      }

      def result = httpBin.post {
        request.uri.path = '/graphql'
        request.headers.'accept'='application/json'
        // request.headers.'Content-Type'='application/json'
        request.contentType = JSON[0]
        request.body = [
          'query': "query { findWidgetUsingLQS(luceneQueryString:\"title:${qry}\") { totalCount results { widgetName } } }".toString(),
          'variables':[:]
        ]
        response.when(200) { FromServer fs, Object body ->
          logger.debug("graphql query returns 200 ${body}");
          // TestTenantG should have 4 widgets
          assert body.data.findWidgetUsingLQS.totalCount==0
          assert body.data.findWidgetUsingLQS.results.size==5
          status='OK'
        }
      }
      logger.debug("Result: ${result}");

    then:"The response is correct"
      status=='OK'

    where:
      tenantid | qry
      'TestTenantG' | 'test'
  }

  void "test Create Mutation for Widget"(tenantid, record) {
    when:"We post a new widget mutation"

      logger.debug("graphql mutation (${record}) for tenant ${tenantid}");

      String status = null;

      def httpBin = HttpBuilder.configure {
        request.uri = 'http://localhost:'+serverPort
        request.headers['X-TENANT'] = tenantid
      }

      def result = httpBin.post {
        request.uri.path = '/graphql'
        request.headers.'accept'='application/json'
        // request.headers.'Content-Type'='application/json'
        request.contentType = JSON[0]
        request.body =  [
          // "query" : 'mutation($widget: WidgetInputType) { createWidget(widget: $widget) { id widgetName errors { field message } } }',
          "query" : 'mutation($widget: WidgetInputType) { createWidget(widget: $widget) { id widgetName lines { widgetLineText } } }',
          "variables": [
            "widget" : record
          ]
        ]
    
        response.when(200) { FromServer fs, Object body ->
          logger.debug("graphql mutation returns 200 ${body}");
          assert body.data.createWidget.widgetName == record.widgetName
          assert body.data.createWidget.id != null
          status='OK'
        }
      }
      logger.debug("Result: ${result}");

    then:"The response is correct"
      status=='OK'

    where:
      tenantid | record
      'TestTenantG' | [ widgetName: 'Widget 334 - From createWidget mutation - TennantG' ]
      'TestTenantF' | [ widgetName: 'Widget 335 - From createWidget mutation - TennantF', lines: [ [ widgetLineText:'Widget Line text 335' ] ] ]
  }

  void "test lucene finder works"(tenantid, qry, expected_count) {

    def n = 0;

    when:"We find by lucene query"
      Tenants.withId(tenantid.toLowerCase()) {
        def wl = Widget.findAllByLuceneQueryString(qry)
        log.debug("Got result ${wl}")
        n = wl.totalCount
      }

    then:"The response is correct"
      n == expected_count

    where:
      tenantid | qry | expected_count
      'TestTenantG' | 'widgetName:Widget' | 6
  }

  void "test Lucene query finder"(tenantid, qry) {
    when:"We post a new tenant request to the admin controller"

      logger.debug("\n\ngraphql query (${qry}) for tenant ${tenantid}");

      String status = null;

      def httpBin = HttpBuilder.configure {
        request.uri = 'http://localhost:'+serverPort
        request.headers['X-TENANT'] = tenantid
      }

      def result = httpBin.post {
        request.uri.path = '/graphql'
        request.headers.'accept'='application/json'
        // request.headers.'Content-Type'='application/json'
        request.contentType = JSON[0]
        request.body = [
          'query': "query { findWidgetByLuceneQuery(luceneQueryString:\"title:${qry}\") { totalCount results { widgetName } } }".toString(),
          'variables':[:]
        ]
        response.when(200) { FromServer fs, Object body ->
          logger.debug("graphql query returns 200 ${body}");
          // TestTenantG should have 4 widgets
          assert body.data.findWidgetByLuceneQuery.totalCount==0
          assert body.data.findWidgetByLuceneQuery.results.size==5
          status='OK'
        }
      }
      logger.debug("Result: ${result}");

    then:"The response is correct"
      status=='OK'

    where:
      tenantid | qry
      'TestTenantG' | 'test'
  }


}
