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
 * because of changes in the grails ecosystem domain class introspection can't happen until after
 * gorm is initialised. This means code that used to execute in doWithSpring needs to move to doWithApplicationConext
 * This class holds the various GraphQL artefacts that need to be configured after applicationContext initialisation
 */
@Slf4j
class GraphqlConfigManager implements GrailsApplicationAware {

  GrailsApplication grailsApplication
  SDLFactory sdlFactory

  public void initialise() {
    log.debug("GraphqlConfigManager::initialise");
    TypeDefinitionRegistry tdl = sdlFactory.generate();
  }
}
