# Parte I — Productor/Consumidor con `wait/notify` (y contraste con busy-wait)

## Ejecutar con **busy-wait** (alto CPU)
```bash
mvn -q -DskipTests exec:java -Dexec.mainClass=edu.eci.arsw.pc.PCApp \
  -Dmode=spin -Dproducers=1 -Dconsumers=1 -Dcapacity=8 -DprodDelayMs=50 -DconsDelayMs=1 -DdurationSec=30
```

## Ejecutar con **monitores** (uso eficiente de CPU)
```bash
mvn -q -DskipTests exec:java -Dexec.mainClass=edu.eci.arsw.pc.PCApp \
  -Dmode=monitor -Dproducers=1 -Dconsumers=1 -Dcapacity=8 -DprodDelayMs=50 -DconsDelayMs=1 -DdurationSec=30
```

## Escenarios a validar
1) **Productor lento / Consumidor rápido** → consumidor debe **esperar sin CPU** cuando no hay elementos.  
2) **Productor rápido / Consumidor lento** con **límite de stock** → productor debe **esperar sin CPU** cuando la cola esté llena (capacidad pequeña, ej. 4 u 8).  
3) Visualiza CPU con **jVisualVM** y compara `mode=spin` vs `mode=monitor`.

## 🎯 Diagrama de componentes
A continuación se muestra el diagrama de componentes del proyecto:
![Diagrama de componentes](https://github.com/Sebs2807/lab3-arsw/blob/diagramas/Lab_busy_wait_vs_wait_notify/img/Notify.jpeg)
