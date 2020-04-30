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
import grails.gorm.multitenancy.CurrentTenant
import grails.gorm.transactions.Transactional
import grails.gorm.multitenancy.Tenants
import org.grails.web.util.WebUtils

@CurrentTenant
class GraphqlController {

  static responseFormats = ['json', 'xml']

  // GraphQL graphQL
  GraphqlConfigManager graphqlConfigManager;

  // Declare a graphql context builder - if the host application declares a spring bean of the form
  // graphqlContextBuilder(MyGraphQLContextBuilder) then it will be used here to get hold of the context
  // otherwise the fetcher will be passed a null context.
  GraphqlContextBuilder graphqlContextBuilder;

  // https://github.com/grails/gorm-graphql/blob/master/plugin/grails-app/controllers/org/grails/gorm/graphql/plugin/GraphqlController.groovy
  def index() {
    log.debug("GraphqlController::index(${params}) -- tenant=${Tenants.currentId()}");
    String query = null;
    String operationName = null;

    // context is an Object in the graphql api and is not used by the library itself but is
    // instead a container where we can add our own context properties. Estabilst this by adding
    // an entry to spring/resources.groovy like graphqlContextBuilder(SpttContextBuilder)
    def gwr = WebUtils.retrieveGrailsWebRequest()
    Map<String,Object> context = graphqlContextBuilder?.buildContext(gwr)
    log.debug("context will be ${context}");

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

    log.debug("Calling graphqlConfigManager.graphQL.execute....");

    ExecutionResult executionResult = graphqlConfigManager.graphQL.execute(ExecutionInput.newExecutionInput()
                 .query(query)
                 .operationName(operationName)
                 .context(context)
                 .root(context) // This we are doing do be backwards compatible
                 .variables(variables)
                 .build())

    log.debug("Got execution result: ${executionResult} ${executionResult.data}");
    if (executionResult.errors.size() > 0) {
      result.put('errors', executionResult.errors)
    }
    result.put('data', executionResult.data)

    respond result
  }

}
