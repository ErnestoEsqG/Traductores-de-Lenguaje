package act4;

import java_cup.runtime.Symbol;

%%

%class LexicoAct4
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

%%

{WhiteSpace}       { /* Ignorar */ }
{Comment}          { /* Ignorar */ }

/* Palabras reservadas */
"package"          { return token(sym.PACKAGE, yytext()); }
"import"           { return token(sym.IMPORT, yytext()); }
"public"           { return token(sym.PUBLIC, yytext()); }
"class"            { return token(sym.CLASS, yytext()); }

/* Delimitadores y puntuación */
";"                { return token(sym.PUNTO_COMA, yytext()); }
"."                { return token(sym.PUNTO, yytext()); }
"*"                { return token(sym.ASTERISCO, yytext()); }
"{"                { return token(sym.LLAVE_IZQ, yytext()); }
"}"                { return token(sym.LLAVE_DER, yytext()); }

/* Identificadores */
{Identifier}       { return token(sym.IDENTIFICADOR, yytext()); }

/* Desconocidos */
.                  { 
    System.err.println("Error léxico en línea " + (yyline + 1) + ", columna " + (yycolumn + 1) + ": token inválido '" + yytext() + "'");
}