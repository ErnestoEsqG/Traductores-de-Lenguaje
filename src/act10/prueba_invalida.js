let contador = 5;
let bandera = false;
const LIMITE = 100;

function ejecutar() {
    let a;

    // Error 1: Operación aritmética inválida sobre booleano
    contador = bandera * 10;

    // Error 2: Condición con expresión numérica en lugar de booleana
    if (contador + 5) {
        console.log("Error");
    }

    // Error 3: Reasignación sobre una constante
    LIMITE = 200;

    // Error 4: Identificador no declarado en la expresión
    a = variableInexistente + 2;
}