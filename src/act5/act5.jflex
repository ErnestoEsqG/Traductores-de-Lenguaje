package act5;

import java_cup.runtime.Symbol;

%%

%class LexicoAct5
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

Identifier     = [:jletter:] [:jletterdigit:]*

Entero         = 0 | [1-9][0-9]*
Flotante       = [0-9]+ \. [0-9]+ ([eE][+-]?[0-9]+)? [fFdD]? | [0-9]+ [fFdD]
Cadena         = \"([^\"\\\r\n]|\\.)*\"
Caracter       = \'([^\'\\\r\n]|\\.)\'
Booleano       = "true" | "false"

%%

{WhiteSpace}       { /* Ignorar */ }
{Comment}          { /* Ignorar */ }

/* Palabras reservadas estructurales */
"package"          { return token(sym.PACKAGE, yytext()); }
"import"           { return token(sym.IMPORT, yytext()); }
"public"           { return token(sym.PUBLIC, yytext()); }
"class"            { return token(sym.CLASS, yytext()); }
"final"            { return token(sym.FINAL, yytext()); }

/* Tipos de datos */
"int"              { return token(sym.INT, yytext()); }
"float"            { return token(sym.FLOAT, yytext()); }
"double"           { return token(sym.DOUBLE, yytext()); }
"char"             { return token(sym.CHAR, yytext()); }
"boolean"          { return token(sym.BOOLEAN, yytext()); }
"String"           { return token(sym.STRING_TIPO, yytext()); }

/* Delimitadores y puntuación */
";"                { return token(sym.PUNTO_COMA, yytext()); }
","                { return token(sym.COMA, yytext()); }
"="                { return token(sym.IGUAL, yytext()); }
"."                { return token(sym.PUNTO, yytext()); }
"*"                { return token(sym.ASTERISCO, yytext()); }
"{"                { return token(sym.LLAVE_IZQ, yytext()); }
"}"                { return token(sym.LLAVE_DER, yytext()); }

/* Literales */
{Booleano}         { return token(sym.VALOR_BOOLEANO, yytext()); }
{Entero}           { return token(sym.NUMERO_ENTERO, yytext()); }
{Flotante}         { return token(sym.NUMERO_FLOTANTE, yytext()); }
{Cadena}           { return token(sym.LITERAL_CADENA, yytext()); }
{Caracter}         { return token(sym.LITERAL_CARACTER, yytext()); }

/* Identificadores */
{Identifier}       { return token(sym.IDENTIFICADOR, yytext()); }

/* Caracter desconocido */
.                  { 
    System.err.println("Error léxico en línea " + (yyline + 1) + ", columna " + (yycolumn + 1) + ": caracter desconocido '" + yytext() + "'");
}