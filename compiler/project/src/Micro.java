import org.antlr.v4.runtime.*;
import java.io.*;

public class Micro 
{
    public static void main( String[] args) throws Exception
    {
    	MicroLexer l = new MicroLexer(CharStreams.fromFileName(args[0]));
        CommonTokenStream tokens = new CommonTokenStream(l);
        MicroParser parser = new MicroParser(tokens);
        ANTLRErrorStrategy error = new CustomErrorStrategy();
        parser.setErrorHandler(error);
    	try{
            parser.program();
        }catch(Exception e){
        }
   	}
}

class CustomErrorStrategy extends DefaultErrorStrategy
{
    public void reportError (Parser recognizer, RecognitionException e)
    {
        throw e;
    }
}

