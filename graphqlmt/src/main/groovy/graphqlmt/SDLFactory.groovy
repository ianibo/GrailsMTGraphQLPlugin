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

'''+buildTypeDefinitions())
    return sw.toString();
  }

  /**
   * iterate over domain classes and build any finder methods we want to expose to the query type
   * SeeAlso https://atheros.ai/blog/graphql-list-how-to-use-arrays-in-graphql-schema
   */
  public buildQueryTypeFields() {
    StringWriter sw = new StringWriter();
    domainClasses.each { key, value ->
	sw.write("  find${key}UsingLQS(luceneQueryString: String) : ${key}PagedResult\n");
    }
    return sw.toString();
  }

  public buildTypeDefinitions() {
    // We want to emit a findX query for each domain class
    StringWriter sw = new StringWriter();
    domainClasses.each { key, value ->
      sw.write("type ${key} {\n".toString());
      writeDomainClassProperties(sw,value)
      // sw.write("  id: String\n".toString());
      sw.write("}\n\n");

      // Do we want to auto define XXQueryResult to let us wrap the array with some pagination properties
      sw.write("type ${key}PagedResult {\n");
      sw.write("  totalCount Int\n");
      sw.write("  results [${key}]\n");
      sw.write("}\n\n");
    }
    return sw.toString();
  }

  public void writeDomainClassProperties(StringWriter sw, PersistentEntity dc) {

    log.debug("composite identity ${dc.getCompositeIdentity()}");
    org.grails.datastore.mapping.model.types.Identity id = dc.getIdentity()
    if ( id != null ) {
      log.debug("add normal identity ${dc.getIdentity()} / ${id.getName()}");
      sw.write("  ${id.getName()}: ID\n".toString());
    }

    log.debug("writeDomainClassProperties(${dc})");
    // @See https://gorm.grails.org/6.0.x/api/org/grails/datastore/mapping/model/PersistentProperty.html
    dc.getPersistentProperties().each { PersistentProperty pp ->
      log.debug(" -> Process persistent property: ${pp} ${pp.getName()} type:${pp.getType()} ${pp.class.name}");
      String tp = convertType(pp, pp.getType());
      log.debug("   -> type conversion = ${tp}");
      sw.write("  ${pp.getName()}: ${tp}\n".toString());
    }
  }

  public String convertType(PersistentProperty pp, java.lang.Class<?> c) {

    String result = null;

    if ( pp instanceof org.grails.datastore.mapping.model.types.Association ) {
      log.debug("process association ${pp.class.name}");
      if ( pp instanceof org.grails.datastore.mapping.model.types.OneToMany ) {
        PersistentEntity associated_entity = pp.getAssociatedEntity();
        result = "[${associated_entity.getJavaClass().getSimpleName()}]".toString();
      }
      else if ( pp instanceof org.grails.datastore.mapping.model.types.ManyToOne ) {
        PersistentEntity associated_entity = pp.getAssociatedEntity();
        result = associated_entity.getJavaClass().getSimpleName();
      }
      else {
        log.warn("Unhandled association type ${pp}");
      }

    }
    else {
      log.debug("Handle instance of ${pp.class.name}");
      switch ( c ) {
        case String.class:
          log.debug("It's a string");
          result = 'String';
        case Long.class:
          result = 'Int';
        case Set.class:
          log.debug("Handling a set");
          result = null;
        default:
          log.debug("unhandled type ${c}");
          result = 'String';
      }
    }
    return result;
  }

}
