# Actividad 10: Análisis Semántico — Verificación de Tipos (JavaScript)

Módulo de análisis semántico desarrollado en **Java** con **JFlex** y **JavaCUP** para un subconjunto del lenguaje **JavaScript**. Valida tipos en expresiones aritméticas, lógicas y relacionales, gestiona la inmutabilidad de constantes y reporta errores con línea y columna exactas.

---

## Estructura del Módulo (`src/act10/`)

* `Simbolo.java`: Estructura para identificadores (nombre, tipo actual e inmutabilidad)[cite: 3].
* `TablaSimbolos.java`: Gestión de ámbito, declaración y reasignación dinámica[cite: 3].
* `ManejadorTipos.java`: Matriz de compatibilidad y sistema de reporte de errores[cite: 3].
* `js_semantico.jflex`: Analizador léxico para JavaScript[cite: 3].
* `js_semantico.cup`: Gramática con acciones semánticas y propagación de tipos[cite: 3].
* `GeneradorJFlexLexico.java` / `GeneradorCupSintantico.java`: Generadores de código[cite: 3].
* `CompiladorMain.java`: Clase principal de ejecución de pruebas[cite: 3, 4].
* `prueba_valida.js` / `prueba_invalida.js`: Archivos de código fuente de prueba[cite: 3, 4].

---

## Reglas Semánticas Clave

* **Tipado Dinámico:** Las variables mutan de tipo con cada asignación válida (`number`, `string`, `boolean`).
* **Inmutabilidad:** Las variables declaradas con `const` no admiten reasignación[cite: 4].
* **Operaciones Aritméticas:** `*`, `/`, `-`, `%` exigen operandos numéricos; `+` suma números o concatena si interviene un `string`.
* **Condiciones y Lógica:** `if`, `&&`, `||` y `!` exigen estrictamente expresiones de tipo `boolean`[cite: 4].

---

## Compilación y Ejecución

1. Ejecutar `GeneradorJFlexLexico.java` para compilar el lexer[cite: 3].
2. Ejecutar `GeneradorCupSintantico.java` para compilar el parser y la tabla de símbolos[cite: 3].
3. Ejecutar `CompiladorMain.java` para correr las pruebas[cite: 3, 4].

### Ejecución desde Terminal

```bash
javac -cp ".:lib/*" -d bin src/act10/*.java
java -cp "bin:lib/*" act10.CompiladorMain


Resultados Obtenidos
1. prueba_valida.js
Plaintext
Analizando: src/act10/prueba_valida.js
>> ÉXITO: 0 errores semánticos detectados.
Evaluación correcta de operaciones aritméticas, lógicas y reasignación dinámica a string[cite: 4].

2. prueba_invalida.js
Plaintext
Analizando: src/act10/prueba_invalida.js
Error semántico en línea 9, columna 16: El operador aritmético '*' no es aplicable a los operandos de tipo 'boolean' y 'number'.
Error semántico en línea 12, columna 20: La expresión dentro de la condición 'if' debe ser de tipo 'boolean', se encontró 'number'.
Error semántico en línea 17, columna 5: No se puede reasignar un valor a la constante 'LIMITE'.
Error semántico en línea 20, columna 9: El identificador 'variableInexistente' no ha sido declarado en este ámbito.