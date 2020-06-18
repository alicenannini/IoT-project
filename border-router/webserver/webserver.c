#include "contiki.h"
#include "net/routing/routing.h"
#include "net/ipv6/uip-ds6-nbr.h"
#include "net/ipv6/uip-ds6-route.h"
#include "net/ipv6/uip-sr.h"

#include <stdio.h>
#include <string.h>

/*---------------------------------------------------------------------------*/
static const char *TOP = "<html>\n  <head>\n    <title>Contiki-NG</title>\n  </head>\n<body>\n";
static const char *BOTTOM = "\n</body>\n</html>\n";
static char buf[256];
static int blen;
#define ADD(...) do {                                                   \
    blen += snprintf(&buf[blen], sizeof(buf) - blen, __VA_ARGS__);      \
  } while(0)
#define SEND(s) do { \
  SEND_STRING(s, buf); \
  blen = 0; \
} while(0);

/* Use simple webserver with only one page for minimum footprint.
 * Multiple connections can result in interleaved tcp segments since
 * a single static buffer is used for all segments.
 */
#include "../border-router/webserver/httpd-simple.h"

/*---------------------------------------------------------------------------*/
static void
ipaddr_add(const uip_ipaddr_t *addr)
{
  uint16_t a;
  int i, f;
  for(i = 0, f = 0; i < sizeof(uip_ipaddr_t); i += 2) {
    a = (addr->u8[i] << 8) + addr->u8[i + 1];
    if(a == 0 && f >= 0) {
      if(f++ == 0) {
        ADD("::");
      }
    } else {
      if(f > 0) {
        f = -1;
      } else if(i > 0) {
        ADD(":");
      }
      ADD("%x", a);
    }
  }
}
/*---------------------------------------------------------------------------*/
static
PT_THREAD(generate_routes(struct httpd_state *s))
{
  static uip_ds6_nbr_t *nbr;

  PSOCK_BEGIN(&s->sout);
  SEND_STRING(&s->sout, TOP);

  ADD("  Neighbors\n  <ul>\n");
  SEND(&s->sout);
  for(nbr = uip_ds6_nbr_head();
      nbr != NULL;
      nbr = uip_ds6_nbr_next(nbr)) {
    ADD("    <li>");
    ipaddr_add(&nbr->ipaddr);
    ADD("</li>\n");
    SEND(&s->sout);
  }
  ADD("  </ul>\n");
  SEND(&s->sout);

#if (UIP_MAX_ROUTES != 0)
  {
    static uip_ds6_route_t *r;
    ADD("  Routes\n  <ul>\n");
    SEND(&s->sout);
    for(r = uip_ds6_route_head(); r != NULL; r = uip_ds6_route_next(r)) {
      ADD("    <li>");
      ipaddr_add(&r->ipaddr);
      ADD("/%u (via ", r->length);
      ipaddr_add(uip_ds6_route_nexthop(r));
      ADD(") %lus", (unsigned long)r->state.lifetime);
      ADD("</li>\n");
      SEND(&s->sout);
    }
    ADD("  </ul>\n");
    SEND(&s->sout);
  }
#endif /* UIP_MAX_ROUTES != 0 */

#if (UIP_SR_LINK_NUM != 0)
  if(uip_sr_num_nodes() > 0) {
    static uip_sr_node_t *link;
    ADD("  Routing links\n  <ul>\n");
    SEND(&s->sout);
    for(link = uip_sr_node_head(); link != NULL; link = uip_sr_node_next(link)) {
      if(link->parent != NULL) {
        uip_ipaddr_t child_ipaddr;
        uip_ipaddr_t parent_ipaddr;

        NETSTACK_ROUTING.get_sr_node_ipaddr(&child_ipaddr, link);
        NETSTACK_ROUTING.get_sr_node_ipaddr(&parent_ipaddr, link->parent);

        ADD("    <li>");
        ipaddr_add(&child_ipaddr);

        ADD(" (parent: ");
        ipaddr_add(&parent_ipaddr);
        ADD(") %us", (unsigned int)link->lifetime);

        ADD("</li>\n");
        SEND(&s->sout);
      }
    }
    ADD("  </ul>");
    SEND(&s->sout);
  }
#endif /* UIP_SR_LINK_NUM != 0 */

  SEND_STRING(&s->sout, BOTTOM);

  PSOCK_END(&s->sout);
}
/*---------------------------------------------------------------------------*/
PROCESS(webserver_nogui_process, "Web server");
PROCESS_THREAD(webserver_nogui_process, ev, data)
{
  PROCESS_BEGIN();

  httpd_init();

  while(1) {
    PROCESS_WAIT_EVENT_UNTIL(ev == tcpip_event);
    httpd_appcall(data);
  }

  PROCESS_END();
}
/*---------------------------------------------------------------------------*/
httpd_simple_script_t
httpd_simple_get_script(const char *name)
{
  return generate_routes;
}
/*---------------------------------------------------------------------------*/
