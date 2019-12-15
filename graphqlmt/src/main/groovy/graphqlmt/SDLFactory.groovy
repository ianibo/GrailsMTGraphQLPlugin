package graphqlmt

import grails.core.GrailsClass
import grails.core.GrailsApplication
import grails.core.support.GrailsApplicationAware
import groovy.util.logging.Slf4j
import grails.util.GrailsNameUtils;

import org.grails.datastore.mapping.model.PersistentEntity

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
    TypeDefinitionRegistry result = null;
    String sdl = buildSDL();
    log.debug("Generated schema:\n\n${sdl}\n\nParsing...\n");
    SchemaParser sp = new SchemaParser();
    sp.parse(sdl);
    return result;
  }

  public String buildSDL() {

    // (grailsApplication.getArtefacts("Domain")).each {GrailsClass dc ->
    (grailsApplication.mappingContext.getPersistentEntities()).each {PersistentEntity dc ->
      log.debug("registerDomainClass(${dc}) - ${dc.getJavaClass().getName()}");
      domainClasses.put(dc.getJavaClass().getSimpleName(), dc);
    }

    StringWriter sw = new StringWriter();
    sw.write('''
schema {
  query: QueryType
}

type QueryType {
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
	sw.write("  find${key}UsingLQS(luceneQueryString: String) : [${key}]\n");
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
    }
    return sw.toString();
  }

  public void writeDomainClassProperties(StringWriter sw, PersistentEntity dc) {

    log.debug("composite identity ${dc.getCompositeIdentity()}");
    log.debug("composite identity ${dc.getIdentity()}");

    log.debug("writeDomainClassProperties(${dc})");
    dc.getPersistentProperties().each { pp ->
      log.debug("${pp}");
    }
  }

}