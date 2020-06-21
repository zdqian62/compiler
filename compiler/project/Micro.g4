grammar Micro;

none: {};

KEYWORDS: 'PROGRAM' | 'BEGIN' | 'END' | 'FUNCTION' | 'READ' | 'WRITE' | 'IF' | 'ELSE' | 'FI' | 'FOR' | 'ROF' | 'RETURN' | 'INT' | 'VOID' | 'STRING' | 'FLOAT';

INTLITERAL: [0-9]+;

FLOATLITERAL: [0-9]*'.'[0-9]+;

STRINGLITERAL: '"'(~'"')*'"';

COMMENT: '--'(~('\n'|'\r'))* -> skip ;

IDENTIFIER: [a-zA-Z][a-zA-Z0-9]*;

OPERATORS: ':=' | '+' | '-' | '*' | '/' | '=' | '!=' | '<' | '>' | '(' | ')' | ';' | ',' | '<=' | '>='; 

WHITESPACE: (' ' | '\n' | '\t' | '\r')+ -> skip;

//Program
program: 'PROGRAM' id 'BEGIN' pgm_body 'END' {Expression.output();};
id: IDENTIFIER;
pgm_body: {SymbolTableStack.global();} decl func_declarations ;
decl: string_decl decl | var_decl decl |  ;

//Global String Declaration
string_decl : 'STRING' id ':=' str ';' {SymbolTableStack.getSymbol($id.text, "STRING", $str.text); Expression.getString($id.text, $str.text);};
str: STRINGLITERAL ;

//Variable Declaration
var_decl: var_type id_list ';' {SymbolTableStack.getSymbol($id_list.text, $var_type.text, ""); Expression.getVariable($var_type.text, $id_list.text);}; 
var_type: 'FLOAT' | 'INT' ;
any_type: var_type | 'VOID' ;
id_list: id id_tail ;
id_tail: ',' id id_tail |  ;

//Function Paramater List
param_decl_list: param_decl param_decl_tail |  ;
param_decl: var_type id {SymbolTableStack.getSymbol($id.text, $var_type.text, "");};
param_decl_tail: ',' param_decl param_decl_tail |  ;

//Function Declarations
func_declarations: func_decl func_declarations |  ;
func_decl: 'FUNCTION' any_type id {SymbolTableStack.function($id.text);} '('param_decl_list')' 'BEGIN' func_body 'END' ;
func_body: decl stmt_list ;

//Statement List
stmt_list: stmt stmt_list |  ;
stmt: base_stmt | if_stmt | for_stmt ;
base_stmt: assign_stmt | read_stmt | write_stmt | return_stmt ;

//Basic Statements
assign_stmt: assign_expr {Expression.stmtdone($assign_expr.ctx.getChild(0).getText());}';' ;
assign_expr: id ':=' expr ;
read_stmt: 'READ' '(' id_list ')'';' {Expression.read($id_list.text);};
write_stmt: 'WRITE' '(' id_list ')'';' {Expression.write($id_list.text);};
return_stmt: 'RETURN' expr ';' ;

//Expressions
expr: ep=expr_prefix factor {
	if($ep.ctx.getChildCount() != 0){
		Expression.connectR();
	}
};
expr_prefix: ep=expr_prefix factor addop{Expression.createNode($addop.text);} {
	if($ep.ctx.getChildCount() == 0){
		Expression.connectL();
	}else{
		Expression.connectLR();
	}
}|  ;
factor: fp=factor_prefix postfix_expr{
	if($fp.ctx.getChildCount() != 0){
		Expression.connectR();
	}
};
factor_prefix: fp=factor_prefix postfix_expr mulop{Expression.createNode($mulop.text);} {
	if($fp.ctx.getChildCount() == 0){
		Expression.connectL();
	}
}|  ;
postfix_expr: primary | call_expr ;
call_expr: id '(' expr_list ')' ;
expr_list: expr expr_list_tail |  ;
expr_list_tail: ',' expr expr_list_tail |  ;
primary: '('expr')' | id{Expression.createNode($id.text);} | INTLITERAL{Expression.createNode($INTLITERAL.text);} | FLOATLITERAL{Expression.createNode($FLOATLITERAL.text);} ;
addop: '+' | '-' ;
mulop: '*' | '/' ;

//Comlex Statements and Condition
if_stmt: 'IF' '(' cond{Expression.ifstmt($cond.ctx.getChild(1).getText());} ')' {SymbolTableStack.block();} decl stmt_list {Expression.ifjump();}{Expression.IRlabel();}else_part 'FI'{Expression.IRlabel();} ;
else_part: 'ELSE' {SymbolTableStack.block();} decl stmt_list{Expression.elsejump();} | ;
cond: expr compop expr;
compop: '<' | '>' | '=' | '!=' | '<=' | '>=' ;

init_stmt: assign_expr {Expression.stmtdone($assign_expr.ctx.getChild(0).getText());}|  ;
incr_stmt: assign_expr {Expression.incr_stmt($assign_expr.ctx.getChild(0).getText());}|  ;

for_stmt: 'FOR' '(' init_stmt{Expression.forlabel(); Expression.IRlabel_for(); Expression.forlabel();} ';' cond{Expression.forstmt($cond.ctx.getChild(1).getText());} ';' incr_stmt ')' decl stmt_list {Expression.IRlabel_for_m(); Expression.form_incr(); Expression.forjump(); Expression.IRlabel_for_p(); }'ROF' ;


