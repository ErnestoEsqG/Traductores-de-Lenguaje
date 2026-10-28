class Calculadora {
    let base: number = 10;
    let activo: boolean = true;

    sumar(a: number, b: number): number {
        return a + b;
    }

    esMayor(valor: number): boolean {
        return valor > base;
    }

    ejecutar(): void {
        let resultado: number = 0;
        let condicion: boolean = false;

        resultado = sumar(10, 20);
        condicion = esMayor(resultado);

        if (condicion) {
            let mensaje: string = "Mayor";
        } else {
            let mensaje: string = "Menor o igual";
        }

        let contador: number = 0;
        while (contador < 3) {
            contador = contador + 1;
        }
        return;
    }
}