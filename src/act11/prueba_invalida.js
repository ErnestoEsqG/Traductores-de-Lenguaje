class CalculadoraErrores {
    let base: number = 10;

    sumar(a: number, b: number): number {
        return a + b;
    }

    // 1. Tipo de retorno incompatible (declara number pero retorna string)
    obtenerEdad(): number {
        return "veinticinco";
    }

    ejecutar(): void {
        let resultado: number = 0;
        let activo: boolean = false;

        // 2. Condición incompatible en if (se pasa number en vez de boolean)
        if (resultado) {
            let x: number = 1;
        }

        // 3. Cantidad incorrecta de argumentos (se envió 1 y requiere 2)
        resultado = sumar(10);

        // 4. Tipo incompatible en argumentos (esperaba number, se envió string)
        resultado = sumar("Hola", 20);

        // 5. Método o función no declarada
        resultado = multiplicar(2, 3);

        // 6. Uso incorrecto de break fuera de ciclo
        break;

        // 7. Variable fuera de ámbito
        {
            let valorLocal: number = 100;
        }
        resultado = valorLocal;

        // 8. Atributo no declarado
        resultado = limite;

        // 9. Declaración duplicada en el mismo ámbito
        let resultado: number = 50;
    }
}

// 10. Retorno fuera de función o método
return 100;