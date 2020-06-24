#include "contiki.h"
#include "coap-engine.h"
#include <string.h>
#include "time.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "Light Actuator"
#define LOG_LEVEL LOG_LEVEL_DBG

/* Component definition */
#include "../dev/bulb.h"


static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_post_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_event_handler(void);

/* An actuator, depending on the post variable mode, the light is activated or deactivated */
EVENT_RESOURCE(res_bulb,
         "title=\"Bulb controller: ?POST/PUT mode=ON|OFF automatic=ON|OFF\";rt=\"Light Control\";obs",
         res_get_handler,
         res_post_put_handler,
         res_post_put_handler,
         NULL,
         res_event_handler);
         
/* Counting how many times the periodic obs handler is called */
static int32_t obs_counter = 0;


static void res_post_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{

	if(request != NULL) {
    LOG_DBG("Received POST/PUT\n");
  }
  
  size_t len = 0;
  const char *mode = NULL;
  int success = 1;

  if((len = coap_get_post_variable(request, "mode", &mode))) {
    LOG_DBG("mode %s\n", mode);

    if(strncmp(mode, "ON", len) == 0) {
    	switch_bulb_on();
    } else if(strncmp(mode, "OFF", len) == 0) {
    	switch_bulb_off();
    } else {
      success = 0;
    }
  } else if((len = coap_get_post_variable(request, "automatic", &mode))) {
    LOG_DBG("automatic %s\n", mode);

    if(strncmp(mode, "ON", len) == 0) {
    	set_bulb_automatic();
      //LOG_DBG("Light on!\n");
    } else if(strncmp(mode, "OFF", len) == 0) {
    	set_bulb_manual();
      //LOG_DBG("Light off\n");
    } else {
      success = 0;
    }
  } else {
    success = 0;

  } 
  
  if(!success)
    coap_set_status_code(response, BAD_REQUEST_4_00);
  else
  	coap_set_status_code(response, CHANGED_2_04);
  
}


static void res_get_handler(coap_message_t *request, coap_message_t *response, 
		uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  /* Keep server log clean from ticking events */
  if(request != NULL) {
    LOG_DBG("Received GET\n");
  }
  
  // (seconds since Jan 1, 1970)
  unsigned long timestamp = (unsigned long)time(NULL);	
  
  unsigned int accept = -1;
  coap_get_header_accept(request, &accept);

  if(accept == TEXT_PLAIN) {
    coap_set_header_content_format(response, TEXT_PLAIN);
    snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "mode=%d,timestamp=%lu,automatic=%d", 
    		get_bulb_mode(), timestamp, bulb_is_automatic());

    coap_set_payload(response, (uint8_t *)buffer, strlen((char *)buffer));
    
  } else if(accept == APPLICATION_XML) {
    coap_set_header_content_format(response, APPLICATION_XML);
    snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "<mode=\"%d\"/><timestamp=\"%lu\"/><automatic=\"%d\"/>", 
    		get_bulb_mode(), timestamp, bulb_is_automatic());

    coap_set_payload(response, buffer, strlen((char *)buffer));
    
  } else if(accept == -1 || accept == APPLICATION_JSON) {
    coap_set_header_content_format(response, APPLICATION_JSON);
    snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "{\"mode\":%d,\"timestamp\":%lu,\"automatic\":%d}", 
    		get_bulb_mode(), timestamp, bulb_is_automatic());

    coap_set_payload(response, buffer, strlen((char *)buffer));
    
  } else {
    coap_set_status_code(response, NOT_ACCEPTABLE_4_06);
    const char *msg = "Supporting content-types text/plain, application/xml, and application/json";
    coap_set_payload(response, msg, strlen(msg));
  }
}


static void res_event_handler(void)
{
	obs_counter++;
	LOG_DBG("######### sending msg %d\n",obs_counter);
	// Notify all the observers
	coap_notify_observers(&res_bulb);
}
