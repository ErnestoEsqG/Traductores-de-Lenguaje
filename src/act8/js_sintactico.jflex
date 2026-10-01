package act8;

import java_cup.runtime.Symbol;

%%

%class LexicoJava
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
InputCharacter = [^\r\n]
WhiteSpace     = {LineTerminator} | [ \t\f]

Comment              = {TraditionalComment} | {EndOfLineComment}
TraditionalComment   = "/*" [^*] ~"*/" | "/*" "*"+ "/"
EndOfLineComment     = "//" {InputCharacter}* {LineTerminator}?

Identifier        = [:jletter:] [:jletterdigit:]*
DecIntegerLiteral = 0 | [1-9][0-9]*
DecFloatLiteral   = [0-9]+ \. [0-9]+

%%

{Comment}            { /* Ignorar */ }
{WhiteSpace}         { /* Ignorar */ }

/* Palabras reservadas JavaScript */
"function"           { return token(sym.FUNCTION); }
"return"             { return token(sym.RETURN); }
"let"                { return token(sym.LET); }
"var"                { return token(sym.VAR); }
"const"              { return token(sym.CONST); }
"if"                 { return token(sym.IF); }
"else"               { return token(sym.ELSE); }
"while"              { return token(sym.WHILE); }
"for"                { return token(sym.FOR); }
"console.log"        { return token(sym.CONSOLE_LOG); }
"true"               { return token(sym.BOOLEAN_LIT, true); }
"false"              { return token(sym.BOOLEAN_LIT, false); }

/* Delimitadores */
"("                  { return token(sym.PARENTESIS_IZQ); }
")"                  { return token(sym.PARENTESIS_DER); }
"{"                  { return token(sym.LLAVE_IZQ); }
"}"                  { return token(sym.LLAVE_DER); }
","                  { return token(sym.COMA); }
";"                  { return token(sym.PUNTO_COMA); }
"="                  { return token(sym.ASIGNAR); }

/* Operadores */
"+"                  { return token(sym.SUMA); }
"-"                  { return token(sym.RESTA); }
"*"                  { return token(sym.MULT); }
"/"                  { return token(sym.DIV); }
"%"                  { return token(sym.MOD); }
"==="                { return token(sym.IGUAL_ESTRICTO); }
"=="                 { return token(sym.IGUAL); }
">"                  { return token(sym.MAYOR); }
"<"                  { return token(sym.MENOR); }

/* Literales e Identificadores */
{Identifier}         { return token(sym.ID, yytext()); }
{DecIntegerLiteral}  { return token(sym.NUM_INT, Integer.parseInt(yytext())); }
{DecFloatLiteral}    { return token(sym.NUM_FLOAT, Double.parseDouble(yytext())); }
\"[^\"]*\"           { return token(sym.CADENA, yytext()); }
\'[^\']*\'           { return token(sym.CADENA, yytext()); }

.                    { 
    System.err.println("Error léxico en línea " + (yyline+1) + ", columna " + (yycolumn+1) + ": caracter inesperado '" + yytext() + "'"); 
}