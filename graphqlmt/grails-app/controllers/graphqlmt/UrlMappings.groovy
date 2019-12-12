package graphqlmt

class UrlMappings {

    static mappings = {
        "/graphql"(controller: 'graphql')
        "/graphql/test"(controller: 'graphql', action:'test')
    }
}
