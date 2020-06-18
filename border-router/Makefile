CONTIKI_PROJECT = border-router
all: $(CONTIKI_PROJECT)
CFLAGS += -DPROJECT_CONF_H=\"project-conf.h\"
CONTIKI = ../..

# The BR is either native or embedded, and in the latter case must support SLIP
PLATFORMS_EXCLUDE = nrf52dk z1

# Include RPL BR module
MODULES += $(CONTIKI_NG_SERVICES_DIR)/rpl-border-router
# Include webserver module
MODULES_REL += webserver
# Include optional target-specific module
include $(CONTIKI)/Makefile.identify-target
MODULES_REL += $(TARGET)

include $(CONTIKI)/Makefile.include
