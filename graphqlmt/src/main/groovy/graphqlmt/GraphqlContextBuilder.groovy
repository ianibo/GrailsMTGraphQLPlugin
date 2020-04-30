package graphqlmt

import org.grails.web.servlet.mvc.GrailsWebRequest 

public interface GraphqlContextBuilder {
  Map buildContext(GrailsWebRequest request)
}
