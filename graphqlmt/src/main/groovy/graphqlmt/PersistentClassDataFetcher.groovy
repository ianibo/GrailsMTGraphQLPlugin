package graphqlmt

import grails.core.GrailsClass
import grails.core.GrailsApplication
import grails.core.support.GrailsApplicationAware
import groovy.util.logging.Slf4j
import grails.util.GrailsNameUtils;

import org.grails.datastore.mapping.model.PersistentEntity

import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment;

import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.gorm.GormStaticApi

import grails.gorm.multitenancy.Tenants

/**
 * because of changes in the grails ecosystem domain class introspection can't happen until after
 * gorm is initialised. This means code that used to execute in doWithSpring needs to move to doWithApplicationConext
 * This class holds the various GraphQL artefacts that need to be configured after applicationContext initialisation
 * @See https://github.com/graphql-java/graphql-java/blob/master/src/main/java/graphql/schema/DataFetcher.java
 */
@Slf4j
class PersistentClassDataFetcher implements DataFetcher {

  private org.grails.datastore.mapping.model.PersistentEntity domainClass = null;
  private Map config = null;

  public PersistentClassDataFetcher() {
  }

  public PersistentClassDataFetcher(org.grails.datastore.mapping.model.PersistentEntity domainClass) {
    log.debug("PersistentClassDataFetcher::PersistentClassDataFetcher(${domainClass})");
    this.domainClass = domainClass;
    this.config = [:];
  }

  public PersistentClassDataFetcher(org.grails.datastore.mapping.model.PersistentEntity domainClass, Map config) {
    log.debug("PersistentClassDataFetcher::PersistentClassDataFetcher(${domainClass},${config})");
    this.domainClass = domainClass;
    this.config = config;
  }

  public Object get(DataFetchingEnvironment environment) {
    Map result = null;
    // println("PersistentClassDataFetcher::get(${environment})");
    log.debug("PersistentClassDataFetcher ${domainClass.class.name} - tenant = ${Tenants.currentId()}");
    log.debug("PersistentClassDataFetcher ${domainClass}/${environment?.arguments}");

    if ( config.methodName != null ) {
      Map params = [
        max:environment?.arguments?.max?:10,
        offset: environment?.arguments?.offset?:0,
        sort: environment?.arguments?.sort,
        order: environment?.arguments?.order
      ]

      log.debug("config method name is ${config.methodName} / ${params}");
      List method_call_args=[]
      config.args?.each { arg ->
        method_call_args.add(environment?.arguments[arg.param_name])
      }
      log.debug("Calculated param list: ${method_call_args}");

      def tr = domainClass.getJavaClass()."${config.methodName}"(*method_call_args, params)
      log.debug("Result is ${tr} / ${tr?.class.name}");

      result = [
        offset:params.offset,
        max:params.max,
        totalCount:tr?.totalCount?:0,
        results: tr
      ]
    }
    else {
      log.debug("Call domainClass.list()");
      // def qr = staticApi.list()
      // result = domainClass.getJavaClass().wibble()
      result = [ 
                 totalCount: 0,
                 results: domainClass.getJavaClass().list()
               ];
    }

    log.debug("get completetd with result: ${result}");
 
    return result;
  }

}
