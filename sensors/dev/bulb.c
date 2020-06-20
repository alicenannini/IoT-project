#include "bulb.h"
#include "contiki.h"
#include <stdbool.h>

static bool automatic = 1;
static bool BULB = 0;
process_event_t AUTOMATIC_BULB_EVENT = NULL;
/* Node process */
extern struct process node_process;

void init_bulb(){
	AUTOMATIC_BULB_EVENT = process_alloc_event();
}

void switch_bulb_on(){
	
		BULB = 1;
	
}

void switch_bulb_off(){

		BULB = 0;

}

bool get_bulb_mode(){

	return BULB;

}

bool bulb_is_on(){

	return BULB == 1;

}

bool bulb_is_off(){

	return BULB == 0;

}

bool bulb_is_automatic(){

	return automatic;

}

void set_bulb_automatic(){

	automatic = 1;
	process_post(&node_process,AUTOMATIC_BULB_EVENT,NULL);

}

void set_bulb_manual(){

	automatic = 0;

}

