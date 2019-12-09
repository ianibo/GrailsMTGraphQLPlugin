package graphqlmt

import grails.core.GrailsClass
import grails.core.GrailsApplication
import grails.core.support.GrailsApplicationAware
import groovy.util.logging.Slf4j
import graphql.language.ObjectTypeDefinition;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;

import static graphql.schema.GraphQLArgument.newArgument
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition
import static graphql.schema.GraphQLList.list
import static graphql.schema.GraphQLObjectType.newObject


@Slf4j
class GraphqlSchemaFactory implements GrailsApplicationAware {

  TypeDefinitionRegistry typeRegistry
  SchemaGenerator schemaGenerator
  GrailsApplication grailsApplication

  

  GraphQLSchema generate() {
    GraphQLSchema result = null;

    // RuntimeWiring.Builder wiring_builder = RuntimeWiring.newRuntimeWiring()

    GraphQLObjectType.Builder queryType = newObject().name('Query')
    GraphQLObjectType.Builder mutationType = newObject().name('Mutation')


    (grailsApplication.getArtefacts("Domain")).each {GrailsClass dc ->
      log.debug("registerDomainClass(${dc}) - ${dc.getName()}");
      // grailsApplication.mainContext.graphQLService.registerDomainClass(gc);
      // def object_type_definition_builder = ObjectTypeDefinition.newObjectTypeDefinition()
      // object_type_definition_builder.name(dc.getName())
      // typeRegistry.add(object_type_definition_builder.build());

      //GraphQLFieldDefinition.Builder queryOne = newFieldDefinition()
      //                           .name("get${dc.getName()}".toString())
      //                           .type(objectType)
      //                           .description(getOperation.description)
      //                           .dataFetcher(new InterceptingDataFetcher(entity, serviceManager, queryInterceptorInvoker, GET, getFetcher))

    }


    // https://github.com/graphql-java/graphql-java/blob/master/src/main/java/graphql/schema/idl/RuntimeWiring.java
    result = GraphQLSchema.newSchema()
               .query(queryType)
               .mutation(mutationType)
               .build() // additionalTypes)


    // result = schemaGenerator.makeExecutableSchema(typeRegistry, wiring_builder.build());

    return result;
  }
}
