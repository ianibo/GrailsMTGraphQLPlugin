package graphqlmt

import grails.core.GrailsClass
import grails.core.GrailsApplication
import grails.core.support.GrailsApplicationAware
import groovy.util.logging.Slf4j
import grails.util.GrailsNameUtils;

// import org.grails.datastore.mapping.model.PersistentEntity

import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.TypeRuntimeWiring;
import graphql.GraphQL
import groovy.util.logging.Slf4j

/**
 * because of changes in the grails ecosystem domain class introspection can't happen until after
 * gorm is initialised. This means code that used to execute in doWithSpring needs to move to doWithApplicationConext
 * This class holds the various GraphQL artefacts that need to be configured after applicationContext initialisation
 *
 * This class mediates between the static domain class definitions and the GraphQL schema definitions
 */
@Slf4j
class GraphqlConfigManager implements GrailsApplicationAware {

  GrailsApplication grailsApplication
  SDLFactory sdlFactory

  GraphQL graphQL;

  // See https://www.graphql-java.com/tutorials/getting-started-with-spring-boot/

  public void initialise() {
    log.debug("GraphqlConfigManager::initialise");
    TypeDefinitionRegistry tdl = sdlFactory.generate();
    RuntimeWiring runtimeWiring = buildWiring();
    SchemaGenerator schemaGenerator = new SchemaGenerator();
    GraphQLSchema gs = schemaGenerator.makeExecutableSchema(tdl, runtimeWiring);
    this.graphQL =  GraphQL.newGraphQL(gs).build();
  }

  private RuntimeWiring buildWiring() {
    RuntimeWiring.Builder rwb = RuntimeWiring.newRuntimeWiring()
 
    // Iterate through each domain class
    sdlFactory.domainClasses.each { key, value ->
      log.debug("Add PersistentClassDataFetcher for ${key} / ${value} / ${value.class} / ${value.getJavaClass()}");
      // rwb.type(newTypeWiring("Query").dataFetcher("find${key}UsingLQS".toString(), new PersistentClassDataFetcher<value.class>()))
      // rwb.type(RuntimeWiring.newTypeWiring("Query").dataFetcher("find${key}UsingLQS".toString(), new PersistentClassDataFetcher()))
      // rwb.type(RuntimeWiring.newTypeWiring("Query").dataFetcher("find${key}UsingLQS".toString(), (dataFetchingEnvironment) -> {
      //   println("Hello");
      // }
      // rwb.type( TypeRuntimeWiring.newTypeWiring("Query").dataFetcher("find${key}UsingLQS".toString(), new PersistentClassDataFetcher(value)) )

      Object graphql_config = grails.util.GrailsClassUtils.getStaticPropertyValue(value.getJavaClass(), 'graphql')
      if ( graphql_config != null ) {
        if ( graphql_config instanceof Map ) {
          log.debug("Class ${key} has static graphql config... process");
          graphql_config.queries.each { k,v -> 
            log.debug("Add query ${k} -> ${v}");
            rwb.type( TypeRuntimeWiring.newTypeWiring("Query").dataFetcher(k, new PersistentClassDataFetcher(value, v)) )
          }
        }
      }

      // The we support List graphql.defaultFinders as a list of default finders that should be added
      grailsApplication.config.graphql?.defaultFinders?.each { fm ->
        log.debug("Add finder method ${fm} to ${key}");
      }

      // See https://www.graphql-java.com/documentation/v13/execution/
      rwb.type( TypeRuntimeWiring.newTypeWiring("Mutation").dataFetcher("create${key}".toString(), new PersistentClassCreateMutation(value)) )
      rwb.type( TypeRuntimeWiring.newTypeWiring("Mutation").dataFetcher("update${key}".toString(), new PersistentClassUpdateMutation(value)) )
      rwb.type( TypeRuntimeWiring.newTypeWiring("Mutation").dataFetcher("delete${key}".toString(), new PersistentClassDeleteMutation(value)) )
    }

    return rwb.build();
  }

  public getGraphQL() {
    return graphQL;
  }
}
