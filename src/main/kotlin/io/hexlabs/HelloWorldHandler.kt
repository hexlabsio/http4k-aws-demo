package io.hexlabs

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory

class HelloWorldHandler : RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
  val logger = LoggerFactory.getLogger(HelloWorldHandler::class.java)
  val jsonMapper = jacksonObjectMapper()

  override fun handleRequest(input: APIGatewayProxyRequestEvent?, context: Context?): APIGatewayProxyResponseEvent {
    logger.info("APIGatewayProxyRequestEvent received: \n ${jsonMapper.writeValueAsString(input)}")
    return APIGatewayProxyResponseEvent().apply {
      statusCode = 200
      body = """{"msg": "hello world"}"""
    }
  }
}
