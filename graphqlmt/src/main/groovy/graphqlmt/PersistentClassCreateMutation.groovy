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

import grails.web.databinding.DataBindingUtils


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
    Object result = null;
    // log.debug("PersistentClassCreateMutation domain class: ${domainClass}");
    // environment
    String class_simple_name = domainClass.getJavaClass().getSimpleName()
    String param_name = class_simple_name.toLowerCase();
    if ( environment.containsArgument(param_name) ) {
      Object p = environment.getArgument(param_name)
      domainClass.getJavaClass().withTransaction {
        // now use data binding to map environment.param into result
        result = domainClass.newInstance()
        DataBindingUtils.bindObjectToInstance(result, p);
        result.save(flush:true, failOnError:true);
      }
    }
    else {
      log.error("unable to locate param ${param_name} in environment arguments ${environment.getArguments()}");
    }

    return result;
  }

}
