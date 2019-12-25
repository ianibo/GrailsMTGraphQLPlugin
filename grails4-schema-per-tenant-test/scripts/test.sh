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

