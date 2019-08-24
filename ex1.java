import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


public class ex1{
	
	public static final String INPUT_FILE_NAME = "input.txt";
	public static final String OUTPUT_FILE_NAME = "output.txt";
	public static final String SPLIT_LINE_DELIMITERS = "[\\s,:=]+";
	
	public static void main(String[] args)  throws IOException {
		BayesianNetwork network = null;

		FileReader fileReader = new FileReader(INPUT_FILE_NAME);
		BufferedReader bufferReader = new BufferedReader(fileReader);

		File outputFile = new File(OUTPUT_FILE_NAME);
		outputFile.createNewFile();
		FileWriter fileWriter = new FileWriter(outputFile);
		BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
		
		String[] tokenLine;
		
		while(bufferReader.ready()){
			tokenLine = bufferReader.readLine().split(SPLIT_LINE_DELIMITERS);
			while(tokenLine.length < 1){
				tokenLine = bufferReader.readLine().split(SPLIT_LINE_DELIMITERS);
			}
			String word = tokenLine[0];
			switch (word) {
			case "Network":
				network = new BayesianNetwork();				
				break;
			case "Variables":
				addVariable2NetworkFromToken(network, tokenLine);
				break;
			case "Var":
				Variable var = network.getVar(tokenLine[1]);
				setValues2VarFromLine(network, var, bufferReader.readLine());
				setValues2VarFromBr(network, var, bufferReader);
				setCptFromBR(var, bufferReader);
				break;
			case "Queries":
				System.out.println(network);
				String line = bufferReader.readLine();
				while(line.length() > 1){
					if(isProbabilityQueries(line)){
						calcProbablility(network, line, bufferWriter);
					}
					else{
						calcIndependent(network, line, bufferWriter);
					}
					line = (bufferReader.ready()) ? bufferReader.readLine() : "";
				}
				break;					
			default:
				break;
			}
		}
		bufferReader.close();
		fileReader.close();
		bufferWriter.close();
		fileWriter.close();
	}

	private static void calcIndependent(BayesianNetwork network, String line, BufferedWriter bufferWriter)
			throws IOException {
		StringTokenizer queryString = new StringTokenizer(line, "|");
		String queriesVarString = queryString.nextToken();
		StringTokenizer queriesVarStringToken = new StringTokenizer(queriesVarString, "- ");
		Variable var1 = network.getVar(queriesVarStringToken.nextToken());
		Variable var2 = network.getVar(queriesVarStringToken.nextToken());
		List<Variable> givens = new ArrayList<Variable>();
		if(queryString.hasMoreTokens()){
			String queriesGivensString = queryString.nextToken();
			StringTokenizer queriesGivensStringToken = new StringTokenizer(queriesGivensString, ", ");
			while(queriesGivensStringToken.hasMoreTokens()){
				givens.add(network.getVar(queriesGivensStringToken.nextToken().split("=")[0]));
			}
		}
		String result = network.getIndependedQuery(var1, var2, givens).getResult();
		bufferWriter.newLine();
		bufferWriter.write(result);
	}

	private static void calcProbablility(BayesianNetwork network, String line, BufferedWriter bufferWriter)
			throws IOException {
		String[] queryString = line.split("[P()|]+");
		String queryVarString = queryString[1];
		VariableCondition queryVar = new VariableCondition(
				network.getVar(queryVarString.split("=")[0]), queryVarString.split("=")[1]);
		List<VariableCondition> evidence = new ArrayList<VariableCondition>();
		String evidence_strings = queryString[2];
		String[] evidence_strings_token = evidence_strings.split("[\\s,=]+");
		for (int i = 0; i < evidence_strings_token.length; i+=2) {
			evidence.add(new VariableCondition(
					network.getVar(evidence_strings_token[i]), evidence_strings_token[i + 1]));
		}
		List<Variable> hiddens = new ArrayList<Variable>();
		if(queryString.length >= 4){
			String hidens_string = queryString[3];
			StringTokenizer hidens_string_token = new StringTokenizer(hidens_string, " ,-");
			if(hidens_string_token.hasMoreTokens()){
				while(hidens_string_token.hasMoreElements()){
					hiddens.add(network.getVar(hidens_string_token.nextToken()));
				}
			}
		}
		String result = network.getProbabilityQuery(queryVar, evidence, hiddens).getResult();
		System.out.println(result);
		bufferWriter.newLine();
		bufferWriter.write(result);
	}

	private static boolean isProbabilityQueries(String line) {
		return line.substring(0, 2).equals("P(") && line.contains(")");
	}

	private static void setValues2VarFromBr(BayesianNetwork network, Variable var, BufferedReader bufferReader)
			throws IOException {
		String[] tokenLine = bufferReader.readLine().split(SPLIT_LINE_DELIMITERS);
		String firstParent = tokenLine[1];
		if(!firstParent.equals("none")){
			setParents2VarFromLine(network, var, tokenLine);
		}		
	}

	private static void setCptFromBR(Variable var, BufferedReader bufferReader) throws IOException {
		bufferReader.readLine();
		StringTokenizer tokenLine = new StringTokenizer(bufferReader.readLine(), " ,:=");
		while(tokenLine.countTokens() > 1){
			List<VariableCondition> variableState = new ArrayList<VariableCondition>();
			for(Variable parent : var.getParents()){
				variableState.add(new VariableCondition(parent, tokenLine.nextToken()));
			}
			while(tokenLine.hasMoreTokens()){
				CptVariableCondition cptVariableState = new CptVariableCondition(var, variableState, tokenLine.nextToken());
				var.addtoCPT(new CptConditionProbability(cptVariableState, Double.parseDouble(tokenLine.nextToken())));
			}
			tokenLine = new StringTokenizer(bufferReader.readLine(), " ,:=");
		}
	}

	private static void setParents2VarFromLine(BayesianNetwork network, Variable var, String[] tokenLine) {
		List<Variable> parents = new ArrayList<Variable>();
		for (int i = 1; i < tokenLine.length; i++) {
			parents.add(network.getVar(tokenLine[i]));
		}
		network.setParents(var.getName(), parents);
	}

	private static void setValues2VarFromLine(BayesianNetwork network, Variable var, String line) {
		StringTokenizer tokenLine = new StringTokenizer(line, " ,:=");
		tokenLine.nextToken();
		List<String> varValueString = new ArrayList<String>();
		while(tokenLine.hasMoreTokens()){
			varValueString.add(tokenLine.nextToken());
		}
		network.setValues(var.getName(), varValueString);
	}

	private static void addVariable2NetworkFromToken(BayesianNetwork network, String[] tokenLine) {
		for (int i = 1; i < tokenLine.length; i++) {
			network.addVariable(tokenLine[i]);
		}
	}
}
