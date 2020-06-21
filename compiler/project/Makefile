team:
	@echo "Team: mrnaiq"
	@echo " "
	@echo "Zhengdong Qian"
	@echo "qian62"
	@echo " "
	@echo "Zhichao Wang"
	@echo "bbb549761883"

compiler:
	javac src/SymbolTableStack.java
	java org.antlr.v4.Tool -o build/ Micro.g4
	mkdir classes
	javac -source 1.7 -target 1.7 -d classes src/*.java build/*.java	

clean:
	rm -rf build
	rm -rf classes
	rm -rf output.txt
