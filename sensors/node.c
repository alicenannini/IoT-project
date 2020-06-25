#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "contiki.h"
#include "coap-engine.h"
#include "coap-blocking-api.h"
#include "os/dev/button-hal.c"

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
extern process_event_t AUTOMATIC_BULB_EVENT ;

/* Server IP and resource path */
#define SERVER_EP "coap://[fd00::1]:4456"
char* service_url = "/registration";
/* To save the response to the blocking request */
coap_message_t *response;

/*---------------------------------------------------------------------------*/
PROCESS(node_process, "Sensor node");
AUTOSTART_PROCESSES(&node_process);
/*---------------------------------------------------------------------------*/
/* This function is will be passed to COAP_BLOCKING_REQUEST() to handle responses. */
void client_chunk_handler(coap_message_t *r)
{
  if(r == NULL) {
    LOG_INFO("Request timed out\n");
    return;
  }
	
	LOG_INFO("REGISTERED\n");
	response = r;
}
/*---------------------------------------------------------------------------*/
PROCESS_THREAD(node_process, ev, data){
  /* Timer for periodically checking the light status */
  static struct etimer timer;
  /* Button for changing the updating mode of the actuator: AUTOMATIC or MANUAL */
  button_hal_button_t* btn;
  /* Endpoint and message definition */
  static coap_endpoint_t server_ep;
	static coap_message_t request[1]; // packet treated as pointer
	
	
	PROCESS_BEGIN();
	
	/* Initialize the status of the bulb component */
	init_bulb();
	/* Initialize the button (index 0 'cause we only have one button) */
	btn = button_hal_get_by_index(0);
	if(btn){
		
		LOG_INFO("%s on pin %u with ID=0, Logic=%s, Pull=%s\n",
			BUTTON_HAL_GET_DESCRIPTION(btn), btn->pin,
			btn->negative_logic ? "Negative" : "Positive",
			btn->pull == GPIO_HAL_PIN_CFG_PULL_UP ? "Pull Up" : "Pull Down");
	
	}
	
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
	do{
		COAP_BLOCKING_REQUEST(&server_ep, request, client_chunk_handler);
	}while(response == NULL);
	
	while(true){
			/* Timer activation */
			etimer_set(&timer, INTERVAL);
			/* Wait for an event */
			PROCESS_WAIT_EVENT();
			
			if(bulb_is_automatic() && ev == PROCESS_EVENT_TIMER){
				LOG_INFO("Checking bulb status (timer expired event)\n");
				/* Periodically check the light status
					If it's down a THRESHOLD then switch ON the bulb, and viceversa */
				if(light_is_down_threshold() && bulb_is_off())
					switch_bulb_on();
				else if(light_is_over_threshold() && bulb_is_on())
					switch_bulb_off();
				
				
			} // if bulb is automatic
			
			else if(ev == button_hal_release_event) {
				btn = (button_hal_button_t *)data;
				if( bulb_is_automatic() ){
					set_bulb_manual();
					LOG_INFO("Release event (bulb is now MANUAL)\n");
				}else{
					set_bulb_automatic();
					LOG_INFO("Release event (bulb is now AUTOMATIC)\n");
				}
			} // is button has been relased
			
			else if(ev == AUTOMATIC_BULB_EVENT)
				LOG_INFO("Automatic Bulb event\n");
			
	}

  PROCESS_END();
}
