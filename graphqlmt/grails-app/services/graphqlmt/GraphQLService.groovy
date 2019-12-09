package graphqlmt;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.StaticDataFetcher;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import grails.core.GrailsClass
import static graphql.schema.GraphQLObjectType.newObject
import graphql.schema.GraphQLObjectType
import graphql.language.ObjectTypeDefinition;


public class GraphQLService {

  SchemaGenerator schemaGenerator = null;
  TypeDefinitionRegistry typeRegistry = null;

  public GraphQLService() {
     schemaGenerator = new SchemaGenerator();
     typeRegistry = new TypeDefinitionRegistry();

     // GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, buildRuntimeWiring());
  }


  // https://github.com/grails/gorm-graphql/blob/master/core/src/main/groovy/org/grails/gorm/graphql/Schema.groovy
  //
  public void registerDomainClass(org.grails.core.DefaultGrailsDomainClass dc) {
    log.debug("registerDomainClass(${dc}) - ${dc.getName()}");

    def object_type_definition_builder = ObjectTypeDefinition.newObjectTypeDefinition()
    object_type_definition_builder.name(dc.getName())

    typeRegistry.add(object_type_definition_builder.build());

    // person.name("Person")
    // person.field(newFieldDefinition()
    //                 .name("friends")
    //                 .type(GraphQLList.list(GraphQLTypeReference.typeRef("Person"))))
    //         .build();


  }
  
}
