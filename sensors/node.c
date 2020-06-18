#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "contiki.h"
#include "contiki-net.h"
#include "coap-engine.h"
#include "coap-blocking-api.h"
#include "random.h"
#include "node-id.h"

/* Log configuration */
#include "coap-log.h"
#include "sys/log.h"
#define LOG_MODULE "IoT NODE"
#define LOG_LEVEL LOG_LEVEL_INFO

/* Interval for collecting data */
#define START_INTERVAL		(5 * CLOCK_SECOND)

/* Resource definition */
extern coap_resource_t res_light;
extern coap_resource_t res_bulb;

/* Server IP and resource path */
#define SERVER_EP "coap://[fd00::1]:4456"
#define NUMBER_OF_URLS 4
char *service_urls[NUMBER_OF_URLS] =
{ "actuators/bulb", "sensors/light" };

/*---------------------------------------------------------------------------*/
PROCESS(node_process, "Sensor node");
AUTOSTART_PROCESSES(&node_process);
/*---------------------------------------------------------------------------*/
/* This function is will be passed to COAP_BLOCKING_REQUEST() to handle responses. */
void client_chunk_handler(coap_message_t *response)
{
  const uint8_t *chunk;

  if(response == NULL) {
    LOG_INFO("Request timed out");
    return;
  }

  int len = coap_get_payload(response, &chunk);

  LOG_INFO("|%.*s", len, (char *)chunk);
}
/*---------------------------------------------------------------------------*/
PROCESS_THREAD(node_process, ev, data){
  
  /* Endpoint and message definition */
  static coap_endpoint_t server_ep;
	static coap_message_t request[1];
	
	PROCESS_BEGIN();
	
	/* Resource activation */
	coap_activate_resource(&res_bulb,service_urls[0]); //actuator
	coap_activate_resource(&res_light,service_urls[1]);//sensor
  
  /* Populate the coap_endpoint_t data structure */
	coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);
	
  /* Prepare the message */
	coap_init_message(request, COAP_TYPE_CON, COAP_GET, 0);
	coap_set_header_uri_path(request, "registration");
	LOG_INFO("Registration request sent\n");
  /* Issue the request in a blocking manner
	The client will wait for the server to reply (or the transmission to timeout) */
	COAP_BLOCKING_REQUEST(&server_ep, request, client_chunk_handler);
	

  PROCESS_END();
}
