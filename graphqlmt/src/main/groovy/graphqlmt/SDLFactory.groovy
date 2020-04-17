package graphqlmt

import grails.core.GrailsClass
import grails.core.GrailsApplication
import grails.core.support.GrailsApplicationAware
import groovy.util.logging.Slf4j
import grails.util.GrailsNameUtils;

import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty

import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;


/**
 * Dynamically generate an SDL description of the domain classes
 *
 * Create a contract which describes the domain classes.
 * It's not clear what the relationship is between GraphQLObjectType and ObjectTypeDefinition when doing programmatic
 * schema generation. However - graphql-java seems really comfortable with SDL. This class solves the ambiguity by 
 * creating an SDL defintion that graphql-java seems more comfortable with.
 */
@Slf4j
class SDLFactory implements GrailsApplicationAware {

  GrailsApplication grailsApplication

  public Map domainClasses = [:]


  TypeDefinitionRegistry generate() {
    // https://github.com/graphql-java/graphql-java/blob/master/src/main/java/graphql/schema/idl/TypeDefinitionRegistry.java
    TypeDefinitionRegistry result = null;
    String sdl = buildSDL();
    log.debug("Generated schema:\n\n${sdl}\n\nParsing...\n");
    SchemaParser sp = new SchemaParser();
    result = sp.parse(sdl);
    return result;
  }

  public String buildSDL() {

    // (grailsApplication.getArtefacts("Domain")).each {GrailsClass dc ->
    (grailsApplication.mappingContext.getPersistentEntities()).each {PersistentEntity dc ->
      log.debug("registerDomainClass(${dc}) - ${dc.getJavaClass().getName()}");
      domainClasses.put(dc.getJavaClass().getSimpleName(), dc);
    }

    StringWriter sw = new StringWriter();
    //schema {
    //  query: Query
    //}
    sw.write('''
type Query {
'''+buildQueryTypeFields()+'''}

type Mutation {
'''+buildMutations()+''' }

type Error {
  field: String
  message: String
}

type DeleteResult {
  result: String
}

'''+buildTypeDefinitions())
    return sw.toString();
  }

  public buildMutations() {
    StringWriter sw = new StringWriter();
    domainClasses.each { key, value ->
      sw.write("  create${key}(${key.toLowerCase()}: ${key}InputType) : ${key}\n");
      sw.write("  update${key}(id: String) : ${key}\n");
      sw.write("  delete${key}(id: String) : DeleteResult\n");
    }
    return sw.toString();
  }

  /**
   * iterate over domain classes and build any finder methods we want to expose to the query type
   * SeeAlso https://atheros.ai/blog/graphql-list-how-to-use-arrays-in-graphql-schema
   */
  public buildQueryTypeFields() {
    StringWriter sw = new StringWriter();
    domainClasses.each { key, value ->
      // sw.write("  find${key}UsingLQS(luceneQueryString: String) : ${key}PagedResult\n");

      Object graphql_config = grails.util.GrailsClassUtils.getStaticPropertyValue(value.getJavaClass(), 'graphql')
      if ( graphql_config != null ) {
        if ( graphql_config instanceof Map ) {
          log.debug("Class (${key}) has static graphql config");
          graphql_config.queries.each { k,v ->
            log.debug("Adding ${k}(luceneQueryString: String) : ${key}PagedResult\n");

            //ToDo: This needs to be String not "class java.lang.String"
            String args = v.args.collect{"${it.param_name}: ${getTypeNameFor(it.type)}"}.join(', ')
            log.debug("----- ${k}(${args}) : ${key}PagedResult");
            sw.write("  ${k}(${args}) : ${key}PagedResult\n");
            // sw.write("  ${k}(luceneQueryString: String) : ${key}PagedResult\n");
          }
        }
        else if ( graphql_config instanceof java.lang.Boolean ) {
          // No special action at the moment
        }
        else {
          log.warn("${key} has graphql property but it is an instance of ${graphql_config.class.name} with value of ${graphql_config}. Expected map");
        }
      }
    }
    return sw.toString();
  }

  public buildTypeDefinitions() {
    // We want to emit a findX query for each domain class
    StringWriter sw = new StringWriter();
    domainClasses.each { key, value ->
      sw.write("type ${key} {\n".toString());
      writeDomainClassProperties(sw,value,false)
      // sw.write("  id: String\n".toString());
      sw.write("}\n\n");

      // Do we want to auto define XXQueryResult to let us wrap the array with some pagination properties
      sw.write("type ${key}PagedResult {\n");
      sw.write("  totalCount: Int\n");
      sw.write("  results: [${key}]\n");
      sw.write("}\n\n");
    }

    // This is fugly - we need separate input type declarations for everything we want to pass in as an input type.. so
    domainClasses.each { key, value ->
      sw.write("input ${key}InputType {\n".toString());
      writeDomainClassProperties(sw,value,true)
      // sw.write("  id: String\n".toString());
      sw.write("}\n\n");
    }


    return sw.toString();
  }

  public void writeDomainClassProperties(StringWriter sw, PersistentEntity dc, boolean isInputType) {
    log.debug("writeDomainClassProperties for ${dc}");
    log.debug("composite identity ${dc.getCompositeIdentity()}");
    org.grails.datastore.mapping.model.types.Identity id = dc.getIdentity()
    if ( id != null ) {
      log.debug("add normal identity ${dc.getIdentity()} / ${id.getName()}");
      sw.write("  ${id.getName()}: ID\n".toString());
    }

    log.debug("writeDomainClassProperties(${dc})");
    // @See https://gorm.grails.org/6.0.x/api/org/grails/datastore/mapping/model/PersistentProperty.html
    dc.getPersistentProperties().each { PersistentProperty pp ->
      log.debug("  -> Process persistent property: ${pp} ${pp.getName()} type:${pp.getType()} ${pp.class.name}");
      String tp = convertType(pp, pp.getType(), isInputType);
      log.debug("  -> type conversion = ${tp}");
      sw.write("  ${pp.getName()}: ${tp}\n".toString());
    }

    if ( !isInputType ) {
      sw.write('  errors: [Error]\n');
    }

  }

  public String convertType(PersistentProperty pp, java.lang.Class<?> c, boolean isInputType) {

    String result = null;

    if ( pp instanceof org.grails.datastore.mapping.model.types.Association ) {
      log.debug("    -> process association ${pp.class.name}");
      if ( pp instanceof org.grails.datastore.mapping.model.types.OneToMany ) {
        PersistentEntity associated_entity = pp.getAssociatedEntity();
        // If we're creating input typedefs, add InputType on to the end *ugh*
        result = "[${associated_entity.getJavaClass().getSimpleName()}${isInputType?'InputType':''}]".toString();
      }
      else if ( pp instanceof org.grails.datastore.mapping.model.types.ManyToOne ) {
        PersistentEntity associated_entity = pp.getAssociatedEntity();
        // If we're creating input typedefs, add InputType on to the end *ugh*
        result = "${associated_entity.getJavaClass().getSimpleName()}${isInputType?'InputType':''}".toString();
      }
      else if ( pp instanceof org.grails.datastore.mapping.model.types.OneToOne ) {
        PersistentEntity associated_entity = pp.getAssociatedEntity();
        // If we're creating input typedefs, add InputType on to the end *ugh*
        result = "${associated_entity.getJavaClass().getSimpleName()}${isInputType?'InputType':''}".toString();
      }
      else {
        log.warn("    -> Unhandled association type ${pp}");
      }
    }
    else {
      // log.debug("    -> Handle instance of ${c} (${c?.class.name}) / ${pp.class.name}");
      switch ( c ) {
        case String.class:
          result = 'String';
          break;
        case Long.class:
          result = 'Int';
          break;
        case Set.class:
          result = null;
          break;
        default:
          log.debug("    -> unhandled type ${c}");
          result = 'String';
          break;
      }
    }
    return result;
  }

  private getTypeNameFor(Class c) {
    String result = null;

    switch ( c ) {
        case String.class:
          result = 'String';
          break;
        case Long.class:
          result = 'Int';
          break;
        case Set.class:
          result = null;
          break;
        default:
          log.debug("    -> unhandled type ${c}");
          result = 'String';
          break;
     }
     return result;
  }

}
