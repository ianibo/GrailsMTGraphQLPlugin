package graphqlmt;

import grails.io.IOUtils
import graphql.ExecutionInput
import graphql.ExecutionResult

// https://github.com/graphql-java/graphql-java/blob/master/src/main/java/graphql/GraphQL.java
import graphql.GraphQL
import groovy.transform.CompileStatic
import org.springframework.context.MessageSource
import graphql.GraphQL
import groovy.transform.CompileStatic
import grails.converters.JSON

class GraphqlController {

  static responseFormats = ['json', 'xml']
  GraphQL graphQL

  // https://github.com/grails/gorm-graphql/blob/master/plugin/grails-app/controllers/org/grails/gorm/graphql/plugin/GraphqlController.groovy
  def index() {
    log.debug("GraphqlController::index(${params})");
    String query = null;
    String operationName = null;
    Object context = null;
    Map<String,Object> variables = [:]

    if (request.contentLength != 0 && request.method != 'GET' ) {
      switch ( request.format ) {
        case 'application/graphql':
          String encoding = request.characterEncoding ?: 'UTF-8'
          InputStream is = request.getInputStream();
          query = IOUtils.toString(is, encoding)
          log.debug("Handle graraphql ${query}");
          break;
        case 'json':
          log.debug("Handle graphql in json ${request.JSON}");
          query = request.JSON.query;
          variables = (request.JSON.variables instanceof Map) ? (Map)request.JSON.variables : Collections.emptyMap()
          operationName = request.JSON.operationName
          break;
        default:
          log.warn("Unhandled mime type ${request.format}");
          break;
      }
    } else {
      // graphQLRequest = GraphQLRequestUtils.graphQLRequestWithParams(params)
      
    }

    log.debug("Process query: ${query}");
    Map<String, Object> result = new LinkedHashMap<>()

    ExecutionResult executionResult = graphQL.execute(ExecutionInput.newExecutionInput()
                 .query(query)
                 .operationName(operationName)
                 .context(context)
                 .root(context) // This we are doing do be backwards compatible
                 .variables(variables)
                 .build())

    if (executionResult.errors.size() > 0) {
      result.put('errors', executionResult.errors)
    }
    result.put('data', executionResult.data)

    respond result
  }

}
