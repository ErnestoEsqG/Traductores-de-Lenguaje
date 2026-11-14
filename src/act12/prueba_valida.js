// Actividad 12 - Prueba valida: todas las reglas de codigo intermedio

// 1. Asignacion de constantes a variables
let x = 5;
let y = 10;

// 2. Uso de variables
let z = x + y;

// 3. Expresion aritmetica con parentesis
const a = 2;
let b = 3, c = 4, d = 1, w = 7;
let result = a * (b + c);

// 4. Expresion logica y relacional
let flag = (x > y) && (z < 10);

// 5. Resultados intermedios
let total = (a + b) * (c - d);

// 6. Expresion anidada con division (divisor calculado)
result = (x + y) / (z - w);

// 7. Division entre una variable y entre una constante distinta de cero
result = a / b;
let mitad = total / 2;
let residuo = total % b;

// 8. Negativos, negacion logica, OR e igualdad estricta
let negativo = -x + -3;
let activo = !flag || (x === 5) && (y != z);

// 9. Cadenas: concatenacion y comparacion
var saludo = "Hola";
var mensaje = saludo + " mundo";
let iguales = mensaje == 'Hola mundo';

// 10. Copia entre variables y declaracion sin valor inicial
let copia;
copia = total;

// 11. Ambitos anidados
{
    let local = x * 2;
    {
        let interno = local + y - 1;
        total = interno;
    }
    z = local;
}
