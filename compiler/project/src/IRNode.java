public class IRNode {
	public String opcode, op1, op2, result;

	public IRNode(String opcode, String op1, String op2, String result){
		this.opcode = opcode;
		this.op1 = op1;
		this.op2 = op2;
		this.result = result;
	}

	public void output(){
		String op1o = "";
		String op2o = "";
		String resulto = "";
		if(op1 != null){
			op1o = " " + op1;
		}
		if(op2 != null){
			op2o = " " + op2;
		}
		if(result != null){
			resulto = " " + result;
		}
		System.out.println(";" + opcode + op1o + op2o + resulto);
	}
}