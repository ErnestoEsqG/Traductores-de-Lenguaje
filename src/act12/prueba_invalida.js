// Actividad 12 - Prueba invalida: validaciones y errores

let a = 8;
let b = 0;
const CERO = 0;
let total = 0;
let result = 0;
let flag = true;

// 1. Type mismatch: operacion entre numero y cadena
total = a + "string";

// 2. Division por cero con divisor literal
result = a / 0;

// 3. Division por cero con divisor constante (const)
result = a % CERO;

// 4. Division entre variable: se valida en tiempo de ejecucion
result = a / b;

// 5. Type mismatch en operacion logica (boolean && number)
flag = flag && a;

// 6. Type mismatch en comparacion (number < string)
flag = a < "diez";

// 7. Type mismatch al asignar (number <- string)
total = "cien";

// 8. Variable no declarada
result = a + noDeclarada;

// 9. Identificador ya declarado en el mismo ambito
let a = 1;

// 10. Reasignacion de una constante
CERO = 5;

// 11. Constante sin valor inicial
const LIMITE;

// 12. Variable sin valor (undefined) usada en una operacion
let vacio;
result = vacio * 2;

// 13. Variable fuera de su ambito
{
    let local = 3;
}
result = local + 1;

// 14. Error sintactico: falta un operando
result = a + * b;

// 15. Error lexico: caracter no reconocido
total = a # 2;
