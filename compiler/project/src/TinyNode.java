public class TinyNode {
	public String opcode, op1, op2;

	public TinyNode(String opcode, String op1, String op2){
		this.opcode = opcode;
		this.op1 = op1;
		this.op2 = op2;
	}

	public void output(){
		String op1o = "";
		String op2o = "";
		if(op1 != null){
			op1o = " " + op1;
		}
		if(op2 != null){
			op2o = " " + op2;
		}

		System.out.println(opcode + op1o + op2o);
	}
}