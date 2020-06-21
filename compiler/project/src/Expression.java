import org.antlr.v4.runtime.*;
import java.io.*;
import java.util.Stack;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.Matcher;

public class Expression{
	private static ArrayList<IRNode> rlist = new ArrayList<IRNode>();
	private static ArrayList<TinyNode> tlist = new ArrayList<TinyNode>();
	private static Stack<ASTNode> ASTtree = new Stack<ASTNode>();
	private static Stack<ASTNode> incr_expr = new Stack<ASTNode>();
	private static Stack<String> incr_id = new Stack<String>();
	private static Stack<String> regstack = new Stack<String>();
	private static Stack<String> typestack = new Stack<String>();
	private static Stack<String> labelstack_if = new Stack<String>();
	private static Stack<String> labelstack_for = new Stack<String>();
	private static Hashtable<String, String> typemap = new Hashtable<String, String>();
	private static int register = 1;
	private static int label = 1;


	private static String update(){
		String current = "$T" + Integer.toString(register);
		register++;
		return current;
	}

	private static String updatelabel(){
		String current = "label" + Integer.toString(label);
		label++;
		return current;
	}

	public static void IRlabel(){
		rlist.add(new IRNode("LABEL", labelstack_if.pop(), null, null));
	}

	public static void IRlabel_for(){
		rlist.add(new IRNode("LABEL", labelstack_for.pop(), null, null));
	}

	public static void IRlabel_for_p(){
		String l = labelstack_for.pop();
		rlist.add(new IRNode("LABEL", "label" + Integer.toString(Integer.parseInt(l.substring(5)) + 1), null, null));
	}

	public static void IRlabel_for_m(){
		String l = labelstack_for.pop();
		rlist.add(new IRNode("LABEL", "label" + Integer.toString(Integer.parseInt(l.substring(5)) - 1), null, null));
	}

	public static void getVariable(String type, String idlist){
		String[] list = idlist.split(",");
		for(int i = 0; i < list.length; i++){
			typemap.put(list[i], type);
			tlist.add(new TinyNode("var", list[i], null));
		}
	}

	public static void getString(String id, String str){
		typemap.put(id, "STRING");
		tlist.add(new TinyNode("str", id, str));
	}

	public static void connectL(){
			ASTNode node1 = ASTtree.pop();
			ASTNode node2 = ASTtree.pop();
			node1.left = node2;
			ASTtree.push(node1);
	}

	public static void connectR(){
			ASTNode node1 = ASTtree.pop();
			ASTNode node2 = ASTtree.pop();
			node2.right = node1;
			ASTtree.push(node2);
	}

	public static void connectLR(){
			ASTNode node1 = ASTtree.pop();
			ASTNode node2 = ASTtree.pop();
			ASTNode node3 = ASTtree.pop();
			node3.right = node2;
			node1.left = node3;
			ASTtree.push(node1);
	}
			
	public static void createNode(String value){
		ASTNode node = new ASTNode(value, null, null);
		ASTtree.push(node);
	}

	public static void output(){
		System.out.println(";IR code");
		System.out.println(";LABEL main");
		System.out.println(";LINK");
		for(int i = 0; i < rlist.size(); i++){
			rlist.get(i).output();
		}
		System.out.println(";RET");
		System.out.println(";tiny code");
		formTiny();
		for(int i = 0; i < tlist.size(); i++){
			tlist.get(i).output();
		}
	}

	public static void stmtdone(String value){
		if(typemap.get(value).contains("INT")){
			typestack.push("INT");
		}else if(typemap.get(value).contains("FLOAT")){
			typestack.push("FLOAT");
		}
		String type = typestack.pop();
		ASTNode temp = ASTtree.pop();
		formIR(temp, type);
		if(type == "INT"){
			if(temp.value.matches("[a-zA-Z][a-zA-Z0-9]*")){
				rlist.add(new IRNode("STOREI", temp.value, null, value));
			}else{
				rlist.add(new IRNode("STOREI", regstack.pop(), null, value));
			}
		}else if(type == "FLOAT"){
			if(temp.value.matches("[a-zA-Z][a-zA-Z0-9]*")){
				rlist.add(new IRNode("STOREF", temp.value, null, value));
			}else{
				rlist.add(new IRNode("STOREF", regstack.pop(), null, value));
			}
		}
	}

	public static void write(String idlist){
		String[] list = idlist.split(",");
		for(int i = 0; i < list.length; i++){
			if(typemap.get(list[i]) != null){
				if(typemap.get(list[i]).contains("INT")){
					rlist.add(new IRNode("WRITEI", null, null, list[i]));
				}else if(typemap.get(list[i]).contains("FLOAT")){
					rlist.add(new IRNode("WRITEF", null, null, list[i]));
				}else if(typemap.get(list[i]).contains("STRING")){
					rlist.add(new IRNode("WRITES", null, null, list[i]));
				}
			}
		}
	}

	public static void read(String idlist){
		String[] list = idlist.split(",");
		for(int i = 0; i < list.length; i++){
			if(typemap.get(list[i]) != null){
				if(typemap.get(list[i]).contains("INT")){
					rlist.add(new IRNode("READI", null, null, list[i]));
				}else if(typemap.get(list[i]).contains("FLOAT")){
					rlist.add(new IRNode("READF", null, null, list[i]));
				}else if(typemap.get(list[i]).contains("STRING")){
					rlist.add(new IRNode("READS", null, null, list[i]));
				}
			}
		}
	}

	public static void ifstmt(String compop){
		ASTNode temp1 = ASTtree.pop();
		ASTNode temp2 = ASTtree.pop();
		if(typemap.get(temp2.value).contains("INT")){
			typestack.push("INT");
		}else if(typemap.get(temp2.value).contains("FLOAT")){
			typestack.push("FLOAT");
		}
		String type = typestack.pop();
		String l1 = updatelabel();
		String l2 = updatelabel();
		labelstack_if.push(l2);
		labelstack_if.push(l1);
		formIR(temp1, type);
		switch(compop){
			case ">":
					if(temp1.value.matches("[a-zA-Z][a-zA-Z0-9]*")){
						rlist.add(new IRNode("LE", temp2.value, temp1.value, l1));
					}else{
						rlist.add(new IRNode("LE", temp2.value, regstack.pop(), l1));
					}
					break;
			case "<":
					if(temp1.value.matches("[a-zA-Z][a-zA-Z0-9]*")){
						rlist.add(new IRNode("GE", temp2.value, temp1.value, l1));
					}else{
						rlist.add(new IRNode("GE", temp2.value, regstack.pop(), l1));
					}
					break;
			case ">=":
					if(temp1.value.matches("[a-zA-Z][a-zA-Z0-9]*")){
						rlist.add(new IRNode("LT", temp2.value, temp1.value, l1));
					}else{
						rlist.add(new IRNode("LT", temp2.value, regstack.pop(), l1));
					}
					break;
			case "<=":
					if(temp1.value.matches("[a-zA-Z][a-zA-Z0-9]*")){
						rlist.add(new IRNode("GT", temp2.value, temp1.value, l1));
					}else{
						rlist.add(new IRNode("GT", temp2.value, regstack.pop(), l1));
					}
					break;
			case "=":
					if(temp1.value.matches("[a-zA-Z][a-zA-Z0-9]*")){
						rlist.add(new IRNode("NE", temp2.value, temp1.value, l1));
					}else{
						rlist.add(new IRNode("NE", temp2.value, regstack.pop(), l1));
					}
					break;
			case "!=":
					if(temp1.value.matches("[a-zA-Z][a-zA-Z0-9]*")){
						rlist.add(new IRNode("EQ", temp2.value, temp1.value, l1));
					}else{
						rlist.add(new IRNode("EQ", temp2.value, regstack.pop(), l1));
					}
					break;
		}
	}

	public static void ifjump(){
		String l = labelstack_if.pop();
		labelstack_if.push(l);
		rlist.add(new IRNode("JUMP", null, null, "label" + Integer.toString(Integer.parseInt(l.substring(5)) + 1)));
	}

	public static void forjump(){
		String l = labelstack_for.pop();
		labelstack_for.push(l);
		rlist.add(new IRNode("JUMP", null, null, "label" + Integer.toString(Integer.parseInt(l.substring(5)) - 1)));
	}

	public static void elsejump(){
		String l = labelstack_if.pop();
		labelstack_if.push(l);
		rlist.add(new IRNode("JUMP", null, null, l));
	}

	public static void forlabel(){
		labelstack_for.push(updatelabel());
	}

	public static void forstmt(String compop){
		ASTNode temp1 = ASTtree.pop();
		ASTNode temp2 = ASTtree.pop();
		if(typemap.get(temp2.value).contains("INT")){
			typestack.push("INT");
		}else if(typemap.get(temp2.value).contains("FLOAT")){
			typestack.push("FLOAT");
		}
		String type = typestack.pop();
		String l1 = updatelabel();
		labelstack_for.push(l1);
		formIR(temp1, type);
		switch(compop){
			case ">":
					if(temp1.value.matches("[a-zA-Z][a-zA-Z0-9]*")){
						rlist.add(new IRNode("LE", temp2.value, temp1.value, l1));
					}else{
						rlist.add(new IRNode("LE", temp2.value, regstack.pop(), l1));
					}
					break;
			case "<":
					if(temp1.value.matches("[a-zA-Z][a-zA-Z0-9]*")){
						rlist.add(new IRNode("GE", temp2.value, temp1.value, l1));
					}else{
						rlist.add(new IRNode("GE", temp2.value, regstack.pop(), l1));
					}
					break;
			case ">=":
					if(temp1.value.matches("[a-zA-Z][a-zA-Z0-9]*")){
						rlist.add(new IRNode("LT", temp2.value, temp1.value, l1));
					}else{
						rlist.add(new IRNode("LT", temp2.value, regstack.pop(), l1));
					}
					break;
			case "<=":
					if(temp1.value.matches("[a-zA-Z][a-zA-Z0-9]*")){
						rlist.add(new IRNode("GT", temp2.value, temp1.value, l1));
					}else{
						rlist.add(new IRNode("GT", temp2.value, regstack.pop(), l1));
					}
					break;
			case "=":
					if(temp1.value.matches("[a-zA-Z][a-zA-Z0-9]*")){
						rlist.add(new IRNode("NE", temp2.value, temp1.value, l1));
					}else{
						rlist.add(new IRNode("NE", temp2.value, regstack.pop(), l1));
					}
					break;
			case "!=":
					if(temp1.value.matches("[a-zA-Z][a-zA-Z0-9]*")){
						rlist.add(new IRNode("EQ", temp2.value, temp1.value, l1));
					}else{
						rlist.add(new IRNode("EQ", temp2.value, regstack.pop(), l1));
					}
					break;
		}
	}

	public static void incr_stmt(String value){
		incr_id.push(value);
		incr_expr.push(ASTtree.pop());
	}

	public static void form_incr(){
		if(!incr_id.isEmpty() && !incr_expr.isEmpty()){
			String value = incr_id.pop();
			ASTNode temp = incr_expr.pop();
			if(typemap.get(value).contains("INT")){
				typestack.push("INT");
			}else if(typemap.get(value).contains("FLOAT")){
				typestack.push("FLOAT");
			}
			String type = typestack.pop();
			formIR(temp, type);
			if(type == "INT"){
				if(temp.value.matches("[a-zA-Z][a-zA-Z0-9]*")){
					rlist.add(new IRNode("STOREI", temp.value, null, value));
				}else{
					rlist.add(new IRNode("STOREI", regstack.pop(), null, value));
				}
			}else if(type == "FLOAT"){
				if(temp.value.matches("[a-zA-Z][a-zA-Z0-9]*")){
					rlist.add(new IRNode("STOREF", temp.value, null, value));
				}else{
					rlist.add(new IRNode("STOREF", regstack.pop(), null, value));
				}
			}
		}
	}

	public static void formTiny(){
		for(int i = 0; i < rlist.size(); i++){
			IRNode ir = rlist.get(i);
			String opcode = tinyreg(ir.opcode);
			String op1 = tinyreg(ir.op1);
			String op2 = tinyreg(ir.op2);
			String rt = tinyreg(ir.result);
			switch(opcode){
				case "ADDI":
							tlist.add(new TinyNode("move", op1, rt));
							tlist.add(new TinyNode("addi", op2, rt));
							break;
				case "ADDF":
							tlist.add(new TinyNode("move", op1, rt));
							tlist.add(new TinyNode("addr", op2, rt));
							break;
				case "SUBI":
							tlist.add(new TinyNode("move", op1, rt));
							tlist.add(new TinyNode("subi", op2, rt));
							break;
				case "SUBF":
							tlist.add(new TinyNode("move", op1, rt));
							tlist.add(new TinyNode("subr", op2, rt));
							break;
				case "MULTI":
							tlist.add(new TinyNode("move", op1, rt));
							tlist.add(new TinyNode("muli", op2, rt));
							break;
				case "MULTF":
							tlist.add(new TinyNode("move", op1, rt));
							tlist.add(new TinyNode("mulr", op2, rt));
							break;
				case "DIVI":
							tlist.add(new TinyNode("move", op1, rt));
							tlist.add(new TinyNode("divi", op2, rt));
							break;
				case "DIVF":
							tlist.add(new TinyNode("move", op1, rt));
							tlist.add(new TinyNode("divr", op2, rt));
							break;
				case "STOREI":
							if(!checkvariable(ir.op1, ir.result)){
								tlist.add(new TinyNode("move", op1, "r" + Integer.toString(register)));
								tlist.add(new TinyNode("move", "r" + Integer.toString(register), rt));
								register++;
							}else{
								tlist.add(new TinyNode("move", op1, rt));
							}
							break;
				case "STOREF":
							if(!checkvariable(ir.op1, ir.result)){
								tlist.add(new TinyNode("move", op1, "r" + Integer.toString(register)));
								tlist.add(new TinyNode("move", "r" + Integer.toString(register), rt));
								register++;
							}else{
								tlist.add(new TinyNode("move", op1, rt));
							}
							break;
				case "READI":
							tlist.add(new TinyNode("sys readi", rt, null));
							break;
				case "READF":
							tlist.add(new TinyNode("sys readr", rt, null));
							break;
				case "WRITEI":
							tlist.add(new TinyNode("sys writei", rt, null));
							break;
				case "WRITEF":
							tlist.add(new TinyNode("sys writer", rt, null));
							break;
				case "WRITES":
							tlist.add(new TinyNode("sys writes", rt, null));
							break;
				case "LABEL":
							tlist.add(new TinyNode("label", op1, null));
							break;
				case "EQ":
							condition_tiny(ir.op1, ir.op2, op1, op2);
							tlist.add(new TinyNode("jeq", rt, null));
							break;
				case "NE":
							condition_tiny(ir.op1, ir.op2, op1, op2);
							tlist.add(new TinyNode("jne", rt, null));
							break;
				case "GT":
							condition_tiny(ir.op1, ir.op2, op1, op2);
							tlist.add(new TinyNode("jgt", rt, null));
							break;
				case "GE":
							condition_tiny(ir.op1, ir.op2, op1, op2);
							tlist.add(new TinyNode("jge", rt, null));
							break;
				case "LT":
							condition_tiny(ir.op1, ir.op2, op1, op2);
							tlist.add(new TinyNode("jlt", rt, null));
							break;
				case "LE":
							condition_tiny(ir.op1, ir.op2, op1, op2);
							tlist.add(new TinyNode("jle", rt, null));
							break;
				case "JUMP":
							tlist.add(new TinyNode("jmp", rt, null));
							break;
			}
		}
		tlist.add(new TinyNode("sys halt", null, null));
	}

	private static String tinyreg(String irreg){
		if(irreg == null){
			return irreg;
		}
		if(irreg.matches("\\$T([1-9]+[0-9]*)")){
			String reg = Integer.toString(Integer.parseInt(irreg.substring(2)) - 1);
			return "r" + reg;
		}
		return irreg;
	}

	private static boolean checkvariable(String op1, String rt){
		return (op1.charAt(0) == '$' || rt.charAt(0) == '$');
	}

	private static void condition_tiny(String irop1, String irop2, String op1, String op2){
		String reg2 = op2;
		if(!checkvariable(irop1, irop2)){
			reg2 = "r" + Integer.toString(register);
			tlist.add(new TinyNode("move", op2, reg2));
			register++;
		}
		if(typemap.get(irop1).contains("FLOAT")){
			tlist.add(new TinyNode("cmpr", op1, reg2));
		}else{
			tlist.add(new TinyNode("cmpi", op1, reg2));	
		}
	}

	public static void formIR(ASTNode node, String type){
		if(node != null){
			formIR(node.left, type);
			formIR(node.right, type);
			if(type == "INT"){
				if(node.value.matches("[0-9]+")){
					String reg = update();
					rlist.add(new IRNode("STOREI", node.value, null, reg));
					regstack.push(reg);
				}else if(node.value.contains("+")){
					if(typemap.get(node.left.value) != null && typemap.get(node.right.value) != null){
						String reg = update();
						rlist.add(new IRNode("ADDI", node.left.value, node.right.value, reg));
						regstack.push(reg);
					}else if(typemap.get(node.left.value) == null && typemap.get(node.right.value) != null){
						String reg = update();
						rlist.add(new IRNode("ADDI", regstack.pop(), node.right.value, reg));
						regstack.push(reg);
					}else if(typemap.get(node.left.value) != null && typemap.get(node.right.value) == null){
						String reg = update();
						rlist.add(new IRNode("ADDI", node.left.value, regstack.pop(), reg));
						regstack.push(reg);
					}else if(typemap.get(node.left.value) == null && typemap.get(node.right.value) == null){
						String reg = update();
						String temp1 = regstack.pop();
						String temp2 = regstack.pop();
						rlist.add(new IRNode("ADDI", temp2, temp1, reg));
						regstack.push(reg);
					}
				}else if(node.value.contains("-")){
					if(typemap.get(node.left.value) != null && typemap.get(node.right.value) != null){
						String reg = update();
						rlist.add(new IRNode("SUBI", node.left.value, node.right.value, reg));
						regstack.push(reg);
					}else if(typemap.get(node.left.value) == null && typemap.get(node.right.value) != null){
						String reg = update();
						rlist.add(new IRNode("SUBI", regstack.pop(), node.right.value, reg));
						regstack.push(reg);
					}else if(typemap.get(node.left.value) != null && typemap.get(node.right.value) == null){
						String reg = update();
						rlist.add(new IRNode("SUBI", node.left.value, regstack.pop(), reg));
						regstack.push(reg);
					}else if(typemap.get(node.left.value) == null && typemap.get(node.right.value) == null){
						String reg = update();
						String temp1 = regstack.pop();
						String temp2 = regstack.pop();
						rlist.add(new IRNode("SUBI", temp2, temp1, reg));
						regstack.push(reg);
					}
				}else if(node.value.contains("*")){
					if(typemap.get(node.left.value) != null && typemap.get(node.right.value) != null){
						String reg = update();
						rlist.add(new IRNode("MULTI", node.left.value, node.right.value, reg));
						regstack.push(reg);
					}else if(typemap.get(node.left.value) == null && typemap.get(node.right.value) != null){
						String reg = update();
						rlist.add(new IRNode("MULTI", regstack.pop(), node.right.value, reg));
						regstack.push(reg);
					}else if(typemap.get(node.left.value) != null && typemap.get(node.right.value) == null){
						String reg = update();
						rlist.add(new IRNode("MULTI", node.left.value, regstack.pop(), reg));
						regstack.push(reg);
					}else if(typemap.get(node.left.value) == null && typemap.get(node.right.value) == null){
						String reg = update();
						String temp1 = regstack.pop();
						String temp2 = regstack.pop();
						rlist.add(new IRNode("MULTI", temp2, temp1, reg));
						regstack.push(reg);
					}
				}else if(node.value.contains("/")){
					if(typemap.get(node.left.value) != null && typemap.get(node.right.value) != null){
						String reg = update();
						rlist.add(new IRNode("DIVI", node.left.value, node.right.value, reg));
						regstack.push(reg);
					}else if(typemap.get(node.left.value) == null && typemap.get(node.right.value) != null){
						String reg = update();
						rlist.add(new IRNode("DIVI", regstack.pop(), node.right.value, reg));
						regstack.push(reg);
					}else if(typemap.get(node.left.value) != null && typemap.get(node.right.value) == null){
						String reg = update();
						rlist.add(new IRNode("DIVI", node.left.value, regstack.pop(), reg));
						regstack.push(reg);
					}else if(typemap.get(node.left.value) == null && typemap.get(node.right.value) == null){
						String reg = update();
						String temp1 = regstack.pop();
						String temp2 = regstack.pop();
						rlist.add(new IRNode("DIVI", temp2, temp1, reg));
						regstack.push(reg);
					}
				}

			}else if(type == "FLOAT"){
				if(node.value.matches("[0-9]*\\.[0-9]+")){
					String reg = update();
					rlist.add(new IRNode("STOREF", node.value, null, reg));
					regstack.push(reg);
				}else if(node.value.contains("+")){
					if(typemap.get(node.left.value) != null && typemap.get(node.right.value) != null){
						String reg = update();
						rlist.add(new IRNode("ADDF", node.left.value, node.right.value, reg));
						regstack.push(reg);
					}else if(typemap.get(node.left.value) == null && typemap.get(node.right.value) != null){
						String reg = update();
						rlist.add(new IRNode("ADDF", regstack.pop(), node.right.value, reg));
						regstack.push(reg);
					}else if(typemap.get(node.left.value) != null && typemap.get(node.right.value) == null){
						String reg = update();
						rlist.add(new IRNode("ADDF", node.left.value, regstack.pop(), reg));
						regstack.push(reg);
					}else if(typemap.get(node.left.value) == null && typemap.get(node.right.value) == null){
						String reg = update();
						String temp1 = regstack.pop();
						String temp2 = regstack.pop();
						rlist.add(new IRNode("ADDF", temp2, temp1, reg));
						regstack.push(reg);
					}
				}else if(node.value.contains("-")){
					if(typemap.get(node.left.value) != null && typemap.get(node.right.value) != null){
						String reg = update();
						rlist.add(new IRNode("SUBF", node.left.value, node.right.value, reg));
						regstack.push(reg);
					}else if(typemap.get(node.left.value) == null && typemap.get(node.right.value) != null){
						String reg = update();
						rlist.add(new IRNode("SUBF", regstack.pop(), node.right.value, reg));
						regstack.push(reg);
					}else if(typemap.get(node.left.value) != null && typemap.get(node.right.value) == null){
						String reg = update();
						rlist.add(new IRNode("SUBF", node.left.value, regstack.pop(), reg));
						regstack.push(reg);
					}else if(typemap.get(node.left.value) == null && typemap.get(node.right.value) == null){
						String reg = update();
						String temp1 = regstack.pop();
						String temp2 = regstack.pop();
						rlist.add(new IRNode("SUBF", temp2, temp1, reg));
						regstack.push(reg);
					}
				}else if(node.value.contains("*")){
					if(typemap.get(node.left.value) != null && typemap.get(node.right.value) != null){
						String reg = update();
						rlist.add(new IRNode("MULTF", node.left.value, node.right.value, reg));
						regstack.push(reg);
					}else if(typemap.get(node.left.value) == null && typemap.get(node.right.value) != null){
						String reg = update();
						rlist.add(new IRNode("MULTF", regstack.pop(), node.right.value, reg));
						regstack.push(reg);
					}else if(typemap.get(node.left.value) != null && typemap.get(node.right.value) == null){
						String reg = update();
						rlist.add(new IRNode("MULTF", node.left.value, regstack.pop(), reg));
						regstack.push(reg);
					}else if(typemap.get(node.left.value) == null && typemap.get(node.right.value) == null){
						String reg = update();
						String temp1 = regstack.pop();
						String temp2 = regstack.pop();
						rlist.add(new IRNode("MULTF", temp2, temp1, reg));
						regstack.push(reg);
					}
				}else if(node.value.contains("/")){
					if(typemap.get(node.left.value) != null && typemap.get(node.right.value) != null){
						String reg = update();
						rlist.add(new IRNode("DIVF", node.left.value, node.right.value, reg));
						regstack.push(reg);
					}else if(typemap.get(node.left.value) == null && typemap.get(node.right.value) != null){
						String reg = update();
						rlist.add(new IRNode("DIVF", regstack.pop(), node.right.value, reg));
						regstack.push(reg);
					}else if(typemap.get(node.left.value) != null && typemap.get(node.right.value) == null){
						String reg = update();
						rlist.add(new IRNode("DIVF", node.left.value, regstack.pop(), reg));
						regstack.push(reg);
					}else if(typemap.get(node.left.value) == null && typemap.get(node.right.value) == null){
						String reg = update();
						String temp1 = regstack.pop();
						String temp2 = regstack.pop();
						rlist.add(new IRNode("DIVF", temp2, temp1, reg));
						regstack.push(reg);
					}
				}

			}
		}
	}
}