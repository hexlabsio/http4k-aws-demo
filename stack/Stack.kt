import io.kloudformation.KloudFormation
import io.kloudformation.StackBuilder
import io.kloudformation.json
import io.hexlabs.kloudformation.module.serverless.Method
import io.hexlabs.kloudformation.module.serverless.serverless
import io.kloudformation.model.KloudFormationTemplate
import io.kloudformation.unaryPlus

class Stack : StackBuilder {
  override fun KloudFormation.create(args: List<String>) {
    val (code) = args
    serverless(serviceName = "htt4k-aws-demo", stage = "demo", bucketName = +"lambda-cf-bucket2") {
      serverlessFunction(functionId = "demo-hello-world", codeLocationKey = +code,
        handler = +"io.hexlabs.HelloWorldHandler",
        runtime = +"java8") {
        lambdaFunction {
          timeout(30)
          memorySize(512)
        }
        http {
          path("/hello") {
            Method.GET()
          }
        }
      }
      serverlessFunction(functionId = "demo-http4k", codeLocationKey = +code,
        handler = +"org.http4k.serverless.lambda.LambdaFunction::handle",
        runtime = +"java8") {
        lambdaFunction {
          timeout(30)
          memorySize(512)
          environment {
            variables(json(mapOf(
              "HTTP4K_BOOTSTRAP_CLASS" to "io.hexlabs.vehicle.inventory.InventoryHandlerLambdaFunction"
            )))
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
  fun template(args: List<String> = emptyList()) =
    KloudFormationTemplate.create { create(args) }
}

fun main() {
  println(Stack())
}
// kloudformation -v 1.1.2 -m serverless@1.1.1 deploy -stack-name htt4k-aws-demo -bucket lambda-cf-bucket2 -location build/lib/http4k-aws-demo-uber.jar
