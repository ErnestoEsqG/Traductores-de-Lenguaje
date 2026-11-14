// Actividad 13 - Prueba valida: control de flujo, funciones y clases

// 1. if / else
let x = 8;
let y = 3;
let z = 0;
if (x > y) {
    z = x;
} else {
    z = y;
}

// 2. switch
let n = 2;
let result = "";
switch (n) {
    case 1:
        result = "Uno";
        break;
    case 2:
        result = "Dos";
        break;
    default:
        result = "Otro";
}

// 3. while
let i = 0;
while (i < 10) {
    i = i + 1;
}

// 4. for
let sum = 0;
for (let j = 0; j < 5; j++) {
    sum = sum + j;
}

// 5. do-while
let count = 0;
do {
    count = count + 1;
} while (count < 10);

// 6. Definicion y llamada de funciones
function suma(a, b) {
    return a + b;
}
let total = suma(5, 3);

// 7. Definicion de clase, creacion de objeto y llamada a metodo
class Persona {
    edad = 0;
    celebrarCumpleanios() {
        this.edad = this.edad + 1;
    }
}
let p = new Persona();
p.celebrarCumpleanios();

// 8. if sin else y condicion logica compuesta
if (x > 0 && y > 0) {
    z = x * y;
}

// 9. Funcion recursiva con if y return
function factorial(k) {
    if (k <= 1) {
        return 1;
    }
    return k * factorial(k - 1);
}
let f = factorial(5);

// 10. Estructuras anidadas con continue y break
let pares = 0;
for (let m = 0; m < 10; m++) {
    if (m % 2 != 0) {
        continue;
    }
    if (m > 6) {
        break;
    }
    pares += 1;
}

// 11. Clase con constructor, atributos y metodo con retorno
class Cuenta {
    saldo = 0;
    constructor(inicial) {
        this.saldo = inicial;
    }
    depositar(monto) {
        if (monto > 0) {
            this.saldo = this.saldo + monto;
        }
        return this.saldo;
    }
}
let cuenta = new Cuenta(100);
let saldoFinal = cuenta.depositar(50);
console.log(saldoFinal);
