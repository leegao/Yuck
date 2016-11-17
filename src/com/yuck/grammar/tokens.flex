package com.yuck.grammar;

%%

%class YuckyLexer
%unicode
%line
%column
%type Token

%{
  StringBuffer string = new StringBuffer();

  private Token symbol(String type) {
    return new Token(type, yyline, yycolumn, yytext());
  }
  private Token symbol(String type, String value) {
    return new Token(type, yyline, yycolumn, value);
  }
%}

LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
WhiteSpace     = {LineTerminator} | [ \t\f]

/* comments */
Comment = {TraditionalComment} | {EndOfLineComment} | {DocumentationComment}

TraditionalComment   = "/*" [^*] ~"*/" | "/*" "*"+ "/"
// Comment can be the last line of the file, without line terminator.
EndOfLineComment     = "//" {InputCharacter}* {LineTerminator}?
DocumentationComment = "/**" {CommentContent} "*"+ "/"
CommentContent       = ( [^*] | \*+ [^/*] )*

Identifier = [:jletter:] [:jletterdigit:]*

DecIntegerLiteral = 0 | [1-9][0-9]*
FloatLiteral = DecIntegerLiteral [.] [0-9]+

%state STRING

%%

/* keywords */
<YYINITIAL> {
  "break"              { return symbol(yytext()); }
  "="                            { return symbol(yytext()); }
  "=="                           { return symbol(yytext()); }
  "!="                           { return symbol(yytext()); }
  "<"                            { return symbol(yytext()); }
  "<="                           { return symbol(yytext()); }
  ">"                            { return symbol(yytext()); }
  ">="                           { return symbol(yytext()); }
  "("                            { return symbol("%("); }
  ")"                       { return symbol("%)"); }
  "["                       { return symbol(yytext()); }
  "]"                       { return symbol(yytext()); }
  "{"                       { return symbol(yytext()); }
  "}"                       { return symbol(yytext()); }
  "."                       { return symbol(yytext()); }
  ".."                       { return symbol(yytext()); }
  ","                       { return symbol(yytext()); }
  "+"                       { return symbol("add"); }
  "-"                       { return symbol(yytext()); }
  "**"                       { return symbol("pow"); }
  "*"                       { return symbol("mul"); }
  "/"                       { return symbol(yytext()); }
  "|"                       { return symbol(yytext()); }
  "&"                       { return symbol(yytext()); }
  "^"                       { return symbol(yytext()); }
  "?"                       { return symbol(yytext()); }
  ":"                       { return symbol("%:"); }
  "::"                       { return symbol("cons"); }
  "+="                       { return symbol(yytext()); }
  "-="                       { return symbol(yytext()); }
  ";"                        { return symbol(yytext()); }
  "and"                             { return symbol(yytext()); }
  "or"                             { return symbol(yytext()); }
  "not"                             { return symbol(yytext()); }
  "function"                     { return symbol(yytext()); }
  "var"                             { return symbol(yytext()); }
  "true"                         { return symbol(yytext()); }
  "false"                         { return symbol(yytext()); }
  "if"                             { return symbol(yytext()); }
  "for"                             { return symbol(yytext()); }
  "elseif"                         { return symbol(yytext()); }
  "else"                         { return symbol(yytext()); }
  "while"                         { return symbol(yytext()); }
  "return"                         { return symbol(yytext()); }
  "in"                             { return symbol(yytext()); }
  "nil"                             { return symbol(yytext()); }
  "match"                         { return symbol(yytext()); }
  "with"                         { return symbol(yytext()); }
  "new"                          { return symbol(yytext()); }
}

<YYINITIAL> {
  /* identifiers */
  {Identifier}                   { return symbol("id"); }

  /* literals */
  {DecIntegerLiteral}            { return symbol("num"); }
  {FloatLiteral}            { return symbol("num"); }
  \"                             { string.setLength(0); yybegin(STRING); }

  /* comments */
  {Comment}                      { /* ignore */ }

  /* whitespace */
  {WhiteSpace}                   { /* ignore */ }
}

<STRING> {
  \"                             { yybegin(YYINITIAL);
                                   return symbol("string", string.toString()); }
  [^\n\r\"\\]+                   { string.append( yytext() ); }
  \\t                            { string.append('\t'); }
  \\n                            { string.append('\n'); }

  \\r                            { string.append('\r'); }
  \\\"                           { string.append('\"'); }
  \\                             { string.append('\\'); }
}

/* error fallback */
[^]                              { throw new Error("Illegal character <"+yytext()+">"); }