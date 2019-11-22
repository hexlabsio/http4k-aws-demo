# Creating a Microservice with Http4K and AWS Lambda

At Hexlabs we have been using AWS lambda functions for number of use cases over the past few years.
The basic idea is that you deploy code that reacts to a specified event in an AWS envrionment. 
AWS is then responsible for ensuring that the code to process the event is run in a timely manner. If no events take place 
the code is not run and so there is cost for the f. On the other side of things if there are a large amount of events
 happening AWS can quickly
spin up many instances of the lambda function to cope with the higher workload. From this we get services that are cost
effective and scalable with a minimal overhead to support them.

A lambda functions can be configured to react to an [ever growing list of events](
https://docs.aws.amazon.com/lambda/latest/dg/lambda-services.html) 
in the AWS ecosystem but we are going to focus on one of the most common uses, reacting to HTTP requests 
sent to [API gateway](https://aws.amazon.com/api-gateway). An API Gateway can be configured to invoke a lambda function
when a specific endpoint and HTTP method is invoked. How to configure will be shown later but for now we will look a at
basic function that could be invoked. 

## Basic API Gateway Lambda
As each AWS event will contains different information the payloads passed to a lambda function will look very 
different. AWS  provides a Java SDK to aid handling different events types, it is also possible to create your own 
handlers that read the events from InputStreams but we will stick with the SDK for a first example. 
Below is a our basic Hello world lambda in Kotlin.

```kotlin
class HelloWorldHandler : RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
  override fun handleRequest(input: APIGatewayProxyRequestEvent?, context: Context?): APIGatewayProxyResponseEvent {
    return APIGatewayProxyResponseEvent().apply {
      statusCode = 200
      body = """{"msg": "hello world"}"""
    }
  }
}
```
This is saying that our lambda will Handle an `APIGatewayProxyRequestEvent` payload and return an `APIGatewayProxyResponseEvent`
with status 200 and a "hello world" msg in JSON body.  

Once attached to an API gateway endpoint a request will return the the HTTP response below.
```shell script
> curl -i -X GET https://tzpwxxbe.execute-api.eu-west-1.amazonaws.com/demo/hello                                                                         
HTTP/2 200
content-type: application/json
content-length: 22
date: Thu, 21 Nov 2019 11:31:36 GMT
x-amzn-requestid: 5ffe35e8-e8fe-4091-ad3b-803ebd060b6c
x-amz-apigw-id: DgdP2EgaDoEFfwg=
x-amzn-trace-id: Root=1-5dd67598-2e8a03500c5af4c99a467348;Sampled=0
x-cache: Miss from cloudfront
via: 1.1 a9fee82d2207aa426fdf06cb95c1f059.cloudfront.net (CloudFront)
x-amz-cf-pop: LHR62-C2
x-amz-cf-id: VNt8h2J0tBu_aJSKsVZlcx0mrBIg6C0BT3Jrd47u3TkJ8tF0_NNC-Q==

{"msg": "hello world"}%

```
If we examine the `APIGatewayProxyRequestEvent` sent to the lambda function we can see that all the information from
 the HTTP request is present alongside other AWS specific information. 
 
 ```json
{
    "resource": "/hello",
    "path": "/hello",
    "httpMethod": "GET",
    "headers": {
        "Accept": "*/*",
        "CloudFront-Forwarded-Proto": "https",
        "CloudFront-Is-Desktop-Viewer": "true",
        "CloudFront-Is-Mobile-Viewer": "false",
        "CloudFront-Is-SmartTV-Viewer": "false",
        "CloudFront-Is-Tablet-Viewer": "false",
        "CloudFront-Viewer-Country": "GB",
        "Host": "tzpwxxbe.execute-api.eu-west-1.amazonaws.com",
        "User-Agent": "curl/7.54.0",
        "Via": "2.0 c946f3637140d7ea94236d87608fb756.cloudfront.net (CloudFront)",
        "X-Amz-Cf-Id": "NQLoEfihGRaa9Mp22DS4J_mb507-xK44EYUB9FMRtE_RpE0BZWJzQ==",
        "X-Amzn-Trace-Id": "Root=1-5dd67549-1426c8f8f9891400c30a620",
        "X-Forwarded-For": "146.90.39.112, 70.132.49.89",
        "X-Forwarded-Port": "443",
        "X-Forwarded-Proto": "https"
    },
    "multiValueHeaders": {
        "Accept": [ "*/*" ],
        "CloudFront-Forwarded-Proto": [ "https" ],
        "CloudFront-Is-Desktop-Viewer": [ "true" ],
        "CloudFront-Is-Mobile-Viewer": [ "false" ],
        "CloudFront-Is-SmartTV-Viewer": [ "false" ],
        "CloudFront-Is-Tablet-Viewer": [ "false" ],
        "CloudFront-Viewer-Country": [ "GB" ],
        "Host": [ "tzpwxxbe.execute-api.eu-west-1.amazonaws.com" ],
        "User-Agent": [ "curl/7.54.0" ],
        "Via": [ "2.0 c946f3637140d7ea94236d87608fb756.cloudfront.net (CloudFront)" ],
        "X-Amz-Cf-Id": [ "NQLoEfihGRaa9Mp22DS4iJ_mb507-xK44EYUB9FMRtE_RpE0BZWJzQ==" ],
        "X-Amzn-Trace-Id": [ "Root=1-5dd67549-1426c8f82f9891400c30a620" ],
        "X-Forwarded-For": [ "146.90.39.100, 70.132.49.00" ],
        "X-Forwarded-Port": [ "443" ],
        "X-Forwarded-Proto": [ "https" ]
    },
    "queryStringParameters": null,
    "multiValueQueryStringParameters": null,
    "pathParameters": null,
    "stageVariables": null,
    "requestContext": {
        "accountId": "11111111",
        "stage": "demo",
        "resourceId": "zt6sq",
        "requestId": "57073c9c-e46f-4856-be5f-a632f9d8c85f",
        "identity": {
            "cognitoIdentityPoolId": null,
            "accountId": null,
            "cognitoIdentityId": null,
            "caller": null,
            "apiKey": null,
            "sourceIp": "146.90.39.100",
            "cognitoAuthenticationType": null,
            "cognitoAuthenticationProvider": null,
            "userArn": null,
            "userAgent": "curl/7.54.0",
            "user": null,
            "accessKey": null
        },
        "resourcePath": "/hello",
        "httpMethod": "GET",
        "apiId": "tzpwxxbe",
        "path": "/demo/hello",
        "authorizer": null
    },
    "body": null,
    "isBase64Encoded": false
}
```

This is great for setting up simple one off endpoints but gets cumbersome very quickly if we want to set up a
REST service with multiple endpoints. Functionality like extracting from parameters encoding 

When we first started to use this we found things like encoding and decoding payloads, checking
HTTP Methods and extracting request parameters were all implemented by hand we were implementing a lot of functionality that would be included in other HTTP libraries. .  
  
  
 ##HTTP4K 
HTTP4K is library written in Kotlin specifically to deal with HTTP programming. Rather than its own implement an HTTP
 server or client it provides a lightweight abstraction that can be easily adapted to existing HTTP servers and clients. 
 Supported servers include:
 - Apache
 - Jetty
 - Ktor
 - Netty
 - Undertow
 - SunHttp
 The library also has modules for Metrics, OpenAPI Contracts, OAuth [amongst others](https://www.http4k.org/)
 
 HTTP4K's design is based on a paper "Server as a Function" that gave the idea that a server should
  be made from the 2 following types of functions:
  
 A HttpHandler or that takes the incoming HTTP Request returning an HTTP Response 
```kotlin 
typealias HttpHandler = (Request) -> Response
```
 A Filter or Middleware that can apply operations pre or post processing by the HttpHandler e.g Tracing or Authentication
```kotlin
interface Filter : (HttpHandler) -> HttpHandler
```

The other concept heavily used is a router that matches a incoming requests to the appropriate HTTPHandler. 

There are also functions that allow these basic components to be combined to make larger components of a the same type

In concrete terms if we want to create an API to fetch and create car information we could naively create some handlers
```kotlin 
  val carLens = Body.auto<Car>().toLens()
  val postCarHandler: HttpHandler = { request: Request ->
    val car = carLens(request)
    val id = carInventoryService.create(car)
    Response(Status.CREATED).with(CarInfo(id, car).jsonBody())
  }
  val getCarHandler: HttpHandler = {
    Response(Status.OK).with(
      carInventoryService.readAll().jsonBody()
    )
  }
```
 These would be bound to specific HTTP request types using a router
 ```kotlin
   val postCarRoute: HttpHandler = routes(
     "/cars" bind POST to postCarHandler,
     "/cars" bind GET to getCarHandler
   )
```
We can capture the time taken for each request to be handled using a filter
```kotlin
  val latencyFilter: Filter = Filter { httpHandler: HttpHandler ->
    { request: Request ->
      val start = System.currentTimeMillis()
      val response = httpHandler(request)
      val latency = System.currentTimeMillis() - start
      logger.info("request took $latency ms")
      response
    }
  }
```
The filter and routes can then be combined to create another HttpHandler 
```kotlin
val service : HttpHandler = latencyFilter.then(postCarRoute)
```
As HttpHandler is just a function `(Request) -> Response` it can be invoked directly with a Request object for testing
```kotlin
println(service(Request(Method.GET, "/cars")))
```
which gives 
```
request took 469 ms
HTTP/1.1 200 OK
content-type: application/json; charset=utf-8

[{"id":"123-X","car":{"make":"Ford","model":"Fiesta","year":1985,"colour":"B....}]
```
or we can run `service` as a Server and invoke remotely using a client, in this case a Ktor server and OkHttp client
```kotlin
  val server  = service.asServer(KtorCIO(9000)).start()
  val client = OkHttp()
  println(client(Request(Method.GET, "http://localhost:9000/cars")))
``` 
which gives 
```
Responding at http://0.0.0.0:9000
request took 0 ms
HTTP/1.1 200 OK
connection: keep-alive
content-type: application/json; charset=utf-8
transfer-encoding: chunked

[{"id":"123-X","car":{"make":"Ford","model":"Fiesta","y...}]
```
 
A mystery line that may be of interest above is 
```kotlin
val carLens = Body.auto<Car>().toLens()
```
In HTTP4K a lens is something that can extract and decode to a specified format from a request while also being
 used to encode and update a response. The carLens above will decode JSON in a request to a car object
 ```kotlin
  val ford: Car = carLens(Request(Method.POST, "/cars").body(
    """{"make":"Ford","model":"Fiesta","year":1985, "colour":"Blue"}"""))
```
and can also be used to modify a response while encoding a car object as JSON
```kotlin
val bmwResponse: Response =  Response(Status.OK)
    .with(carLens of Car(make = "BMW", model = "i8", year = 2019, colour = "White"))
```
HTTP4K also comes with predefined lenses to extract from Parameters and Headers in HTTP messages.

###HTTP4K Serverless module
While an AWS lambda functions doesn't equate to a server the APIGateway Request and Response events have been adapted to
 the HTTP4k model in the HTTP4K Serverless module. In this case we implement a supplied AppLoader interface.
 
 ```kotlin
object InventoryHandlerLambdaFunction : AppLoader {
override fun invoke(env: Map<String, String>): HttpHandler =
  latencyFilter.then(
    routes(
      "/cars" bind Method.POST to postCarHandler,
      "/cars" bind Method.GET to getCarHandler
    )
  )
} 
```

We now have access to HTTP4Ks functionality and are able to create powerful and succinct REST services inside our lambda function. 

###Deployment
Setting up an Lambda function in AWS involves a number of operations that can be off putting. These include setup and
 configuration for:
- IAM roles
- Policies 
- Log groups
- AWS Gateway

There a number of libraries that help with this configuration but seeing as this is a Kotlin article, we are going to
 use [Kloudformation](https://kloudformation.hexlabs.io) an open source library developed by Hexlabs.
Kloudformation allows users to generate AWS infrastructure templates (AKA Cloudformation, see what we did there) using
 Kotlin for over 300 AWS resources, it also has a serverless module that greatly reduces boilerplate when creating
  templates for lambda functions. 
 The code below is enough to generate a Cloudformation template for our lambda setup against API gateway. The full
  generated template can be [viewed here](https://github.com/hexlabsio/http4k-aws-demo/blob/master/template.yml) 
 ```kotlin
class Stack : StackBuilder {
  override fun KloudFormation.create(args: List<String>) {
    val (code) = args
    serverless(serviceName = "htt4k-aws-demo", stage = "demo", bucketName = +"lambda-cf-bucket") {
      serverlessFunction(functionId = "demo-http4k", codeLocationKey = +code,
        handler = +"org.http4k.serverless.lambda.LambdaFunction::handle",
        runtime = +"java8") {
        lambdaFunction {
          timeout(30)
          memorySize(512)
          environment {
            variables(json(mapOf("HTTP4K_BOOTSTRAP_CLASS" to "io.hexlabs.vehicle.inventory.InventoryHandler")))
          }
        }
        http {
          path("/inventory/cars") {
            Method.GET()
            Method.POST()
            path("{id}") {
              Method.GET()
              Method.PUT()
              Method.DELETE()
            }
          }
        }
      }
    }
  }
} 
```

A few parts to note. 
- The `http {..}` is optional and specifies endpoints bound from API gateway to our lambda function.   
- When using HTTP4K the `handler` field should always be set to `org.http4k.serverless.lambda.LambdaFunction::handle`
- The object that Implements the HTTP4K `AppLoader` we mentioned earlier is set as an environment variable `HTTP4K_BOOTSTRAP_CLASS`

At this point the `Stack` can be translated to a Cloudformation template and deployed using the 
[AWS CLI](https://aws.amazon.com/cli/).
 
Alternatively we can use Kloudformations own CLI against the Stack.kt file.
The Kloudformation CLI that can be installed using
 ```shell script
curl -s https://install.kloudformation.hexlabs.io | sh
```
Once installed ensure aws credentials are stored in `~/.aws/credentials` and simply run 
```shell script
kloudformation -v 1.1.76 -m serverless@1.1.2 deploy -stack-name htt4k-aws-demo -bucket lambda-cf-bucket -location ./build/libs/http4k-aws-demo-uber.jar
```

This will deploy our lambda giving the ServiceEndpoint as an output

```shell script
ApiDeployment23bce43cb19b486bbd0cf1ec0bbdf956 AWS::ApiGateway::Deployment Status became CREATE_COMPLETE (rk5a9v)
ApiDeployment654780393ab0422185c845adb03ac95e AWS::ApiGateway::Deployment Status became CREATE_COMPLETE (lfz1n4)
htt4k-aws-demo AWS::CloudFormation::Stack Status became CREATE_COMPLETE (arn:aws:cloudformation:eu-west-1:1111111:stack/htt4k-aws-demo/4aead360-0bae-11ea-89d2-060f8efab01c)

Stack Create Complete

ServiceEndpoint: https://mc925zld.execute-api.eu-west-1.amazonaws.com/demo
```

We are now able to code build and deploy an lambda function all using Kotlin. If you interested the code can be found 
[here](https://github.com/hexlabsio/http4k-aws-demo). Some may be scared off by the idea of using lambda functions
 with the JVM due to [cold starts](https://levelup.gitconnected.com/aws-lambda-cold-start-language-comparisons-2019-edition-%EF%B8%8F-1946d32a0244)
 while this isn't as big a problem as in the past HTTP4K can also be compiled to a native image using Graalvm greatly
  reducing this time. This will be a topic for future articles but in the meantime if you interested we have a basic
   implementation available [here](https://github.com/hexlabsio/graalvm-http-lambda-seed)  
   
