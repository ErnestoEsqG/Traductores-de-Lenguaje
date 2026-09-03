/*
PrimerApellido_SegundoApellido_Nombre(s)
*/
package lexico;

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
    System.out.printf("[Línea %-2d, Columna %-2d] Token: %-18s | Lexema: %s%n", 
                      yyline + 1, yycolumn + 1, symTerminalName(type), yytext());
    return new Symbol(type, yyline + 1, yycolumn + 1, yytext());
  }

  private Symbol token(int type, Object value) {
    System.out.printf("[Línea %-2d, Columna %-2d] Token: %-18s | Valor: %s%n", 
                      yyline + 1, yycolumn + 1, symTerminalName(type), value.toString());
    return new Symbol(type, yyline + 1, yycolumn + 1, value);
  }

  private String symTerminalName(int type) {
    try {
      java.lang.reflect.Field[] fields = sym.class.getFields();
      for (java.lang.reflect.Field f : fields) {
        if (f.getInt(null) == type) return f.getName();
      }
    } catch (Exception ignored) {}
    return String.valueOf(type);
  }
%}

/* Terminadores de línea y espacios */
LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
WhiteSpace     = {LineTerminator} | [ \t\f]

/* Comentarios (Tradicional y fin de línea) */
TraditionalComment   = "/*" [^*] ~"*/" | "/*" "*"+ "/"
EndOfLineComment     = "//" {InputCharacter}* {LineTerminator}?
Comment              = {TraditionalComment} | {EndOfLineComment}

/* Identificadores en JS: permiten letras, _, $ */
Identifier = [a-zA-Z_$][a-zA-Z0-9_$]*

/* Números (Enteros, punto flotante y notación exponencial) */
DecIntegerLiteral = 0 | [1-9][0-9]*
FloatLiteral      = [0-9]+\.[0-9]+([eE][+-]?[0-9]+)?

/* Cadenas de texto: comillas dobles, simples y template strings */
DoubleQuoteString = \"([^\"\\\r\n]|\\.)*\"
SingleQuoteString = \'([^\'\\\r\n]|\\.)*\'
TemplateLiteral   = \`([^`\\]|\\.)*\`

%%

/* Ignorar espacios y comentarios */
{WhiteSpace}         { /* Ignorar */ }
{Comment}            { /* Ignorar */ }

/* 50 Palabras Clave de JavaScript */
"break"              { return token(sym.BREAK); }
"case"               { return token(sym.CASE); }
"catch"              { return token(sym.CATCH); }
"class"              { return token(sym.CLASS); }
"const"              { return token(sym.CONST); }
"continue"           { return token(sym.CONTINUE); }
"debugger"           { return token(sym.DEBUGGER); }
"default"            { return token(sym.DEFAULT); }
"delete"             { return token(sym.DELETE); }
"do"                 { return token(sym.DO); }
"else"               { return token(sym.ELSE); }
"export"             { return token(sym.EXPORT); }
"extends"            { return token(sym.EXTENDS); }
"finally"            { return token(sym.FINALLY); }
"for"                { return token(sym.FOR); }
"function"           { return token(sym.FUNCTION); }
"if"                 { return token(sym.IF); }
"import"             { return token(sym.IMPORT); }
"in"                 { return token(sym.IN); }
"instanceof"         { return token(sym.INSTANCEOF); }
"new"                { return token(sym.NEW); }
"return"             { return token(sym.RETURN); }
"super"              { return token(sym.SUPER); }
"switch"             { return token(sym.SWITCH); }
"this"               { return token(sym.THIS); }
"throw"              { return token(sym.THROW); }
"try"                { return token(sym.TRY); }
"typeof"             { return token(sym.TYPEOF); }
"var"                { return token(sym.VAR); }
"void"               { return token(sym.VOID); }
"while"              { return token(sym.WHILE); }
"with"               { return token(sym.WITH); }
"yield"              { return token(sym.YIELD); }
"let"                { return token(sym.LET); }
"static"             { return token(sym.STATIC); }
"enum"               { return token(sym.ENUM); }
"await"              { return token(sym.AWAIT); }
"implements"         { return token(sym.IMPLEMENTS); }
"interface"          { return token(sym.INTERFACE); }
"package"            { return token(sym.PACKAGE); }
"private"            { return token(sym.PRIVATE); }
"protected"          { return token(sym.PROTECTED); }
"public"             { return token(sym.PUBLIC); }
"async"              { return token(sym.ASYNC); }
"of"                 { return token(sym.OF); }
"get"                { return token(sym.GET); }
"set"                { return token(sym.SET); }
"constructor"        { return token(sym.CONSTRUCTOR); }
"from"               { return token(sym.FROM); }
"as"                 { return token(sym.AS); }

/* Literales Booleanos y Especiales */
"true"               { return token(sym.BOOLEAN_LITERAL, true); }
"false"              { return token(sym.BOOLEAN_LITERAL, false); }
"null"               { return token(sym.NULL_LITERAL); }
"undefined"          { return token(sym.UNDEFINED_LITERAL); }

/* Operadores Compuestos de JavaScript */
"==="                { return token(sym.EXACT_EQ); }
"!=="                { return token(sym.EXACT_NEQ); }
"=="                 { return token(sym.EQ); }
"!="                 { return token(sym.NEQ); }
"<="                 { return token(sym.LEQ); }
">="                 { return token(sym.GEQ); }
"=>"                 { return token(sym.ARROW); }
"+="                 { return token(sym.PLUS_ASSIGN); }
"-="                 { return token(sym.MINUS_ASSIGN); }
"*="                 { return token(sym.MULT_ASSIGN); }
"/="                 { return token(sym.DIV_ASSIGN); }
"**"                 { return token(sym.EXP); }
"&&"                 { return token(sym.AND); }
"||"                 { return token(sym.OR); }

/* Operadores Simples */
"+"                  { return token(sym.PLUS); }
"-"                  { return token(sym.MINUS); }
"*"                  { return token(sym.MULT); }
"/"                  { return token(sym.DIV); }
"%"                  { return token(sym.MOD); }
"="                  { return token(sym.ASSIGN); }
"<"                  { return token(sym.LT); }
">"                  { return token(sym.GT); }
"!"                  { return token(sym.NOT); }
"?"                  { return token(sym.QUESTION); }

/* Símbolos y Delimitadores */
"("                  { return token(sym.LPAREN); }
")"                  { return token(sym.RPAREN); }
"["                  { return token(sym.LBRACK); }
"]"                  { return token(sym.RBRACK); }
"{"                  { return token(sym.LBRACE); }
"}"                  { return token(sym.RBRACE); }
";"                  { return token(sym.SEMICOLON); }
","                  { return token(sym.COMMA); }
"."                  { return token(sym.DOT); }
":"                  { return token(sym.COLON); }

/* Literales e Identificadores */
{DecIntegerLiteral}  { return token(sym.INT_LITERAL, Integer.parseInt(yytext())); }
{FloatLiteral}       { return token(sym.FLOAT_LITERAL, Double.parseDouble(yytext())); }
{DoubleQuoteString}  { return token(sym.STRING_LITERAL, yytext()); }
{SingleQuoteString}  { return token(sym.STRING_LITERAL, yytext()); }
{TemplateLiteral}    { return token(sym.TEMPLATE_LITERAL, yytext()); }
{Identifier}         { return token(sym.IDENTIFIER, yytext()); }

/* Errores Léxicos */
.                    { System.out.printf("[ERROR LÉXICO] Símbolo no reconocido en [Línea %d, Columna %d]: %s%n", 
                                          yyline + 1, yycolumn + 1, yytext()); }