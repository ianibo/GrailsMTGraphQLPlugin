package sptt

import graphqlmt.GraphqlContextBuilder;
import org.grails.web.servlet.mvc.GrailsWebRequest

public class SpttContextBuilder implements GraphqlContextBuilder {
  Map buildContext(GrailsWebRequest request) {
    Map result = [
      textContextProperity:'testContextValue'
    ]
  }
}

