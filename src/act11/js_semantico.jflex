package act11;

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
WhiteSpace     = {LineTerminator} | [ \t\f]
Comment        = "/*" [^*] ~"*/" | "/*" "*"+ "/" | "//" [^\r\n]* {LineTerminator}?
Identifier     = [:jletter:] [:jletterdigit:]*
DecInteger     = 0 | [1-9][0-9]*
StringLiteral  = \"([^\"\\\\]|\\\\.)*\"

%%

{Comment}            { /* Ignorar */ }
{WhiteSpace}         { /* Ignorar */ }

/* Palabras reservadas */
"class"              { return token(sym.CLASS); }
"function"           { return token(sym.FUNCTION); }
"return"             { return token(sym.RETURN); }
"if"                 { return token(sym.IF); }
"else"               { return token(sym.ELSE); }
"while"              { return token(sym.WHILE); }
"for"                { return token(sym.FOR); }
"break"              { return token(sym.BREAK); }
"continue"           { return token(sym.CONTINUE); }
"let"                { return token(sym.LET); }

/* Tipos */
"number"             { return token(sym.TIPO, TipoDato.NUMBER); }
"boolean"            { return token(sym.TIPO, TipoDato.BOOLEAN); }
"string"             { return token(sym.TIPO, TipoDato.STRING); }
"void"               { return token(sym.TIPO, TipoDato.VOID); }

/* Literales booleanas */
"true"               { return token(sym.LITERAL_BOOL, Boolean.TRUE); }
"false"              { return token(sym.LITERAL_BOOL, Boolean.FALSE); }

/* Delimitadores */
"{"                  { return token(sym.LLAVE_IZQ); }
"}"                  { return token(sym.LLAVE_DER); }
"("                  { return token(sym.PAR_IZQ); }
")"                  { return token(sym.PAR_DER); }
";"                  { return token(sym.PUNTO_COMA); }
","                  { return token(sym.COMA); }
"."                  { return token(sym.PUNTO); }
":"                  { return token(sym.DOS_PUNTOS); }

/* Operadores */
"="                  { return token(sym.ASIGNACION); }
"+"                  { return token(sym.MAS); }
"-"                  { return token(sym.MENOS); }
"*"                  { return token(sym.POR); }
"/"                  { return token(sym.DIV); }
">"                  { return token(sym.MAYOR); }
"<"                  { return token(sym.MENOR); }
">="                 { return token(sym.MAYOR_IGUAL); }
"<="                 { return token(sym.MENOR_IGUAL); }
"=="                 { return token(sym.IGUAL_IGUAL); }
"!="                 { return token(sym.DISTINTO); }

/* Literales e Identificadores */
{DecInteger}         { return token(sym.LITERAL_NUM, yytext()); }
{StringLiteral}      { return token(sym.LITERAL_STR, yytext()); }
{Identifier}         { return token(sym.ID, yytext()); }

.                    { System.err.println("Token desconocido en [" + (yyline+1) + ":" + (yycolumn+1) + "]: " + yytext()); }