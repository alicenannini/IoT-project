#ifndef PROJECT_CONF_H_
#define PROJECT_CONF_H_

/* Disabling TCP on CoAP nodes. */
#undef UIP_CONF_TCP
#define UIP_CONF_TCP 0   

/* Enable client-side support for COAP observe */
#define COAP_OBSERVE_CLIENT 1

/* Force button descriptions */
#define BUTTON_HAL_CONF_WITH_DESCRIPTION 1

#endif /* PROJECT_CONF_H_ */
