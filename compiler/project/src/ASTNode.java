public class ASTNode {
	public String value;
	public ASTNode left, right;

	public ASTNode(String value, ASTNode left, ASTNode right){
		this.value = value;
		this.left = left;
		this.right = right;
	}
}