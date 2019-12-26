# curl -X "POST" "http://localhost:8080/graphql" \
#      -H "Content-Type: application/graphql" \
#      -d $'
# {
#   speakerList(max: 3) {
#     id
#     name
#     talks {
#       title
#     }
#   }
# }'
# 
# 



curl -X "POST" --header "X-TENANT: test" "http://localhost:8080/admin/createTenant"
curl -X "GET" --header "X-TENANT: test" "http://localhost:8080/widget/createWidget?name=fred"

curl -X "POST" "http://localhost:8080/graphql" \
     --header "X-TENANT: test" \
     -H "Accept: application/json" \
     -H "Content-Type: application/json" -d '
{
  "query": "query { __schema { types { name fields { name } } } }",
  "variables":{
  }
}'

curl -X "POST" "http://localhost:8080/graphql" \
     --header "X-TENANT: test" \
     -H "Accept: application/json" \
     -H "Content-Type: application/json" -d '
{
  "query": "query { findWidgetUsingLQS(luceneQueryString:\"title:fred\") { widgetName } }",
  "variables":{
  }
}'

