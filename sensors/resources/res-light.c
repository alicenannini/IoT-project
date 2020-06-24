#include "contiki.h"

#include <stdio.h>
#include <string.h>
#include "time.h"
#include "coap-engine.h"
#include "coap-observe.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "Light Sensor  "
#define LOG_LEVEL LOG_LEVEL_DBG

/* Component definition */
#include "../dev/light-control.h"


/* Counting how many times the periodic obs handler is called */
static int32_t obs_counter = 0;


static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_event_handler(void);

/* A simple getter example. Returns the reading from light sensor with a simple etag */
EVENT_RESOURCE(res_light,
						   "title=\"Photosynthetic and solar light\";rt=\"Light Sensor\";obs",
						   res_get_handler,
						   NULL,
						   NULL,
						   NULL,
						   res_event_handler);



static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  /* Keep server log clean from ticking events */
  if(request != NULL) {
    LOG_DBG("Received GET\n");
  }
  
  double light_value = collect_light_data();
  // (seconds since Jan 1, 1970)
  unsigned long timestamp = (unsigned long)time(NULL);	

  unsigned int accept = -1;
  coap_get_header_accept(request, &accept);

  if(accept == TEXT_PLAIN) {
    coap_set_header_content_format(response, TEXT_PLAIN);
    snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "light=%f,timestamp=%lu", light_value, timestamp);

    coap_set_payload(response, (uint8_t *)buffer, strlen((char *)buffer));
    
  } else if(accept == APPLICATION_XML) {
    coap_set_header_content_format(response, APPLICATION_XML);
    snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "<light=\"%f\"/><timestamp=\"%lu\"/>", light_value, timestamp);

    coap_set_payload(response, buffer, strlen((char *)buffer));
    
  } else if(accept == -1 || accept == APPLICATION_JSON) {
    coap_set_header_content_format(response, APPLICATION_JSON);
    snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "{\"light\":%f,\"timestamp\":%lu}", light_value, timestamp);

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
	LOG_DBG("######### sending %d\n",obs_counter);
	// Notify all the observers
	coap_notify_observers(&res_light);
}


