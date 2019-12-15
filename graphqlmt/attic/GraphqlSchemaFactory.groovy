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
import graphql.schema.StaticDataFetcher;

import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLList
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

  Map typedefs = [:]
  

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
      registerTypeDefinition(dc);

      // b.type(GraphQLTypeReference.typeRef(dc.getName()))
      // We need to add a field to the query object for each domain class we wish to expose - given the Widget domain we may want to expose query { widget 
      // https://github.com/graphql-java/graphql-java/issues/1301
      queryType.field(newFieldDefinition()
                         .name("query${dc.getName()}")
                         .type(GraphQLList.list(GraphQLTypeReference.typeRef(dc.getName())))
                         .dataFetcher(new StaticDataFetcher("world!")));
    }

    queryType.field(newFieldDefinition()
                         .name("hello")
                         .type(graphql.Scalars.GraphQLString)
                         .dataFetcher(new StaticDataFetcher("world!")));

    log.debug("Completed read of all domain classes - build schema");


    // https://github.com/graphql-java/graphql-java/blob/master/src/main/java/graphql/schema/idl/RuntimeWiring.java
    result = GraphQLSchema.newSchema()
               .query(queryType)
               .mutation(mutationType)
               .build() // additionalTypes)


    // result = schemaGenerator.makeExecutableSchema(typeRegistry, wiring_builder.build());

    return result;
  }

  // see  https://github.com/graphql-java/graphql-java/blob/master/src/main/java/graphql/schema/idl/RuntimeWiring.java
  private GraphQLObjectType registerTypeDefinition(GrailsClass gc) {

    String typename = gc.getName()
    GraphQLObjectType result = typedefs.get(typename);

    // Return the type if we already know about it otherwise register it
    if ( result == null ) {
      result = newObject()
                 .name(typename)
                 .field(newFieldDefinition()
                         .name("id")
                         .type(graphql.Scalars.GraphQLString)
                         .dataFetcher(new StaticDataFetcher("world!"))
                 )
                 .build()
      typedefs[typename] = result;

      def type_definition = result.getDefinition();
      log.debug("Register type for ${typename} - instanceof ${type_definition?.class?.name}");

      // if ( type_definition != null ) {
      //   typeRegistry.add(type_definition);
      // }
      // else {
      //   log.error("Type null... Should not happen. Type was ${result?.class?.name}");
      // }
    }

    return result;
  }
}
