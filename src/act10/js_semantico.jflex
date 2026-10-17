package act10;

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

TraditionalComment   = "/*" [^*] ~"*/" | "/*" "*"+ "/"
EndOfLineComment     = "//" {InputCharacter}* {LineTerminator}?
CommentContent       = ( [^*] | \*+ [^/*] )*
DocumentationComment = "/**" {CommentContent} "*"+ "/"
Comment = {TraditionalComment} | {EndOfLineComment} | {DocumentationComment}

Identifier = [:jletter:] [:jletterdigit:]*

DecIntegerLiteral = 0 | [1-9][0-9]*
DecFloatLiteral   = [0-9]+ \. [0-9]+
StringLiteral     = \"([^\"\\\r\n]|\\.)*\"

%%

<YYINITIAL> {
  {Comment}            { /* Ignorar */ }
  {WhiteSpace}         { /* Ignorar */ }

  /* Palabras reservadas JavaScript */
  "let"                { return token(sym.LET); }
  "var"                { return token(sym.VAR); }
  "const"              { return token(sym.CONST); }
  "function"           { return token(sym.FUNCTION); }
  "if"                 { return token(sym.IF); }
  "else"               { return token(sym.ELSE); }
  "console.log"        { return token(sym.CONSOLE_LOG); }

  /* Booleanos */
  "true"               { return token(sym.LIT_BOOLEAN, "true"); }
  "false"              { return token(sym.LIT_BOOLEAN, "false"); }

  /* Operadores aritméticos */
  "+"                  { return token(sym.MAS, "+"); }
  "-"                  { return token(sym.MENOS, "-"); }
  "*"                  { return token(sym.POR, "*"); }
  "/"                  { return token(sym.DIV, "/"); }
  "%"                  { return token(sym.MOD, "%"); }

  /* Operadores relacionales y de igualdad */
  "==="                { return token(sym.IGUALDAD_ESTRICTA, "==="); }
  "!=="                { return token(sym.DIFERENTE_ESTRICTO, "!=="); }
  ">="                 { return token(sym.MAYORIGUAL, ">="); }
  "<="                 { return token(sym.MENORIGUAL, "<="); }
  ">"                  { return token(sym.MAYOR, ">"); }
  "<"                  { return token(sym.MENOR, "<"); }

  /* Operadores lógicos */
  "&&"                 { return token(sym.AND, "&&"); }
  "||"                 { return token(sym.OR, "||"); }
  "!"                  { return token(sym.NOT, "!"); }

  /* Delimitadores */
  "="                  { return token(sym.ASIGNACION, "="); }
  ";"                  { return token(sym.PUNTOYCOMA); }
  ","                  { return token(sym.COMA); }
  "("                  { return token(sym.PAR_ABRE); }
  ")"                  { return token(sym.PAR_CIERRA); }
  "{"                  { return token(sym.LLAVE_ABRE); }
  "}"                  { return token(sym.LLAVE_CIERRA); }

  /* Literales e Identificadores */
  {DecFloatLiteral}    { return token(sym.LIT_NUMBER, yytext()); }
  {DecIntegerLiteral}  { return token(sym.LIT_NUMBER, yytext()); }
  {StringLiteral}      { return token(sym.LIT_STRING, yytext()); }
  {Identifier}         { return token(sym.IDENTIFICADOR, yytext()); }

  .                    { System.err.println("Error léxico [" + (yyline+1) + ":" + (yycolumn+1) + "]: Carácter inválido '" + yytext() + "'"); }
}