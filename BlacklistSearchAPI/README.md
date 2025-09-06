# Blacklist Search API (Spring Boot 3.x, Java 21)

## Laboratorio de Paralelismo — Parte I: Hilos en Java

Este ejercicio introduce los conceptos básicos de **hilos (threads)** en Java, su ciclo de vida y cómo interactúan con la API REST que construimos en este proyecto.

---

## 🎯 Objetivo del Taller

- Comprender cómo crear y ejecutar hilos en Java.
- Observar la diferencia entre `start()` y `run()`.
- Integrar la lógica de hilos en un **nuevo endpoint REST** y validarlo con pruebas unitarias.

---

## 📂 Estructura de Paquetes

```
src/
└── main/
    └── java/co/eci/blacklist/
        ├── api/                # Controladores REST
        ├── application/        # Servicios de aplicación
        ├── domain/             # Lógica de negocio
        ├── infrastructure/     # Configuración y fachada
        └── labs/part1/         # Ejercicios de hilos (Parte I)
        └── labs/part2/         # Ejercicios de hilos (Parte II)
```

---

## 🧩 Actividades Laboratorio parte 1

### Actividad 1: Clase `CountThread`

Cree una clase en `co.eci.blacklist.labs.part1` llamada **`CountThread`** que extienda `Thread`.  
Su método `run()` debe imprimir en consola todos los números dentro del rango `[A..B]`.

```java
public class CountThread extends Thread {
  private final int from;
  private final int to;

  public CountThread(int from, int to) {
    this.from = from;
    this.to = to;
    setName("CountThread-" + from + "-" + to);
  }

  @Override
  public void run() {
    for (int i = from; i <= to; i++) {
      System.out.println("[" + getName() + "] " + i);
    }
  }
}
```

---

### Actividad 2: Clase `CountMainThreads`

Cree una clase principal que instancie y ejecute varios hilos usando la clase `CountThread`.

```java
public class CountMainThreads {
  public static void main(String[] args) throws InterruptedException {
    CountThread t1 = new CountThread(0, 99);
    CountThread t2 = new CountThread(99, 199);
    CountThread t3 = new CountThread(200, 299);

    t1.start();
    t2.start();
    t3.start();

    t1.join();
    t2.join();
    t3.join();
  }
}
```

Cambie el incio con 'start()' por 'run()'. Cómo cambia la salida?, por qué?.

---

Cuando ejecutamos el programa con start() obtenemos el siguiente resultado:

<img width="698" height="753" alt="image" src="https://github.com/user-attachments/assets/4398344c-989c-4265-809d-7064f2e8753b" />

...


---


Cuando ejecutamos el programa con run() obtenemos el siguiente resultado:

<img width="947" height="758" alt="image" src="https://github.com/user-attachments/assets/36a5637f-d263-4825-9bcf-1f681b946c80" />

...

<img width="1019" height="753" alt="image" src="https://github.com/user-attachments/assets/8727b4ca-4627-4e2f-afec-5a8ab8c1644b" />

...

<img width="1059" height="756" alt="image" src="https://github.com/user-attachments/assets/83b9c9f1-23f3-4476-891f-7d92d0ddaf52" />

...

<img width="954" height="624" alt="image" src="https://github.com/user-attachments/assets/f439d249-dfda-4062-943d-f612e399b9a2" />

---



## 🧩 Actividades Laboratorio parte 2

# Parte II — Hilos en Java (Validación de Listas Negras)

## 🎯 Contexto

En un software de **vigilancia automática de seguridad informática**, se requiere validar direcciones IP en miles de **listas negras** conocidas y reportar aquellas que existan en al menos 5 de estas listas.

El componente se estructura así:

- **`HostBlackListsDataSourceFacade`**  
  Fachada para consultar si una IP está en alguna de las N listas negras (`isInBlacklistServer`).  
  También permite reportar cuando una IP es considerada peligrosa o confiable.

  > Esta clase **no es modificable**, pero es _thread-safe_.

- **`BlacklistChecker`**  
  Clase del dominio que implementa el método `checkHost(ip, nThreads)`.  
  Este método divide el espacio de servidores en segmentos y paraleliza la búsqueda usando hilos, siguiendo la política:
  - Si la IP aparece en **≥ 5 listas negras** → se reporta como **no confiable**.
  - En caso contrario → se reporta como **confiable**.  
    Además, retorna la lista de índices de listas negras donde se encontró la IP.

---

## 🧩 Actividad 1: Clase de Búsqueda Paralela

Cree una clase que represente el ciclo de vida de un hilo encargado de buscar en un **segmento de servidores**:

- Debe consultar el rango asignado de servidores con la fachada `HostBlackListsDataSourceFacade`.
- Guardar los índices de servidores donde aparece la IP.
- Permitir recuperar cuántas ocurrencias se encontraron.

En el proyecto actual esta lógica ya está **encapsulada en `BlacklistChecker`**, usando **virtual threads**.

> Para efectos pedagógicos, puede implementarse una versión simplificada con `Thread` clásico dentro del paquete `labs.part2`.

---

## 🧩 Actividad 2: Modificar `checkHost`

En el método `checkHost(String ip, int nThreads)`:

1. Divida el total de servidores entre **N segmentos**.
   - Recuerde manejar los casos donde N no divide de forma exacta.
2. Cree **N hilos** (uno por segmento).
3. Ejecute cada hilo con `start()` y espere con `join()`.
4. Sume los resultados:
   - Número total de servidores revisados.
   - Número de coincidencias encontradas.
5. Si las coincidencias son **≥ 5 (`BLACK_LIST_ALARM_COUNT`)**, reporte la IP como **no confiable**; de lo contrario, como **confiable**.
6. Mantenga el **LOG** que informa antes de retornar:  
   INFO: Checked blacklists: X of Y
   INFO: HOST 205.24.34.55 Reported as trustworthy
   INFO: HOST 205.24.34.55 Reported as NOT trustworthy

> En la API, este método ya existe en `BlacklistChecker` y se expone a través de `BlacklistController` en el endpoint:
>
> ```
> GET /api/v1/blacklist/check?ip={ipv4}&threads={n}
> ```

---

## 🧩 Actividad 3: Pruebas con IPs específicas

- **`200.24.34.55`** → Está registrada varias veces en los primeros servidores (la búsqueda termina rápido).
- **`202.24.34.55`** → Está registrada de forma **dispersa**, lo que hace más lenta la búsqueda.
- **`212.24.24.55`** → **No aparece** en ninguna lista negra (peor caso).

---

## 🗣️ Parte II.I — Discusión (NO implementar aún)

Actualmente, aunque se encuentre la IP en ≥ 5 listas negras, los hilos **siguen ejecutándose** hasta terminar su segmento.

**Pregunta para discusión:**  
¿Cómo se podría modificar la implementación para **detener la búsqueda tempranamente** cuando ya se alcanzó el umbral de 5 coincidencias?

- ¿Qué mecanismo de sincronización o bandera compartida sería necesario?
- ¿Qué implicaciones trae esto al control de la concurrencia?

---

# Parte III — Evaluación de Desempeño

Ahora que la búsqueda es paralela, evaluemos el impacto del número de hilos en el tiempo de ejecución.

## 🧩 Experimentos propuestos

Ejecute la validación de IP dispersa (`202.24.34.55`) con diferentes configuraciones de hilos:

1. **1 hilo**.
2. **Núcleos físicos del procesador** (use `Runtime.getRuntime().availableProcessors()`).
3. **El doble de núcleos**.
4. **50 hilos**.
5. **100 hilos**.

---

## 📊 Monitoreo con jVisualVM

1. Arranque la aplicación.
2. Ejecute los experimentos uno a uno.
3. Monitoree **CPU** y **memoria** en [jVisualVM](https://visualvm.github.io/).  
   ![](img/jvisualvm.png)
4. Anote los tiempos de ejecución.

---

## 📈 Análisis de resultados

Con los datos recolectados, grafique:

- **Eje X:** número de hilos
- **Eje Y:** tiempo de ejecución (ms)

## 📊 Ejemplo de Tabla de Resultados

| Número de Hilos | Tiempo de Ejecución (ms) |
| --------------- | ------------------------ |
| 1               | 1200                     |
| 4               | 400                      |
| 8               | 250                      |
| 16              | 220                      |
| 50              | 210                      |
| 100             | 215                      |

> _Reemplace los valores con los obtenidos en sus experimentos._

## 📉 Ejemplo de Gráfica

Puede graficar los resultados usando Excel, Google Sheets o herramientas como [plotly](https://plotly.com/).

![Ejemplo de gráfica de desempeño](img/performance_chart.png)
Discuta con su compañero:

1. Según la [Ley de Amdahl](https://es.wikipedia.org/wiki/Ley_de_Amdahl), ¿por qué el mejor desempeño no se logra con cientos de hilos (p. ej. 500)?  
   ![](img/ahmdahls.png)

2. ¿Qué ocurre al usar **número de hilos = núcleos** vs. **el doble de núcleos**?

3. ¿Qué pasaría si, en lugar de un solo equipo, se distribuye el trabajo en **100 máquinas** con un hilo cada una?  
   ¿Mejoraría el rendimiento?  
   ¿Cómo influye la **fracción paralelizable (P)** del problema?

---

## ✅ Conclusiones esperadas

- El problema de búsqueda en listas negras es un caso **vergonzosamente paralelo** (_embarrassingly parallel_).
- Aumentar hilos reduce el tiempo **hasta cierto punto**, pero hay un límite práctico por overhead.
- El desempeño óptimo suele alcanzarse con un número de hilos cercano al número de **núcleos disponibles**.

## 🚀 Documentación de la API Base - Endpoints Principales

- `GET /api/v1/blacklist/check?ip={ipv4}&threads={n}`  
  Respuesta:

  ```json
  {
    "ip": "200.24.34.55",
    "trustworthy": false,
    "matches": [0, 1, 2, 3, 4],
    "checkedServers": 123,
    "totalServers": 10000,
    "elapsedMs": 7,
    "threads": 8
  }
  ```

  - `threads`: si no se envía o es 0, usa `availableProcessors()`.
  - Umbral configurable (por defecto **5**) en `application.yaml` (`blacklist.alarm-count`).

- Actuator: `/actuator/health`, `/actuator/prometheus`.

---

## 🛠️ Ejecución Local

```bash
mvn spring-boot:run
# o
mvn -DskipTests package && java -jar target/blacklist-api-0.0.1-SNAPSHOT.jar
```

## 🐳 Docker

```bash
docker build -t blacklist-api:latest .
docker run --rm -p 8080:8080 blacklist-api:latest
```

Como podemos ver a continuación, el proyecto se ejcuta de forma correcta con Docker: 

<img width="1280" height="499" alt="image" src="https://github.com/user-attachments/assets/29216732-6345-4c3a-b535-e3e3ed4c1358" />

<img width="1212" height="754" alt="image" src="https://github.com/user-attachments/assets/420eea42-abe2-46cb-b614-b95d34876809" />

<img width="1228" height="703" alt="image" src="https://github.com/user-attachments/assets/62f3ce65-4753-46e0-ba83-92f0fcb41c59" />


---

## 📝 Notas de Diseño

- Java 21 con **Virtual Threads** (executor por tarea) para paralelismo eficiente.
- Corte temprano al alcanzar el umbral (5 por defecto).
- Validación estricta de IPv4.
- Métricas por Actuator/Micrometer.

---

## 🧪 Pruebas

- `BlacklistCheckerTest`: valida corte temprano y conteo de chequeos.
- `BlacklistControllerTest`: valida validación de IPv4 y flujo feliz.

---

## 🎯 Diagrama de componentes
A continuación se muestra el diagrama de componentes del proyecto:
![Diagrama de componentes](https://github.com/Sebs2807/lab3-arsw/blob/diagramas/BlacklistSearchAPI/img/Blacklist.jpeg)

## 📄 Licencia

MIT
