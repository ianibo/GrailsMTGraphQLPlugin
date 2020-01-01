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
 *
 * Mutations are just ordinary DataFetchers that use environment.getArgument to modify an object and return an object
 */
@Slf4j
class PersistentClassCreateMutation implements DataFetcher {

  private org.grails.datastore.mapping.model.PersistentEntity domainClass = null;

  public PersistentClassCreateMutation() {
  }

  public PersistentClassCreateMutation(org.grails.datastore.mapping.model.PersistentEntity domainClass) {
    this.domainClass = domainClass;
  }

  public Object get(DataFetchingEnvironment environment) {
    Map result = null;
    // println("PersistentClassDataFetcher::get(${environment})");
    log.debug("PersistentClassCreateMutation ${domainClass.class.name}");
    // log.debug("PersistentClassDataFetcher ${domainClass}/${environment}");

    // GormStaticApi staticApi = GormEnhancer.findStaticApi(domainClass.javaClass)

    log.debug("Call domainClass.list()");
    // def qr = staticApi.list()
    // result = domainClass.getJavaClass().wibble()
    result = domainClass.newInstance()

    // now use data binding to map environment.p_classname into result

    log.debug("get completetd with result: ${result}");
    return result;
  }

}
