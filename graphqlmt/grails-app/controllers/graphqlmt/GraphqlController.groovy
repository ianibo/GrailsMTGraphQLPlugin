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


class GraphqlController {

  static responseFormats = ['json', 'xml']
  GraphQL graphQL

  // https://github.com/grails/gorm-graphql/blob/master/plugin/grails-app/controllers/org/grails/gorm/graphql/plugin/GraphqlController.groovy
  def index() {

    if (request.contentLength != 0 && request.method != 'GET' ) {
      String encoding = request.characterEncoding ?: 'UTF-8'
      InputStream is = request.getInputStream();
      String body = IOUtils.toString(is, encoding)
    } else {
      // graphQLRequest = GraphQLRequestUtils.graphQLRequestWithParams(params)
    }

    Map<String, Object> result = new LinkedHashMap<>()

    // ExecutionResult executionResult = graphQL.execute(ExecutionInput.newExecutionInput()
    //             .query(graphQLRequest.query)
    //             .operationName(graphQLRequest.operationName)
    //             //.context(context)
    //             .root(context) // This we are doing do be backwards compatible
    //             .variables(graphQLRequest.variables)
    //             .build())

    // if (executionResult.errors.size() > 0) {
    //   result.put('errors', executionResult.errors)
    // }
    // result.put('data', executionResult.data)

    result
  }
}
