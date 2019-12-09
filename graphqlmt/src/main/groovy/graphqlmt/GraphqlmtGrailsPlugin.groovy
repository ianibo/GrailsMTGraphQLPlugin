package graphqlmt

import grails.plugins.*
import grails.core.GrailsClass
import graphql.GraphQL
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;


class GraphqlmtGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "4.0.1 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def title = "Graphqlmt" // Headline display name of the plugin
    def author = "Your name"
    def authorEmail = ""
    def description = '''\
Brief summary/description of the plugin.
'''

    List loadAfter = ['services']

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/graphqlmt"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
//    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
//    def organization = [ name: "My Company", url: "http://www.my-company.com/" ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    // Online location of the plugin's browseable source code.
//    def scm = [ url: "http://svn.codehaus.org/grails-plugins/" ]



    // https://github.com/grails/gorm-graphql/blob/master/plugin/src/main/groovy/org/grails/gorm/graphql/plugin/GormGraphqlGrailsPlugin.groovy


    Closure doWithSpring() { {->
            // TODO Implement runtime spring config (optional)

            // Generate a graphql schema
            // graphQLSchema(graphQLSchemaGenerator: "generate")
            typeRegistry(TypeDefinitionRegistry)

            graphQLSchemaGenerator(GraphqlSchemaGenerator) {
              typeRegistry = ref('typeRegistry')
            }

            graphQLSchema(graphQLSchemaFactory: "generate")

            // install the graphql object in the global context so other components can access
            graphQL(GraphQL, ref("graphQLSchema"))

            // GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, buildRuntimeWiring());
        }
    }

    void doWithDynamicMethods() {
        // TODO Implement registering dynamic methods to classes (optional)
    }

    void doWithApplicationContext() {
        // TODO Implement post initialization spring config (optional)
        // (grailsApplication.getArtefacts("Domain")).each {GrailsClass gc ->
        //     println("Process domain class : ${gc}");
        //     grailsApplication.mainContext.graphQLService.registerDomainClass(gc);
        // }
    }


    void onChange(Map<String, Object> event) {
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    void onConfigChange(Map<String, Object> event) {
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    void onShutdown(Map<String, Object> event) {
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
