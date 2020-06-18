#include "contiki.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "RPL BR"
#define LOG_LEVEL LOG_LEVEL_INFO


PROCESS(router_process, "Border router");
AUTOSTART_PROCESSES(&router_process);

/*---------------------------------------------------------------------------*/
PROCESS_THREAD(router_process, ev, data){

  PROCESS_BEGIN();

#if BORDER_ROUTER_CONF_WEBSERVER
  PROCESS_NAME(webserver_nogui_process);
  process_start(&webserver_nogui_process, NULL);
#endif

  LOG_INFO("Border Router started\n");
  PROCESS_END();

}
