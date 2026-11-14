// Actividad 13 - Prueba invalida: validaciones en control de flujo, funciones y clases

let a = 5;
let nombre = "Ana";
let b = 0;

function suma(p, q) {
    return p + q;
}

class Persona {
    edad = 0;
    cumplir() {
        this.edad = this.edad + 1;
    }
}
let persona = new Persona();

// 1. Condicion de if que no es booleana
if (a + 1) {
    b = 1;
}

// 2. Condicion de while que no es booleana
while (nombre) {
    b = 2;
}

// 3. break fuera de un bucle o switch
break;

// 4. continue fuera de un bucle
continue;

// 5. return fuera de una funcion
return a;

// 6. Funcion no declarada
b = restar(a, 1);

// 7. Numero incorrecto de argumentos
b = suma(a);

// 8. Caso de switch con tipo distinto y default repetido
switch (a) {
    case "uno":
        b = 1;
        break;
    default:
        b = 2;
    default:
        b = 3;
}

// 9. Clase no declarada
let animal = new Animal();

// 10. Metodo y atributo inexistentes
persona.saludar();
b = persona.altura;

// 11. this fuera de un metodo de clase
b = this.edad;

// 12. Llamar a una variable como funcion y acceder a miembros de un numero
b = a(1);
b = a.edad;

// 13. Division por cero y Type mismatch dentro de un bucle
for (let i = 0; i < 3; i++) {
    b = a / 0;
    b = a - nombre;
}

// 14. Asignar a una funcion
suma = 3;

// 15. Error sintactico: falta la condicion del if
if () {
    b = 4;
}
