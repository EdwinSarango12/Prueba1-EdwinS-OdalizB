# Prueba 1 Bimestre 1: Sistema de Pagos de Tarjetas del metro de Quito

Este proyecto implementa una arquitectura cliente-servidor basada en el protocolo UDP. La aplicación cuenta con un servidor central que administra los campos y evalúa las respuestas, junto con un cliente gráfico desarrollado en JavaFX para la interacción con el usuario final.

## Características Principales``
*   **Puerto utilizados: '5052'
*   **Comunicación UDP:** Uso de `DatagramSocket` y `DatagramPacket` para el intercambio de mensajes rápidos y sin conexión entre el cliente y el servidor.
*   **Interfaz Gráfica en JavaFX:** El cliente proporciona una experiencia visual intuitiva con botones, opciones de selección (RadioButtons) y alertas, asegurando que la aplicación no se bloquee gracias al uso de hilos secundarios.
*   **Sesiones de Evaluación Concurrentes:** El servidor mantiene un modelo basado en estados por la combinación de *IP:Puerto* de cada cliente, permitiendo que varios usuarios ingresen al sistema de manera simultánea sin cruzar datos.

## Estructura del proyecto``
<img width="355" height="747" alt="Captura de pantalla 2026-04-22 161902" src="https://github.com/user-attachments/assets/7c9d4ee8-9d09-4183-beb8-7d75dd10f7ef" />

## Requisitos del Sistema e Instalación

*   **Java DK (JDK 11 o superior):** Esencial para la compilación y ejecución.
*   **JavaFX:** Necesario para el entorno de ventanas del cliente (incluido típicamente en distribuciones como Liberica JDK o configurable mediante módulos de sistema).

## Ejecución del Proyecto

1.  **Levantar el Servidor UDP:** Ejecuta la clase `ServidorUDP.java`. Observarás el mensaje "Servidor UDP iniciado en puerto 1234".
2.  **Iniciar el Cliente(s):** Ejecuta la clase `ClienteApp.java`. Aparecerá la ventana de la interfaz gráfica.
3.  **Interactuar:**
    *   Haz clic en "Iniciar" para comenzar la sesión y recibir la aplicación y el ingreso de la cédula.
    *   Selecciona las respuestas correctas.
    *   Haz clic en buscar usuario o registrar uno nuevo
    *   Al concluir, verás tu calificación final en la interfaz.

## Autor
Edwin Sarango 
Odaliz Balseca
