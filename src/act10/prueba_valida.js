let base = 10;
let factor = 2.5;
const PI = 3.1416;
let esActivo = true;

function ejecutar() {
    let resultado = base * 5;
    let calculoComplejo = (base + factor) * PI;
    let condicion = (resultado > 20) && esActivo;

    // Asignación válida en tipado dinámico: cambia a string sin error
    resultado = "Cálculo finalizado exitosamente";

    if (condicion) {
        console.log(resultado);
    }
}