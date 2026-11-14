/*
 * Camarena Velazquez Alberto Dario
 * Esqueda Guzman Ernesto
 * Nava Gutierrez Alan Santiago
 */
package act13;

import java_cup.runtime.Symbol;

%%

%class LexicoJava
%public
%unicode
%cup
%line
%column

%{
  /** Receptor de errores léxicos; lo asigna CompiladorMain. */
  public ControladorSemantico sem;

  private Symbol token(int type) {
    return new Symbol(type, yyline + 1, yycolumn + 1, yytext());
  }
  private Symbol token(int type, Object value) {
    return new Symbol(type, yyline + 1, yycolumn + 1, value);
  }
%}

%eofval{
  return new Symbol(sym.EOF, yyline + 1, yycolumn + 1, "fin de archivo");
%eofval}

LineTerminator = \r|\n|\r\n
WhiteSpace     = {LineTerminator} | [ \t\f]
Comment        = "/*" ~"*/" | "//" [^\r\n]*
Identifier     = [A-Za-z_$][A-Za-z0-9_$]*
Number         = [0-9]+ ("." [0-9]+)?
StringDouble   = \" ([^\"\\\r\n] | \\.)* \"
StringSingle   = \' ([^\'\\\r\n] | \\.)* \'

%%

{Comment}            { /* Ignorar */ }
{WhiteSpace}         { /* Ignorar */ }

/* Palabras reservadas */
"let"                { return token(sym.LET); }
"const"              { return token(sym.CONST); }
"var"                { return token(sym.VAR); }
"if"                 { return token(sym.IF); }
"else"               { return token(sym.ELSE); }
"switch"             { return token(sym.SWITCH); }
"case"               { return token(sym.CASE); }
"default"            { return token(sym.DEFAULT); }
"while"              { return token(sym.WHILE); }
"for"                { return token(sym.FOR); }
"do"                 { return token(sym.DO); }
"break"              { return token(sym.BREAK); }
"continue"           { return token(sym.CONTINUE); }
"function"           { return token(sym.FUNCTION); }
"return"             { return token(sym.RETURN); }
"class"              { return token(sym.CLASS); }
"new"                { return token(sym.NEW); }
"this"               { return token(sym.THIS); }
"true"               { return token(sym.LITERAL_BOOL, yytext()); }
"false"              { return token(sym.LITERAL_BOOL, yytext()); }

/* Delimitadores */
"{"                  { return token(sym.LLAVE_IZQ); }
"}"                  { return token(sym.LLAVE_DER); }
"("                  { return token(sym.PAR_IZQ); }
")"                  { return token(sym.PAR_DER); }
";"                  { return token(sym.PUNTO_COMA); }
","                  { return token(sym.COMA); }
":"                  { return token(sym.DOS_PUNTOS); }
"."                  { return token(sym.PUNTO); }

/* Operadores relacionales y de igualdad */
"==="                { return token(sym.IGUAL_ESTRICTO); }
"!=="                { return token(sym.DISTINTO_ESTRICTO); }
"=="                 { return token(sym.IGUAL_IGUAL); }
"!="                 { return token(sym.DISTINTO); }
">="                 { return token(sym.MAYOR_IGUAL); }
"<="                 { return token(sym.MENOR_IGUAL); }
">"                  { return token(sym.MAYOR); }
"<"                  { return token(sym.MENOR); }

/* Operadores lógicos */
"&&"                 { return token(sym.AND); }
"||"                 { return token(sym.OR); }
"!"                  { return token(sym.NOT); }

/* Incremento, decremento y asignación compuesta */
"++"                 { return token(sym.INCREMENTO); }
"--"                 { return token(sym.DECREMENTO); }
"+="                 { return token(sym.ASIGNACION_COMPUESTA, yytext()); }
"-="                 { return token(sym.ASIGNACION_COMPUESTA, yytext()); }
"*="                 { return token(sym.ASIGNACION_COMPUESTA, yytext()); }
"/="                 { return token(sym.ASIGNACION_COMPUESTA, yytext()); }

/* Asignación y operadores aritméticos */
"="                  { return token(sym.ASIGNACION); }
"+"                  { return token(sym.MAS); }
"-"                  { return token(sym.MENOS); }
"*"                  { return token(sym.POR); }
"/"                  { return token(sym.DIV); }
"%"                  { return token(sym.MOD); }

/* Literales e identificadores */
{Number}             { return token(sym.LITERAL_NUM, yytext()); }
{StringDouble}       { return token(sym.LITERAL_STR, yytext()); }
{StringSingle}       { return token(sym.LITERAL_STR, yytext()); }
{Identifier}         { return token(sym.ID, yytext()); }

[^]                  { if (sem != null) { sem.errorLexico(yyline + 1, yycolumn + 1, yytext()); } }
