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
	public static void main(String[] args)  throws IOException {
		BayesianNetwork network = null;

		FileReader fileReader = new FileReader("input.txt");
		BufferedReader bufferReader = new BufferedReader(fileReader);

		File outputFile = new File("output.txt");
		outputFile.createNewFile();
		FileWriter fileWriter = new FileWriter(outputFile);
		BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
		
		boolean firstLine = true;
		StringTokenizer tokenLine;
		
		while(bufferReader.ready()){
			tokenLine = new StringTokenizer(bufferReader.readLine(), " ,:=");
			while(tokenLine.countTokens()<1){
				tokenLine = new StringTokenizer(bufferReader.readLine(), " ,:=");
			}
			String firstWord = tokenLine.nextToken();
			if(firstWord.equals("Network")){
				network = new BayesianNetwork();
			}
			if(firstWord.equals("Variables")){
				while(tokenLine.hasMoreTokens()){
					network.addVariable(tokenLine.nextToken());
				}
			}
			if(firstWord.equals("Var")){
				Variable var = network.getVar(tokenLine.nextToken());
				tokenLine = new StringTokenizer(bufferReader.readLine(), " ,:=");
				tokenLine.nextToken();
				List<String> varValueString = new ArrayList<String>();
				while(tokenLine.hasMoreTokens()){
					varValueString.add(tokenLine.nextToken());
				}
				network.setValues(var.getName(), varValueString);
				tokenLine = new StringTokenizer(bufferReader.readLine(), " ,:=");
				tokenLine.nextToken();
				String first_parent = tokenLine.nextToken();
				if(!first_parent.equals("none")){
					List<Variable> parents = new ArrayList<Variable>();
					parents.add(network.getVar(first_parent));
					while(tokenLine.hasMoreTokens()){
						parents.add(network.getVar(tokenLine.nextToken()));
					}
					network.setParents(var.getName(), parents);
				}
				bufferReader.readLine();
				tokenLine = new StringTokenizer(bufferReader.readLine(), " ,:=");
				while(tokenLine.countTokens()>1){
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
			if(firstWord.equals("Queries")){
				System.out.println(network);
				String line = bufferReader.readLine();
				while(line.length() > 1){
					if(line.substring(0, 2).equals("P(") && line.contains(")")){
						StringTokenizer query_string = new StringTokenizer(line, "P()|");
						String query_var_string = query_string.nextToken();
						VariableCondition query_var = new VariableCondition(network.getVar(query_var_string.split("=")[0]), query_var_string.split("=")[1]);
						List<VariableCondition> evidence = new ArrayList<VariableCondition>();
						String evidence_strings = query_string.nextToken();
						StringTokenizer evidence_strings_token = new StringTokenizer(evidence_strings, ",= ");
						while(evidence_strings_token.hasMoreTokens()){
							evidence.add(new VariableCondition(network.getVar(evidence_strings_token.nextToken()), evidence_strings_token.nextToken()));
						}
						List<Variable> hiddens = new ArrayList<Variable>();
						if(query_string.hasMoreElements()){
							String hidens_string = query_string.nextToken();
							StringTokenizer hidens_string_token = new StringTokenizer(hidens_string, " ,-");
							if(hidens_string_token.hasMoreTokens()){
								while(hidens_string_token.hasMoreElements()){
									hiddens.add(network.getVar(hidens_string_token.nextToken()));
								}
							}
						}
						String result = network.P(query_var, evidence, hiddens);
						System.out.println(result);
						
						if(!firstLine) bufferWriter.newLine();
						bufferWriter.write(result);
						if(firstLine) firstLine = false;
					}
					else{
						StringTokenizer query_string = new StringTokenizer(line, "|");
						String queries_var_string = query_string.nextToken();
						StringTokenizer queries_var_string_token = new StringTokenizer(queries_var_string, "- ");
						Variable var1 = network.getVar(queries_var_string_token.nextToken());
						Variable var2 = network.getVar(queries_var_string_token.nextToken());
						List<Variable> givens = new ArrayList<Variable>();
						if(query_string.hasMoreTokens()){
							String queries_givens_string = query_string.nextToken();
							StringTokenizer queries_givens_string_token = new StringTokenizer(queries_givens_string, ", ");
							while(queries_givens_string_token.hasMoreTokens()){
								givens.add(network.getVar(queries_givens_string_token.nextToken().split("=")[0]));
							}
						}
						String result = (!network.BayesBall(var1, var2, null, givens, true)) ? "yes" : "no";
						
						if(!firstLine) bufferWriter.newLine();
						bufferWriter.write(result);
						if(firstLine) firstLine = false;
					}
					line = (bufferReader.ready()) ? bufferReader.readLine() : "";
				}
			}
		}
		bufferReader.close();
		fileReader.close();
		bufferWriter.close();
		fileWriter.close();
	}
}
