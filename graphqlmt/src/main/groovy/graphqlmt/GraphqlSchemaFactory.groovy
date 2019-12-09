package graphqlmt

import grails.core.GrailsClass
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.GraphQLSchema;
import grails.core.GrailsApplication
import grails.core.support.GrailsApplicationAware
import groovy.util.logging.Slf4j
import graphql.language.ObjectTypeDefinition;
import graphql.schema.idl.RuntimeWiring;

@Slf4j
class GraphqlSchemaFactory implements GrailsApplicationAware {

  TypeDefinitionRegistry typeRegistry
  SchemaGenerator schemaGenerator
  GrailsApplication grailsApplication

  

  GraphQLSchema generate() {
    GraphQLSchema result = null;

    RuntimeWiring.Builder wiring_builder = RuntimeWiring.newRuntimeWiring()

    (grailsApplication.getArtefacts("Domain")).each {GrailsClass dc ->
      log.debug("registerDomainClass(${dc}) - ${dc.getName()}");
      // grailsApplication.mainContext.graphQLService.registerDomainClass(gc);
      def object_type_definition_builder = ObjectTypeDefinition.newObjectTypeDefinition()
      object_type_definition_builder.name(dc.getName())
      typeRegistry.add(object_type_definition_builder.build());
    }


    // https://github.com/graphql-java/graphql-java/blob/master/src/main/java/graphql/schema/idl/RuntimeWiring.java

    result = schemaGenerator.makeExecutableSchema(typeRegistry, wiring_builder.build());

    return result;
  }
}
