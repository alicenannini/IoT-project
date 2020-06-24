#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "contiki.h"
#include "coap-engine.h"
#include "coap-blocking-api.h"

/* Log configuration */
#include "coap-log.h"
#include "sys/log.h"
#define LOG_MODULE "NODE          "
#define LOG_LEVEL LOG_LEVEL_INFO

/* Interval for checking resources status */
#define INTERVAL		(10 * CLOCK_SECOND)

/* Resource definition */
extern coap_resource_t res_light;
extern coap_resource_t res_bulb;

/* Components of the node */
#include "./dev/bulb.h"
#include "./dev/light-control.c"

/* Server IP and resource path */
#define SERVER_EP "coap://[fd00::1]:4456"
char* service_url = "/registration";


/*---------------------------------------------------------------------------*/
PROCESS(node_process, "Sensor node");
AUTOSTART_PROCESSES(&node_process);
/*---------------------------------------------------------------------------*/
/* This function is will be passed to COAP_BLOCKING_REQUEST() to handle responses. */
void client_chunk_handler(coap_message_t *response)
{
  if(response == NULL) {
    LOG_INFO("Request timed out\n");
    return;
  }
	
	LOG_INFO("REGISTERED\n");
}
/*---------------------------------------------------------------------------*/
PROCESS_THREAD(node_process, ev, data){
  /* Timer for registration */
  static struct etimer timer;
  /* Endpoint and message definition */
  static coap_endpoint_t server_ep;
	static coap_message_t request[1]; // packet treated as pointer
	
	/* Initialize the status of the bulb component */
	init_bulb();
	PROCESS_BEGIN();
	
	
	/* Resource activation */
	coap_activate_resource(&res_light,"sensors/light"); //sensor
	coap_activate_resource(&res_bulb,"actuators/bulb");  //actuator

  /* Populate the coap_endpoint_t data structure */
	coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);
	
  /* Prepare the message */
	coap_init_message(request, COAP_TYPE_CON, COAP_GET, 0);
	coap_set_header_uri_path(request, service_url);
	/* Issue the request in a blocking manner
		The client will wait for the server to reply (or the transmission to timeout) */
	COAP_BLOCKING_REQUEST(&server_ep, request, client_chunk_handler);
	
	while(true){
		
			while(bulb_is_automatic()){
				/* Timer activation */
				etimer_set(&timer, INTERVAL);
		
				/* Periodically check the light status
					If it's down a THRESHOLD then switch ON the bulb, and viceversa */
				if(light_is_down_threshold() && bulb_is_off())
					switch_bulb_on();
				else if(light_is_over_threshold() && bulb_is_on())
					switch_bulb_off();
				
				/* Wait for the periodic timer to expire */
				PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&timer));
				
				
			} // while bulb is automatic
			// wait that bulb is setted in automatic mode
			PROCESS_WAIT_EVENT();
			//PROCESS_WAIT_EVENT_UNTIL(ev == AUTOMATIC_BULB_EVENT);
	}

  PROCESS_END();
}
