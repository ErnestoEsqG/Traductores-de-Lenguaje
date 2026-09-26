package act7;

import java_cup.runtime.Symbol;

%%

%class LexicoAct7
%public
%unicode
%cup
%line
%column

%{
  private Symbol token(int type) {
    return new Symbol(type, yyline + 1, yycolumn + 1, yytext());
  }

  private Symbol token(int type, Object value) {
    return new Symbol(type, yyline + 1, yycolumn + 1, value);
  }
%}

LineTerminator = \r|\n|\r\n
WhiteSpace     = {LineTerminator} | [ \t\f]
Comment        = "/*" [^*] ~"*/" | "/*" "*"+ "/" | "//" [^\r\n]* {LineTerminator}?

Identificador  = [:jletter:] [:jletterdigit:]*
Entero         = 0 | [1-9][0-9]*
Cadena         = \"([^\"\\]|\\.)*\"

%%

{Comment}            { /* Ignorar */ }
{WhiteSpace}         { /* Ignorar */ }

/* Palabras reservadas */
"void"               { return token(sym.VOID); }
"si"                 { return token(sym.SI); }
"sino"               { return token(sym.SINO); }
"segun"              { return token(sym.SEGUN); }
"caso"               { return token(sym.CASO); }
"defecto"            { return token(sym.DEFECTO); }
"romper"             { return token(sym.ROMPER); }
"mientras"           { return token(sym.MIENTRAS); }
"hacer"              { return token(sym.HACER); }
"para"               { return token(sym.PARA); }
"leer"               { return token(sym.LEER); }
"escribir"           { return token(sym.ESCRIBIR); }
"true"               { return token(sym.BOOLEANO, true); }
"false"              { return token(sym.BOOLEANO, false); }

/* Operadores lógicos y relacionales */
"&&"                 { return token(sym.AND); }
"||"                 { return token(sym.OR); }
"!"                  { return token(sym.NOT); }
"=="                 { return token(sym.IGUAL_IGUAL); }
"!="                 { return token(sym.DIFERENTE); }
"<="                 { return token(sym.MENOR_IGUAL); }
">="                 { return token(sym.MAYOR_IGUAL); }
"<"                  { return token(sym.MENOR); }
">"                  { return token(sym.MAYOR); }

/* Operadores aritméticos y asignación */
"="                  { return token(sym.ASIGNACION); }
"+"                  { return token(sym.MAS); }
"-"                  { return token(sym.MENOS); }
"*"                  { return token(sym.POR); }
"/"                  { return token(sym.DIV); }
"%"                  { return token(sym.MOD); }

/* Símbolos delimitadores */
"("                  { return token(sym.PAR_ABRE); }
")"                  { return token(sym.PAR_CIERRA); }
"{"                  { return token(sym.LLAVE_ABRE); }
"}"                  { return token(sym.LLAVE_CIERRA); }
":"                  { return token(sym.DOS_PUNTOS); }
";"                  { return token(sym.PUNTO_COMA); }
","                  { return token(sym.COMA); }

/* Literales */
{Entero}             { return token(sym.NUMERO, Integer.parseInt(yytext())); }
{Cadena}             { return token(sym.CADENA, yytext()); }
{Identificador}      { return token(sym.ID, yytext()); }

. {
    System.err.println("Error léxico en línea " + (yyline + 1) + ", columna " + (yycolumn + 1) + ": Carácter desconocido '" + yytext() + "'");
}