package graphqlmt;

import grails.io.IOUtils
import graphql.ExecutionInput
import graphql.ExecutionResult

// https://github.com/graphql-java/graphql-java/blob/master/src/main/java/graphql/GraphQL.java
import graphql.GraphQL
import groovy.transform.CompileStatic
import org.springframework.context.MessageSource

@CompileStatic
class GraphqlController {

  static responseFormats = ['json', 'xml']

  // https://github.com/grails/gorm-graphql/blob/master/plugin/grails-app/controllers/org/grails/gorm/graphql/plugin/GraphqlController.groovy
  def index() {
     Map<String, Object> result = new LinkedHashMap<>()
     result
  }
}
