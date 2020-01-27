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
    log.debug("PersistentClassDataFetcher ${domainClass.class.name}");
    // log.debug("PersistentClassDataFetcher ${domainClass}/${environment}");

    // GormStaticApi staticApi = GormEnhancer.findStaticApi(domainClass.javaClass)

    log.debug("Call domainClass.list()");
    // def qr = staticApi.list()
    // result = domainClass.getJavaClass().wibble()
    result = [ 
               totalCount: 0,
               results: domainClass.getJavaClass().list()
             ];

    log.debug("get completetd with result: ${result}");
 
    return result;
  }

}
