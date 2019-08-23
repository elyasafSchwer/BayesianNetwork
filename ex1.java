import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import sun.net.NetworkClient;

public class ex1{
	
	public static final String INPUT_FILE_NAME = "input.txt";
	public static final String OUTPUT_FILE_NAME = "output.txt";
	
	public static void main(String[] args)  throws IOException {
		BayesianNetwork network = null;

		FileReader fileReader = new FileReader(INPUT_FILE_NAME);
		BufferedReader bufferReader = new BufferedReader(fileReader);

		File outputFile = new File(OUTPUT_FILE_NAME);
		outputFile.createNewFile();
		FileWriter fileWriter = new FileWriter(outputFile);
		BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
		
		boolean firstLine = true;
		StringTokenizer tokenLine;
		
		while(bufferReader.ready()){
			tokenLine = new StringTokenizer(bufferReader.readLine(), " ,:=");
			while(tokenLine.countTokens() < 1){
				tokenLine = new StringTokenizer(bufferReader.readLine(), " ,:=");
			}
			String firstWord = tokenLine.nextToken();
			switch (firstWord) {
			case "Network":
				network = new BayesianNetwork();				
				break;
			case "Variables":
				addVariable2NetworkFromToken(network, tokenLine);
				break;
			case "Var":
				Variable var = network.getVar(tokenLine.nextToken());
				setValues2VarFromLine(network, var, bufferReader.readLine());
				setValues2VarFromBr(network, var, bufferReader);
				setCptFromBR(var, bufferReader);
				break;
			case "Queries":
				System.out.println(network);
				String line = bufferReader.readLine();
				while(line.length() > 1){
					if(isProbabilityQueries(line)){
						calcProbablility(network, line, bufferWriter, firstLine);
					}
					else{
						calcIndependent(network, line, bufferWriter, firstLine);
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

	private static void calcIndependent(BayesianNetwork network, String line, BufferedWriter bufferWriter,
			boolean firstLine) throws IOException {
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
		if(!firstLine){
			bufferWriter.newLine();
		}
		bufferWriter.write(result);
		firstLine = false;
	}

	private static void calcProbablility(BayesianNetwork network, String line, BufferedWriter bufferWriter, 
			boolean firstLine) throws IOException {
		StringTokenizer queryString = new StringTokenizer(line, "P()|");
		String queryVarString = queryString.nextToken();
		VariableCondition queryVar = 
				new VariableCondition(network.getVar(queryVarString.split("=")[0]), queryVarString.split("=")[1]);
		List<VariableCondition> evidence = new ArrayList<VariableCondition>();
		String evidence_strings = queryString.nextToken();
		StringTokenizer evidence_strings_token = new StringTokenizer(evidence_strings, ",= ");
		while(evidence_strings_token.hasMoreTokens()){
			evidence.add(new VariableCondition(
					network.getVar(evidence_strings_token.nextToken()), evidence_strings_token.nextToken()));
		}
		List<Variable> hiddens = new ArrayList<Variable>();
		if(queryString.hasMoreElements()){
			String hidens_string = queryString.nextToken();
			StringTokenizer hidens_string_token = new StringTokenizer(hidens_string, " ,-");
			if(hidens_string_token.hasMoreTokens()){
				while(hidens_string_token.hasMoreElements()){
					hiddens.add(network.getVar(hidens_string_token.nextToken()));
				}
			}
		}
		String result = network.getProbabilityQuery(queryVar, evidence, hiddens).getResult();
		System.out.println(result);
		if(!firstLine){
			bufferWriter.newLine();
		}
		bufferWriter.write(result);
		firstLine = false;
	}

	private static boolean isProbabilityQueries(String line) {
		return line.substring(0, 2).equals("P(") && line.contains(")");
	}

	private static void setValues2VarFromBr(BayesianNetwork network, Variable var, BufferedReader bufferReader)
			throws IOException {
		StringTokenizer tokenLine = new StringTokenizer(bufferReader.readLine(), " ,:=");
		tokenLine.nextToken();
		String first_parent = tokenLine.nextToken();
		if(!first_parent.equals("none")){
			setParents2VarFromLine(network, var, first_parent, tokenLine);
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

	private static void setParents2VarFromLine(BayesianNetwork network, Variable var, String first_parent, StringTokenizer tokenLine) {
		List<Variable> parents = new ArrayList<Variable>();
		parents.add(network.getVar(first_parent));
		while(tokenLine.hasMoreTokens()){
			parents.add(network.getVar(tokenLine.nextToken()));
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

	private static void addVariable2NetworkFromToken(BayesianNetwork network, StringTokenizer tokenLine) {
		while(tokenLine.hasMoreTokens()){
			network.addVariable(tokenLine.nextToken());
		}
	}
}
