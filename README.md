# IoT-project
IoT project based on Cooja simulations in Contiki and a java application with Californium


1. Start the Cooja simulation

2. From terminal:
  - Start contiki-ng:
    > contikier
  - Go in the border router directory:
    > cd border-router
  - Launch:
    > make TARGET=cooja connect-router-cooja
  
3. Start the java application:
    > cd app
  
    > java -jar target/APPLICATION.jar 
