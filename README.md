# Sistema Distribuido de Gestión de Inventario

## 1. Introducción

Este documento describe el diseño e implementación de un sistema distribuido de gestión de inventario para una cadena de tiendas minoristas. El objetivo principal es optimizar la **consistencia del inventario**, reducir la **latencia en las actualizaciones de stock**, disminuir los **costos operativos** y asegurar la **seguridad** y **observabilidad** del sistema.

Se realizó mediante una arquitectura de microservicios basada en **Java (Spring WebFlux)**, con uso intensivo de eventos mediante **Apache Kafka**.

---

## 2. Arquitectura General del Sistema

El sistema se compone de los siguientes microservicios:

- **Inventory Service (centralizado)**: administra el inventario global y publica eventos Kafka.
- **Order Service (centralizado)**: gestiona el flujo de órdenes y se comunica con Inventory.
- **Store Service (por tienda)**: mantiene una réplica local del inventario y permite realizar compras.
- **Auth Service (centralizado)**: mínima implementación de registro de usuario, para demostración de funcionalidad de JWT.

![Diagrama de componentes](./diagrams/components.png)

Kafka maneja los siguientes tópicos:

- `new-category`
- `new-inventory`
- `inventory-updated`

---

## 3. Funcionamiento del Sistema

### 3.1 Sincronización del Inventario

Cada tienda cuenta con un servicio local que actúa como consumer de los tópicos de Kafka: **new-category**, **new-product** e **inventory-updated**. Estos tópicos permiten que la base de datos local en cada tienda se mantenga actualizada en tiempo real con los cambios realizados desde el inventario centralizado.
Cuando se registra una nueva categoría o un nuevo producto desde el Inventory Service, se publica un evento en Kafka que las tiendas consumen para insertar los datos correspondientes en su base local.
Cuando se realiza una compra, el Inventory Service actualiza el stock global y emite uno o varios eventos en el tópico **inventory-updated**, indicando el nuevo estado del inventario.
Cada Store Service, al estar suscrito a estos tópicos, captura estos eventos y actualiza inmediatamente su base de datos local, manteniéndola sincronizada con el estado central.
Este mecanismo elimina la necesidad de procesos periódicos de sincronización (que antes se realizaban cada 15 minutos) y permite una arquitectura event-driven, más eficiente, resiliente y en tiempo casi real. De esta forma, se logra una consistencia eventual con muy baja latencia, lo cual es clave para evitar ventas de productos sin stock o inconsistencias de disponibilidad.
La sincronización inmediata del stock permite que los usuarios y clientes finales consulten disponibilidad con una mayor confiabilidad, mejorando significativamente la experiencia de usuario.

![Diagrama de flujo](./diagrams/flow.png)

### 3.2 Flujo de Compra

1. Store valida stock local.
2. Solicita orden a Order Service.
3. Order valida con Inventory y reserva stock.
4. Inventory publica evento `inventory-updated`.
5. Order persiste orden.
6. Store puede consultar el historial de órdenes.

![Diagrama de secuencia](./diagrams/sequence.png)

---

## 4. Decisiones Arquitectónicas

| Elemento                       | Justificación                                                 |
| ------------------------------ | ------------------------------------------------------------- |
| **Microservicios**             | Separación de responsabilidades, escalabilidad independiente. |
| **Kafka**                      | Comunicación asíncrona y sincronización en tiempo real.       |
| **Bases locales**              | Alta disponibilidad y operación offline temporal.             |
| **WebFlux**                    | Asincronía eficiente y manejo reactivo.                       |
| **Retry en fachadas**          | Mayor tolerancia a fallos.                                    |
| **Excepciones personalizadas** | Mejor trazabilidad de errores.                                |
| **Pods redundantes**           | Alta disponibilidad en Inventory y Orders.                    |

---

## 5. Requisitos No Funcionales Cumplidos

| Categoría               | Implementación                                                 |
| ----------------------- | -------------------------------------------------------------- |
| **Consistencia**        | Confirmación en Inventory antes de cerrar orden.               |
| **Baja latencia**       | Consumo en tiempo real de eventos Kafka.                       |
| **Observabilidad**      | Excepciones con códigos personalizados y logging estructurado. |
| **Tolerancia a fallos** | Retry en llamadas entre microservicios.                        |
| **Seguridad**           | JWT básico simulado.                                           |
| **Escalabilidad**       | Servicios desacoplados y replicables.                          |
| **Documentación**       | Diagramas UML, README y estructura clara.                      |

---

## 6. Stack Tecnológico

- Java 21
- Spring WebFlux
- Apache Kafka
- MySQL
- Maven
- JWT (básico)
- Redis (futuro)
- GenAI

## 8. Conclusión y Propuestas Futuras

El sistema cumple con los objetivos propuestos y mejora significativamente la operación del inventario distribuido. La transición desde una arquitectura monolítica hacia un modelo de microservicios permitió resolver problemas estructurales del sistema anterior, como la latencia en las actualizaciones, las inconsistencias de stock, y la baja tolerancia a fallos.

Mediante el uso de Kafka como sistema de mensajería y la implementación de un modelo event-driven, se logró una sincronización casi en tiempo real entre el inventario central y las tiendas locales, sin necesidad de sincronización periódica. Esto garantiza una experiencia de usuario más confiable y mejora los indicadores de disponibilidad de producto.

La adopción de Spring WebFlux y programación reactiva permitió una gestión más eficiente de recursos y una alta capacidad de concurrencia, mientras que las fachadas con retry automático y las excepciones personalizadas aportaron robustez y trazabilidad al sistema.

Asimismo, se establecieron las bases para una futura expansión del sistema con características como seguridad avanzada (OAuth2), monitorización distribuida (Grafana/Prometheus) y despliegue escalable (Kubernetes). El diseño modular y desacoplado asegura que cada componente pueda evolucionar o escalar independientemente, favoreciendo la mantenibilidad y la innovación continua.

## Futuras mejoras:

Para garantizar la evolución continua del sistema y su capacidad de adaptación a nuevos escenarios de escalabilidad, resiliencia y seguridad, se proponen las siguientes mejoras estructurales:

### 1. Arquitectura Evolutiva y Tolerancia a Fallos

**Kafka distribuido y descentralizado por nodo de tienda:** Se propone transicionar hacia una arquitectura donde cada instancia del Store Service opere con su propio nodo de Kafka local. Estos nodos estarán configurados como parte de un único cluster global federado, asegurando:

- Alta disponibilidad de mensajería incluso ante fallos de red o caída de nodos centrales.
- Independencia operativa de cada tienda, con capacidad de reintentar sincronización en caso de reconexión.
- Latencia mínima en la escritura y consumo de eventos locales.

**Replicación y balanceo de carga en servicios críticos:** Se planea la implementación de múltiples réplicas para los pods del Inventory Service y Order Service, acompañados de un load balancer configurado con políticas de failover. Esto permitirá distribuir eficientemente la carga de trabajo y garantizar continuidad operativa ante fallos de instancias individuales.

### 2. Seguridad Avanzada y Control de Acceso

**Sistema de suscripción por tienda con autenticación por API Key:** Cada tienda deberá registrarse en el sistema para obtener una API Key única que la identifique como cliente autorizado del sistema central. Esta clave será requerida tanto para consumir eventos desde Kafka como para interactuar con el Order Service, permitiendo un control granular de acceso.

- Las API Keys se gestionarán mediante un servicio de autorización simple (basado inicialmente en JWT y extensible a OAuth2).
- Futura integración con OAuth2 y servidor de identidad:

### 3. Observabilidad y Monitorización Distribuida

- Trazabilidad distribuida con Prometheus, Grafana y/o Datadog:
- Integración con herramientas como Micrometer para instrumentar métricas personalizadas y trazas distribuidas.
- Monitorización de health-checks, métricas de consumo de Kafka, uso de recursos y errores por servicio.
- Visualización unificada en Grafana y alertas automatizadas mediante Prometheus AlertManager.
- Registro de eventos clave del flujo de compra e inventario (creación, modificación, fallos, reintentos).
- Persistencia de logs estructurados y seguimiento temporal para análisis retrospectivo.

### 4. Resiliencia Operativa y Modo Offline

**Modo offline con re-sincronización automática:** Las tiendas podrán seguir operando temporalmente incluso si pierden conexión con el sistema central. Al restablecer la conexión, los eventos acumulados (tanto locales como pendientes del sistema central) serán sincronizados automáticamente mediante mecanismos de retry con respaldo en disco o memoria.

### 5. Escalabilidad y Despliegue Orquestado

- Contenedorización avanzada y despliegue en Kubernetes.
- Habilitación de políticas de autoscaling horizontal y vertical.
- Configuración de nodos tolerantes a fallos para servicios con mayor carga (Inventory y Orders).

## Apoyo de Herramientas de Generación de Inteligencia Artificial (GenAI)

Durante el desarrollo del sistema distribuido, se utilizó activamente el apoyo de herramientas de Inteligencia Artificial Generativa como asistente técnico para acelerar la codificación y reducir errores en la construcción de capas del sistema.

En particular, se aprovecharon estas herramientas para:

- Generar rápidamente controladores REST bien documentados y estructurados.
- Producir clases de servicios con anotaciones y convenciones.
- Escribir pruebas unitarias con mocking eficiente de dependencias.
- Documentar la arquitectura y explicar decisiones técnicas de forma clara y profesional.

Estas herramientas permitieron una mayor agilidad en el desarrollo, validación y documentación de los microservicios, así como una reducción significativa en el tiempo necesario para tareas repetitivas o estructurales dando una mayor productividad, coherencia en la arquitectura, y mejor trazabilidad del código, sin sacrificar el control humano sobre el diseño ni la calidad del sistema.
