package act6;

import java_cup.runtime.Symbol;

%%

%class LexicoAct6
%public
%unicode
%cup
%line
%column

%{
  private Symbol token(int type) {
    System.out.println("[" + (yyline + 1) + ":" + (yycolumn + 1) + "] Token: " + yytext());
    return new Symbol(type, yyline + 1, yycolumn + 1, yytext());
  }

  private Symbol token(int type, Object value) {
    System.out.println("[" + (yyline + 1) + ":" + (yycolumn + 1) + "] Token: " + value);
    return new Symbol(type, yyline + 1, yycolumn + 1, value);
  }
%}

LineTerminator = \r|\n|\r\n
WhiteSpace     = {LineTerminator} | [ \t\f]
Comment        = "/*" [^*] ~"*/" | "/*" "*"+ "/" | "//" [^\r\n]* {LineTerminator}?

Identifier        = [:jletter:] [:jletterdigit:]*
DecIntegerLiteral = 0 | [1-9][0-9]*
DecFloatLiteral   = (0 | [1-9][0-9]*) \. [0-9]+

%%

{WhiteSpace}         { /* Ignorar */ }
{Comment}            { /* Ignorar */ }

"function"           { return token(sym.FUNCTION); }
"leer"               { return token(sym.LEER); }
"escribir"           { return token(sym.ESCRIBIR); }
"true"               { return token(sym.VALOR, yytext()); }
"false"              { return token(sym.VALOR, yytext()); }

"="                  { return token(sym.IGUAL); }
"+"                  { return token(sym.MAS); }
"-"                  { return token(sym.MENOS); }
"*"                  { return token(sym.POR); }
"/"                  { return token(sym.ENTRE); }
"%"                  { return token(sym.MOD); }
"&&"                 { return token(sym.AND); }
"||"                 { return token(sym.OR); }
"!"                  { return token(sym.NOT); }
"("                  { return token(sym.PAR_IZQ); }
")"                  { return token(sym.PAR_DER); }
"{"                  { return token(sym.LLAVE_IZQ); }
"}"                  { return token(sym.LLAVE_DER); }
";"                  { return token(sym.PUNTO_COMA); }
","                  { return token(sym.COMA); }

\"([^\"\\]|\\.)*\"   { return token(sym.CADENA, yytext()); }
'([^'\\]|\\.)*'     { return token(sym.CADENA, yytext()); }
{DecFloatLiteral}    { return token(sym.VALOR, yytext()); }
{DecIntegerLiteral}  { return token(sym.VALOR, yytext()); }
{Identifier}         { return token(sym.IDENTIFICADOR, yytext()); }

.                    { System.err.println("Error léxico en Línea: " + (yyline + 1) + ", Columna: " + (yycolumn + 1) + " -> Carácter: '" + yytext() + "'"); }