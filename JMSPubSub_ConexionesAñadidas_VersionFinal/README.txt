
Servicio de Publisher-Subscriber concurrente con JMS
    
Autores: Juan Antonio Ortega Aparicio
         César Borao Moratinos
                    
Fecha: 15/05/21
            
En esta segunda parte de la práctica final de IST hemos implementado un servicio de JMS publisher-subscriber de forma concurrente y asíncrono.
Para ello hemos implementado 4 clases : TestPubSubAsyncReceiver, PubSubAsyncReceiver, PubSubSender y TestPubSubSender.

- TestPubSubAsyncReceiver -

En esta clase se ha implementado un procedimiento principal mediante el cual lanzamos en un pool N subscriptores (N viene definido en la constante
NSUB en el código). Dichos subscriptores ejecutarán de forma concurrente el código implementado en la clase PubSubAsyncReceiver.
Finalmente esperaremos a que termine el pool de hilos lanzados, asegurando con la cláusura finally de que esto será asi pese a cualquier interrupción.

- PubSubAsyncReceiver -

Esta clase implementa la interfaz Runnable, y se encargará de conectar a cada subscriptor de nuestro patrón de diseño publisher-subscriber. 
Es en esta clase donde esperamos a recibir mensajes de algún publicador, incluido el mensaje de cierre (Dicho mensaje viene especificado en la
constante "STOP" y que además deberá coincidir con la constante "STOP" de la clase TestPubSubSender). Finalmente nos aseguramos de que la conexión
 de cada subscriptor se cierre mediante la cláusura finally.

- TestPubSubSender -

Esta clase es la que se encarga de establecer la conexión de publicador y de lanzar N publicadores concurrentes (N viene definido en la constante NPUB en el código) 
en nuestro patrón de diseño publisher/subscriber. Además garantiza el cierre de la conexión aunque haya alguna interrupción mediante la cláusura finally. 
Finalmente se publica el mensaje de cierre establecido en la constante "STOP" de nuestro código, que además debe coincidir con la constante "STOP" de la clase
PubSubAsyncReceiver tal y como hemos mencionado anteriormente.

- PubSubSender -

Esta clase implementa la interfaz Runnable, y se encargará de establecer la sesión de cada publicador de nuestro patrón de diseño publisher-subscriber. 
Es en esta clase donde enviamos los mensajes desde el lado del publicador (los mensajes en nuestro código son N números empezando en el 0). 
Finalmente nos aseguramos de que la sesión de cada publicador se cierre mediante la cláusura finally.
  