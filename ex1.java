import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.StringTokenizer;
import java.util.Vector;

public class ex1{
	public static void main(String[] args)  throws IOException {
		BayesianNetwork network = null;

		FileReader file_reader = new FileReader("input.txt");
		BufferedReader buffer_reader = new BufferedReader(file_reader);

		File output_file = new File("output.txt");
		output_file.createNewFile();
		FileWriter file_writer = new FileWriter(output_file);
		BufferedWriter buffer_writer = new BufferedWriter(file_writer);
		
		boolean first_line = true;
		StringTokenizer token_line;
		
		while(buffer_reader.ready()){
			token_line = new StringTokenizer(buffer_reader.readLine(), " ,:=");
			while(token_line.countTokens()<1){
				token_line = new StringTokenizer(buffer_reader.readLine(), " ,:=");
			}
			String first_word = token_line.nextToken();
			if(first_word.equals("Network")){
				network = new BayesianNetwork();
			}
			if(first_word.equals("Variables")){
				while(token_line.hasMoreTokens()){
					network.addVariable(token_line.nextToken());
				}
			}
			if(first_word.equals("Var")){
				Variable var = network.getVar(token_line.nextToken());
				token_line = new StringTokenizer(buffer_reader.readLine(), " ,:=");
				token_line.nextToken();
				Vector<String> var_value_string = new Vector<String>();
				while(token_line.hasMoreTokens()){
					var_value_string.add(token_line.nextToken());
				}
				network.setValues(var.getName(), var_value_string);
				token_line = new StringTokenizer(buffer_reader.readLine(), " ,:=");
				token_line.nextToken();
				String first_parent = token_line.nextToken();
				if(!first_parent.equals("none")){
					Vector<Variable> parents = new Vector<Variable>();
					parents.add(network.getVar(first_parent));
					while(token_line.hasMoreTokens()){
						parents.add(network.getVar(token_line.nextToken()));
					}
					network.setParents(var.getName(), parents);
				}
				buffer_reader.readLine();
				token_line = new StringTokenizer(buffer_reader.readLine(), " ,:=");
				while(token_line.countTokens()>1){
					Vector<VariableState> variable_state = new Vector<VariableState>();
					for(Variable parent : var.getParents()){
						variable_state.add(new VariableState(parent, token_line.nextToken()));
					}
					while(token_line.hasMoreTokens()){
						CptVariableState cpt_variable_state = new CptVariableState(var, variable_state, token_line.nextToken());
						var.addtoCPT(new CptStateProbability(cpt_variable_state, Double.parseDouble(token_line.nextToken())));
					}
					token_line = new StringTokenizer(buffer_reader.readLine(), " ,:=");
				}
			}
			if(first_word.equals("Queries")){
				System.out.println(network);
				String line = buffer_reader.readLine();
				while(line.length() > 1){
					if(line.substring(0, 2).equals("P(") && line.contains(")")){
						StringTokenizer query_string = new StringTokenizer(line, "P()|");
						String query_var_string = query_string.nextToken();
						VariableState query_var = new VariableState(network.getVar(query_var_string.split("=")[0]), query_var_string.split("=")[1]);
						Vector<VariableState> evidence = new Vector<VariableState>();
						String evidence_strings = query_string.nextToken();
						StringTokenizer evidence_strings_token = new StringTokenizer(evidence_strings, ",= ");
						while(evidence_strings_token.hasMoreTokens()){
							evidence.add(new VariableState(network.getVar(evidence_strings_token.nextToken()), evidence_strings_token.nextToken()));
						}
						Vector<Variable> hiddens = new Vector<Variable>();
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
						
						if(!first_line) buffer_writer.newLine();
						buffer_writer.write(result);
						if(first_line) first_line = false;
					}
					else{
						StringTokenizer query_string = new StringTokenizer(line, "|");
						String queries_var_string = query_string.nextToken();
						StringTokenizer queries_var_string_token = new StringTokenizer(queries_var_string, "- ");
						Variable var1 = network.getVar(queries_var_string_token.nextToken());
						Variable var2 = network.getVar(queries_var_string_token.nextToken());
						Vector<Variable> givens = new Vector<Variable>();
						if(query_string.hasMoreTokens()){
							String queries_givens_string = query_string.nextToken();
							StringTokenizer queries_givens_string_token = new StringTokenizer(queries_givens_string, ", ");
							while(queries_givens_string_token.hasMoreTokens()){
								givens.add(network.getVar(queries_givens_string_token.nextToken().split("=")[0]));
							}
						}
						String result = (!network.BayesBall(var1, var2, null, givens, true)) ? "yes" : "no";
						
						if(!first_line) buffer_writer.newLine();
						buffer_writer.write(result);
						if(first_line) first_line = false;
					}
					line = (buffer_reader.ready()) ? buffer_reader.readLine() : "";
				}
			}
		}
		buffer_reader.close();
		file_reader.close();
		buffer_writer.close();
		file_writer.close();
	}
}
