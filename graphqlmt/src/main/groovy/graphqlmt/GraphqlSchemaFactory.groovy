package graphqlmt

import grails.core.GrailsClass
import grails.core.GrailsApplication
import grails.core.support.GrailsApplicationAware
import groovy.util.logging.Slf4j
import graphql.language.ObjectTypeDefinition;
import graphql.language.FieldDefinition;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLTypeReference;

import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;

import static graphql.schema.GraphQLArgument.newArgument
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition
import static graphql.schema.GraphQLList.list
import static graphql.schema.GraphQLObjectType.newObject

@Slf4j
class GraphqlSchemaFactory implements GrailsApplicationAware {

  // https://javadoc.io/static/com.graphql-java/graphql-java/2019-10-21T00-35-45-a74776c/graphql/schema/idl/TypeDefinitionRegistry.html
  TypeDefinitionRegistry typeRegistry

  SchemaGenerator schemaGenerator
  GrailsApplication grailsApplication

  

  GraphQLSchema generate() {
    GraphQLSchema result = null;

    // RuntimeWiring.Builder wiring_builder = RuntimeWiring.newRuntimeWiring()

    GraphQLObjectType.Builder queryType = newObject().name('Query')
    GraphQLObjectType.Builder mutationType = newObject().name('Mutation')


    // see https://github.com/grails/gorm-graphql/blob/aabd1ca5bf904a9d6298fe63b6dbf427fbaab506/core/src/main/groovy/org/grails/gorm/graphql/Schema.groovy
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

      // Lets get the type definition for the given entity
      ObjectTypeDefinition objectType = getTypeDefinition(dc);

      GraphQLFieldDefinition.Builder b = new GraphQLFieldDefinition.Builder()
      b.name('wibblexyz')
      b.type(GraphQLTypeReference.typeRef(dc.getName()))
      // We need to add a field to the query object for each domain class we wish to expose - given the Widget domain we may want to expose query { widget 
      queryType.field(b.build());
    }


    // https://github.com/graphql-java/graphql-java/blob/master/src/main/java/graphql/schema/idl/RuntimeWiring.java
    result = GraphQLSchema.newSchema()
               .query(queryType)
               .mutation(mutationType)
               .build() // additionalTypes)


    // result = schemaGenerator.makeExecutableSchema(typeRegistry, wiring_builder.build());

    return result;
  }

  // see  https://github.com/graphql-java/graphql-java/blob/master/src/main/java/graphql/schema/idl/RuntimeWiring.java
  private ObjectTypeDefinition getTypeDefinition(GrailsClass gc) {

    ObjectTypeDefinition result = null;
    String typename = gc.getName()

    // Return the type if we already know about it otherwise register it
    java.util.Optional o = typeRegistry.getType(typename)
    if ( o.isPresent() ) {
      result = o.get();
    }
    else {
      // result = newObject()
      //           .name(typename)
      //           .field(newFieldDefinition()
      //                   .name("id")
      //                   .type(graphql.Scalars.GraphQLString)
      //           )
      //           .build().getDefinition();
      // https://javadoc.io/static/com.graphql-java/graphql-java/2019-10-21T00-35-45-a74776c/index.html?graphql/language/FieldDefinition.html
      ObjectTypeDefinition.Builder b = new ObjectTypeDefinition.Builder()
      result = b.name(typename)
                // .fieldDefinition( new FieldDefinition.Builder().name('id').type(graphql.Scalars.GraphQLString).build() )
                .build();
      typeRegistry.add(result);
    }

    return result;
  }
}
