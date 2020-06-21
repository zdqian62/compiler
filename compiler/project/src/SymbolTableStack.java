import org.antlr.v4.runtime.*;
import java.io.*;
import java.util.Stack;
import java.util.ArrayList;

class Symbol{
	public String name, type, value;

	public Symbol(String name, String type, String value){
		this.name = name;
		this.type = type;
		this.value = value;
	}
}

class SymbolTable{
	public String name;
	public ArrayList<Symbol> list = new ArrayList<Symbol>();

	public SymbolTable(String name){
		this.name = name;
	}
}

public class SymbolTableStack{
	private static Stack<SymbolTable> sts = new Stack<SymbolTable>();
	private static int idx = 1;

	public static void global(){
		sts.push(new SymbolTable("GLOBAL"));
	}

	public static void function(String func_name){
		sts.push(new SymbolTable(func_name));
	}

	public static void block(){
		String bl_name = "BLOCK" + idx;
		sts.push(new SymbolTable(bl_name));
		idx += 1;
	}

	public static void getSymbol(String symbolname, String symboltype, String symbolvalue){
		String[] slist = symbolname.split(",");
		SymbolTable temp = sts.pop();
		for(int i = 0; i < slist.length; i++){
			Symbol newsymbol = new Symbol(slist[i], symboltype, symbolvalue);
			for(int j = 0; j < temp.list.size(); j++){
				if(newsymbol.name.equals(temp.list.get(j).name)){
					System.out.println("DECLARATION ERROR " + newsymbol.name);
					System.exit(0);
				}
			}
			temp.list.add(newsymbol);
		}
		sts.push(temp);
	}

	public static void outputresult(){
		Stack<SymbolTable> temp = new Stack<SymbolTable>();
		while(!sts.empty()){
			temp.push(sts.pop());
		}
		while(!temp.empty()){
			SymbolTable current = temp.peek();
			System.out.println("Symbol table " + current.name);
			for(int i = 0; i < current.list.size(); i++){
				Symbol cr_symbol = current.list.get(i);
				if(cr_symbol.type == "STRING"){
					System.out.println("name " + cr_symbol.name + " type " + cr_symbol.type + " value " + cr_symbol.value);
				}
				else{
					System.out.println("name " + cr_symbol.name + " type " + cr_symbol.type);
				}
			}
			temp.pop();
			if(!temp.empty()){
				System.out.println();
			}
		}
	}
}