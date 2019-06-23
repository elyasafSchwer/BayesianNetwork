import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.Map.Entry;

class BayesianNetwork {

	HashMap<String, Variable> variables;

	public BayesianNetwork(){
		this.variables = new HashMap<String, Variable>();
	}

	public void addVariable(String name){
		this.variables.put(name, new Variable(name));
	}

	public void setValues(String name, Vector<String> values){
		getVar(name).setValues(values);
	}

	public void setParents(String name, Vector<Variable> parents){
		getVar(name).setParents(parents);
	}

	public Variable getVar(String name){
		return variables.get(name);
	}

	public VariableState createCptVariableEqualValue(String name, String value){
		return new VariableState(getVar(name), value);
	}

	public CptVariableState createCptVariableState(String name, Vector<VariableState> variableState, String value){
		return new CptVariableState(getVar(name), variableState, value);
	}

	public CptStateProbability createCptStateProbability(CptVariableState state, double value){
		return new CptStateProbability(state, value);
	}

	public void addToCPT(String name, CptStateProbability cpt_state_probability){
		getVar(name).addtoCPT(cpt_state_probability);
	}

	public CPT getCPT(String name){
		return getVar(name).getCPT();
	}

	public void sysoFactor(Vector<Factor> factors){
		System.out.println("Factor:");
		for (Factor factor : factors) {
			System.out.println(factor);
		}
	}
	public static int sums;
	public static int muls;
	public String P(VariableState query_state,  Vector<VariableState> evidence, Vector<Variable> hiddens){

		sums = 0;
		muls = 0;
		
		Vector <Variable> remaining_var = new Vector<Variable>(variables.values());
		
		System.out.println("------------------Q:P(" + query_state + " | " + evidence.toString().substring(1, evidence.toString().length()-1) +") - " + ((hiddens.isEmpty()) ? "" : Arrays.toString(hiddens.stream().map(a->a.getName()).toArray())) +"-------------------");

		Vector<Factor> factors = new Vector<Factor>();
		Vector<Variable> visible_vars = new Vector<Variable>();
		visible_vars.add(query_state.getVariable());
		for (VariableState var_state : evidence){
			visible_vars.add(var_state.getVariable());
		}
		for (Entry<String, Variable> entry : variables.entrySet()) {
			//if this var is visible var OR is a ancestor of one of visible var make his factor.
			if(visible_vars.contains(entry.getValue()) || entry.getValue().isAncestorOfOneOf(visible_vars)) factors.add(new Factor(entry.getValue().getCPT()));
		}

		sysoFactor(factors);

		System.out.println("instantiated by evidence:\n");
		//remove all not evidence state for all evidence var.
		for(Factor factor: factors){
			for(VariableState evidence_state : evidence){
				factor.removeEvidenceState(evidence_state);
			}
		}
		//remove all table that length <=1.
		for(Iterator<Factor> it = factors.iterator(); it.hasNext();){
			Factor factor = it.next();
			if(factor.getTableLength() <= 1){
				it.remove();
			}
		}

		sysoFactor(factors);

		for(Iterator<Factor> it = factors.iterator(); it.hasNext();){
			Factor factor = it.next();
			if(factor.getTableLength() <= 1){
				it.remove();
			}
		}

		System.out.println("--------------------JOIN AND ELIMINATE--------------------\n");
		while(!hiddens.isEmpty()){
			System.out.println("join and eliminate by " + hiddens.get(0).getName()+":");
			join(hiddens.get(0), factors);
			eliminate(hiddens.get(0), factors);
			remaining_var.remove(hiddens.get(0));
			hiddens.remove(0);
		}
		
		System.out.println("join all remaining factors:\n");
		sysoFactor(factors);
		for (Variable var : remaining_var) {
				System.out.println("join by " + var.getName()+":");
				join(var, factors);
		}

		sysoFactor(factors);
		return normalize(query_state, factors.get(0)) + "," + sums + "," +muls;
	}

	public String normalize(VariableState query_sata, Factor last_factor){
		return last_factor.normalize(query_sata);
	}

	public void eliminate(Variable hidden, Vector<Factor> factors){
		for (Factor factor : factors) {
			if(factor.isRefers(hidden)){
				System.out.println("eliminate:");
				Factor eliminate_hidden = new Factor(factor, hidden);
				System.out.println(eliminate_hidden);
				factors.remove(factor);
				factors.add(eliminate_hidden);
				return;
			}
		}
	}

	public void sysoFactorsTitles(Vector<Factor> factors){
		System.out.print("factors: ");
		for (Factor factor : factors) {
			System.out.print(factor.getTitle() +" ");
		}
		System.out.println("\n");
	}

	public void join(Variable hidden, Vector<Factor> factors){
		Vector<Factor> refer_hidden = new Vector<Factor>();
		for (Factor fact : factors){
			if (fact.isRefers(hidden)) refer_hidden.add(fact);
		}
		while(refer_hidden.size()>1){
			sysoFactorsTitles(factors);

			Factor factor1 = Collections.min(refer_hidden);
			refer_hidden.remove(factor1);
			factors.remove(factor1);
			Factor factor2 = Collections.min(refer_hidden);
			refer_hidden.remove(factor2);
			factors.remove(factor2);

			System.out.print(factor1);
			System.out.print("x\n\n"+factor2);

			Factor new_factor = new Factor(factor1, factor2, hidden);

			System.out.println("=\n\n"+new_factor);

			factors.add(new_factor);
			refer_hidden.add(new_factor);
		}
	}

	public boolean BayesBall(Variable var1, Variable var2, Variable last_visit, Vector<Variable> givens, boolean getFromSun){
		if (var1.equals(var2)) return true;
		if(!givens.contains(var1)){
			for (Variable sun: var1.getSuns()){
				if(!sun.equals(last_visit) && BayesBall(sun, var2, var1 ,givens, false)) return true;
			}
		}
		if(getFromSun || givens.contains(var1)){
			for (Variable parent: var1.getParents()){
				if(BayesBall(parent, var2, var1, givens, true)) return true;
			}
		}
		return false;
	}

	public String toString(){
		String result = "";
		for (Entry<String, Variable> entry : variables.entrySet()) {
			result+=entry.getValue().toString()+"\n";
		}
		result+="\n";
		for (Entry<String, Variable> entry : variables.entrySet()) {
			result+=entry.getValue().getCPT().toString()+"\n\n";
		}
		return result;
	}
}
