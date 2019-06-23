import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import java.util.Vector;

class Variable{
	private String name;
	private Vector<String> values;
	private Vector<Variable> parents;
	private Vector<Variable> suns; 
	private CPT cpt;

	public Variable(String name){
		this.name = name;
		this.values = new Vector<String>();
		this.parents = new Vector<Variable>();
		this.suns = new Vector<Variable>();
		this.cpt = new CPT(this);
	}

	public void setValues(Vector<String> values){
		this.values = new Vector<String>(values);
	}

	public void setParents(Vector<Variable> parents){
		this.parents = new Vector<Variable>(parents);
		for (Variable parent : parents){
			parent.suns.add(this);
		}
	}

	public String toString(){
		return name + " - " + this.values.toString();	
	}

	public String getVlueAt(int i){
		return this.values.get(i);
	}

	public int indexOf(String value){
		return this.values.indexOf(value);
	}

	public void addtoCPT(CptStateProbability cpt_state_probability){
		this.cpt.addToCPT(cpt_state_probability);
	}

	public Vector<Variable> getParents() {
		return this.parents;
	}

	public String getName() {
		return this.name;
	}

	public CPT getCPT() {
		return this.cpt;
	}

	public Vector<String> getValues(){
		return this.values;
	}

	public String getParentsName(){
		return Arrays.toString(this.parents.stream().map(a->a.name).toArray()).substring(1, Arrays.toString(this.parents.stream().map(a->a.name).toArray()).length()-1);
	}

	public boolean isAncestorOfOneOf(Vector<Variable> variables) {
		if(suns.size() == 0) return false;
		for (Variable sun : suns){
			if(variables.contains(sun)) return true;
		}
		for (Variable sun : suns) {
			if(sun.isAncestorOfOneOf(variables)) return true;
		}
		return false;
	}

	public Vector<Variable> getSuns() {
		return this.suns;
	}
}

class VariableState{
	private Variable variable;
	private int value;
	public VariableState(Variable variable, String value){
		this.variable = variable;
		this.value = variable.indexOf(value);
	}
	public VariableState(Variable variable){
		this.variable = variable;
		this.value = -1;
	}
	public String toString(){
		return (this.value!=-1) ? this.variable.getName() + "=" + this.variable.getVlueAt(this.value) : this.variable.getName();
	}

	public boolean equals(VariableState other){
		return (this.variable == other.variable && this.value == other.value);
		//yes, I check if the pointers of variable is equals.
	}
	public Variable getVariable() {
		return this.variable;
	}
	public String getValue(){
		return (this.value!=-1) ? this.variable.getVlueAt(value) : "none";
	}
	public boolean haveValue(){
		return value > -1;
	}
}

/**this object contains variable, value for him, and state for each parents**/
class CptVariableState{
	private Variable variable;
	private Vector<VariableState> variable_states;
	int myValue;

	public CptVariableState(Variable variable, 	Vector<VariableState> variableState , String value){
		this.variable = variable;
		this.variable_states = new Vector<VariableState>(variableState);
		this.myValue = this.variable.indexOf(value);
	}

	public boolean equals_state(CptVariableState other){
		for (VariableState cptVariableEqualValue : this.variable_states) {
			if(!(other.variable_states.contains(cptVariableEqualValue))) return false;
		}
		for (VariableState cptVariableEqualValue : other.variable_states) {
			if(!(this.variable_states.contains(cptVariableEqualValue))) return false;
		}
		return true;
	}

	public boolean equals (CptVariableState other){
		return (this.variable == other.variable && this.myValue == other.myValue && equals_state(other));
	}

	public Variable getVariable(){
		return this.variable;
	}

	public VariableState getMyVariableState(){
		return new VariableState(variable, variable.getVlueAt(myValue));
	}
	
	public Vector<VariableState> getVariableState() {
		return this.variable_states;
	}

	public String toString(){
		return this.variable.getName() + " = " + this.variable.getVlueAt(myValue) + " | " + this.variable_states.toString();
	}

}

class CptStateProbability{
	private CptVariableState state;
	private double value;

	public CptStateProbability(CptVariableState state, double value){
		this.state = state;
		this.value = value;
	}

	public String toString(){
		return this.state.toString() + " : " + this.value;
	}

	public CptVariableState getState() {
		return this.state;
	}

	public double getValue() {
		return this.value;
	}
}

class CPT{
	private Variable variable;
	private Vector<CptStateProbability> table;

	public CPT(Variable variable){
		this.variable = variable;
		this.table = new Vector<CptStateProbability>();
	}

	public void addToCPT(CptStateProbability cpt_state_probability){
		this.table.add(cpt_state_probability);
	}
	public String getTitle(){
		return (variable.getParents().isEmpty()) ?  "P("+variable.getName()+")" : "P("+variable.getName() + " | " + this.variable.getParentsName() + ") ";
	}
	public String toString(){
		String result = getTitle() + "\n";
		for (CptStateProbability cptState : table) {
			result+=cptState.toString()+"\n";
		}
		return result;
	}

	public Variable getVariable() {
		return this.variable;
	}

	public Vector<CptStateProbability> getTable() {
		return this.table;
	}
}

/**this object contains state of some variable without main variable**/
class FactorVariablesState{
	private Vector<VariableState> variable_state;

	//copy constructor
	public FactorVariablesState(Vector<VariableState> variable_state){
		this.variable_state = new Vector<VariableState>(variable_state);
	}
	//constructor from cpt - every "var - state - parent state" become to "vars - state"
	public FactorVariablesState(CptVariableState cptvariableState){
		this.variable_state = new Vector<VariableState>(cptvariableState.getVariableState());
		this.variable_state.add(new VariableState(cptvariableState.getVariable(), cptvariableState.getVariable().getVlueAt(cptvariableState.myValue)));
	}

	public String toString(){
		return this.variable_state.toString();
	}

	public Vector<VariableState> getVariableState() {
		return this.variable_state;
	}

	public VariableState stateOf(Variable var){
		for (VariableState variable_state_it : this.variable_state) {
			if(variable_state_it.getVariable() == var) return variable_state_it;
		}
		return null;
	}

	public boolean equals(FactorVariablesState other){
		for (VariableState variable_state_it1 : this.variable_state) {
			boolean contain = false;
			for (VariableState variable_state_it2 : other.variable_state) {
				if (variable_state_it1.equals(variable_state_it2)) contain = true;
			}
			if(!contain) return false;
		}
		for (VariableState variable_state_it1 : other.variable_state) {
			boolean contain = false;
			for (VariableState variable_state_it2 : this.variable_state) {
				if (variable_state_it1.equals(variable_state_it2)) contain = true;
			}
			if(!contain) return false;
		}
		return true;
	}
}

class FactorStateProbability{
	private FactorVariablesState state;
	private double value;

	public FactorStateProbability(FactorVariablesState state, double value){
		this.state = state;
		this.value = value;
	}

	public FactorStateProbability(CptStateProbability cpt_state_probability){
		this.state = new FactorVariablesState(cpt_state_probability.getState()); 
		this.value = cpt_state_probability.getValue();
	}

	public FactorVariablesState getState(){
		return this.state;
	}

	public double getValue() {
		return this.value;
	}

	public String toString(){
		return this.state.toString() + " : " + String.format("%.8f", value);
	}
}

//this object form is ([intersection] | [evidence] ) - the title state and table for each intersection value;
class Factor implements Comparable<Factor>{
	private Vector<VariableState> intersection;
	private Vector<VariableState> evidence;
	private Vector<FactorStateProbability> table;

	private void init_factors(){
		this.intersection = new Vector<VariableState>();
		this.evidence = new Vector<VariableState>();
		this.table = new Vector<FactorStateProbability>();
	}

//build factor from cpt
	public Factor(CPT cpt){		
		init_factors();
		//in this case (copy from cpt) intersection is only one variable
		this.intersection.add(new VariableState(cpt.getVariable()));
		//build my title
		for (Variable var : cpt.getVariable().getParents()) {
			this.evidence.add(new VariableState(var));
		}
		//add to factor all state_proprability from cpt (without last value)
		for (CptStateProbability cpt_state_probability : cpt.getTable()){
			this.table.add(new FactorStateProbability(cpt_state_probability));
		}
		//save sum of cpt value and the set of main var value that present in cpt for all state
		HashMap<Vector<VariableState>, Double> sum_state_prob_from_cpt = new HashMap<Vector<VariableState>, Double>();
		HashMap<Vector<VariableState>, Vector<VariableState>> set_of_var_present_cpt = new HashMap<Vector<VariableState>, Vector<VariableState>>();
		for(CptStateProbability cpt_state_probability : cpt.getTable()){
			if(!(sum_state_prob_from_cpt.containsKey(cpt_state_probability.getState().getVariableState()))){
				sum_state_prob_from_cpt.put(cpt_state_probability.getState().getVariableState(), cpt_state_probability.getValue());
				set_of_var_present_cpt.put(cpt_state_probability.getState().getVariableState(), new Vector<VariableState>());
				set_of_var_present_cpt.get(cpt_state_probability.getState().getVariableState()).add(cpt_state_probability.getState().getMyVariableState());
			}
			else{
				double old_sum = sum_state_prob_from_cpt.get(cpt_state_probability.getState().getVariableState());
				sum_state_prob_from_cpt.put(cpt_state_probability.getState().getVariableState(), old_sum + cpt_state_probability.getValue());
				set_of_var_present_cpt.get(cpt_state_probability.getState().getVariableState()).add(cpt_state_probability.getState().getMyVariableState());
			}
		}
		//make full main value set
		Vector<VariableState> all_state_of_var = new Vector<VariableState>();
		for(int i=0; i<cpt.getVariable().getValues().size(); i++){
			all_state_of_var.add(new VariableState(cpt.getVariable(), cpt.getVariable().getVlueAt(i)));
		}
		//for all parent-state, find that value that not present in cpt and add to factor with 1-sum value; 
		for (Entry<Vector<VariableState>, Double> vars_state_and_sum : sum_state_prob_from_cpt.entrySet()){
			for(VariableState variable_state : all_state_of_var){
				boolean contains = false;
				for(VariableState cpt_var_state : set_of_var_present_cpt.get(vars_state_and_sum.getKey())){
					if(cpt_var_state.equals(variable_state)) contains = true;
				}
				if(!contains){
					Vector<VariableState> new_state = new Vector<VariableState>(vars_state_and_sum.getKey());
					new_state.add(variable_state);
					this.table.add(new FactorStateProbability(new FactorVariablesState(new_state), 1 - vars_state_and_sum.getValue()));
				}
			}
		}
	}

	//join factor1 and factor2 where var = var to a new factor.
	public Factor(Factor factor1, Factor factor2, Variable var){
		init_factors();
		int mul_count = 0;

		//P(X|Y) x P(Y|Z) --> P(X,Y|Z)
		if( (factor1.evidenceContain(var) && factor2.intersectionContain(var)) || (factor1.intersectionContain(var) && factor2.evidenceContain(var)) ){
			this.intersection.add(new VariableState(var));
		}
		//P(X|Y) x P(X|Z) --> P(X|Y,Z)
		else{
			this.evidence.add(new VariableState(var));
		}
		//add another variable to my title.
		for (VariableState variable_state : factor1.intersection) {
			if(variable_state.getVariable() != var) this.intersection.add(variable_state);
		}
		for (VariableState variable_state : factor1.evidence) {
			if(variable_state.getVariable() != var) this.evidence.add(variable_state);
		}
		for (VariableState variable_state : factor2.intersection) {
			if(variable_state.getVariable() != var) this.intersection.add(variable_state);
		}
		for (VariableState variable_state : factor2.evidence) {
			if(variable_state.getVariable() != var) this.evidence.add(variable_state);
		}

		for (FactorStateProbability factor_state_probability1 : factor1.table) {
			for (FactorStateProbability factor_state_probability2 : factor2.table) {
				if(factor_state_probability1.getState().stateOf(var).equals(factor_state_probability2.getState().stateOf(var))){
					Vector<VariableState> new_variable_state = new Vector<VariableState>();
					new_variable_state.add(factor_state_probability1.getState().stateOf(var));
					//add another var-sate to sate. (It can be assumed that there are no other identical variables in the two factors)
					for (VariableState variable_state1 : factor_state_probability1.getState().getVariableState()) {
						if(variable_state1.getVariable() != var) new_variable_state.add(variable_state1);
					}
					for (VariableState variable_state2 : factor_state_probability2.getState().getVariableState()) {
						if(variable_state2.getVariable() != var) new_variable_state.add(variable_state2);
					}
					mul_count++;
					this.table.add(new FactorStateProbability(new FactorVariablesState(new_variable_state), factor_state_probability1.getValue() * factor_state_probability2.getValue()));
				}
			}
		}
		System.out.println(mul_count + " muls \n");
		BayesianNetwork.muls +=mul_count;
	}
	//aliminate factor according to variable to a new factor. 
	public Factor(Factor factor, Variable var){
		int count_sums = 0;

		this.intersection = new Vector<VariableState>(factor.intersection);
		this.evidence = new Vector<VariableState>(factor.evidence);
		this.table = new Vector<FactorStateProbability>();
		//P(X|Var) --> P(X)
		for (VariableState variable_state : this.intersection) {
			if(variable_state.getVariable() == var) {this.intersection.remove(variable_state); break;}
		}
		//P(Var,X|Y) --> P(X|Y)
		for (VariableState variable_state : this.evidence) {
			if(variable_state.getVariable() == var) {this.evidence.remove(variable_state); break;}
		}

		Vector<FactorStateProbability> oldTable = new Vector<FactorStateProbability>(factor.table);

		while(!oldTable.isEmpty()){
			FactorVariablesState state_without_var = copyStateWithoutVar(oldTable.get(0).getState(), var);
			double sum = 0;
			for(Iterator<FactorStateProbability> it = oldTable.iterator(); it.hasNext();){
				FactorStateProbability fsp = it.next();
				if(copyStateWithoutVar(fsp.getState(), var).equals(state_without_var)){
					if(sum!=0) count_sums++;
					sum += fsp.getValue();
					it.remove();
				}
			}
			this.table.add(new FactorStateProbability(state_without_var, sum));
		}
		System.out.println("\n"+count_sums+ " sum"+"\n");
		BayesianNetwork.sums +=count_sums;
	}

	public FactorVariablesState copyStateWithoutVar(FactorVariablesState state, Variable var){
		Vector<VariableState> copy_state = new Vector<VariableState>(state.getVariableState());
		for (VariableState variable_state : copy_state) {
			if(variable_state.getVariable() == var) {copy_state.remove(variable_state); break;}
		}
		return new FactorVariablesState(copy_state);
	}

	public Vector<Variable> getEvidenceVariable(){
		Vector<Variable> result = new Vector<Variable>();
		for (VariableState variable_state : evidence) {
			result.add(variable_state.getVariable());
		}
		return result;
	}

	public String toString(){
		String result = (!this.evidence.isEmpty()) ? "P(" + listWithoutBracket(this.intersection.toString()) + " | " + listWithoutBracket(this.evidence.toString()) + ")\n" : "P(" + listWithoutBracket(this.intersection.toString()) + ")\n";
		for (FactorStateProbability factor_state_probability : this.table) {
			result+=factor_state_probability.toString() + "\n";
		}
		return result+"\n";
	}

	public String getTitle(){
		return ((!this.evidence.isEmpty()) ? "P(" + listWithoutBracket(this.intersection.toString()) + " | " + listWithoutBracket(this.evidence.toString()) + ")" : "P(" + listWithoutBracket(this.intersection.toString()) + ")") + " - " + this.table.size()+" ";
	}

	public void removeEvidenceState(VariableState evidence_variable_state) {
		for(Iterator<VariableState> it = this.evidence.iterator(); it.hasNext();){
			VariableState variable_state = it.next();
			if(variable_state.haveValue() && variable_state.getVariable() == evidence_variable_state.getVariable() && !variable_state.equals(evidence_variable_state)){
				it.remove();
				evidence.add(variable_state);
			}
		}
		for(Iterator<VariableState> it = this.intersection.iterator(); it.hasNext();){
			VariableState variable_state = it.next();
			if(variable_state.haveValue() && variable_state.getVariable() == evidence_variable_state.getVariable() && !variable_state.equals(evidence_variable_state)){
				it.remove();
				evidence.add(variable_state);
			}
		}
		for (Iterator<FactorStateProbability> it = this.table.iterator(); it.hasNext();){
			FactorStateProbability factor_state_probability = it.next();
			boolean contains_evidence = false;
			for(VariableState variable_state : factor_state_probability.getState().getVariableState()){
				if(variable_state.getVariable().equals(evidence_variable_state.getVariable()) && !(variable_state.equals(evidence_variable_state))){
					contains_evidence = true;
				}
			}
			if(contains_evidence){
				it.remove();
			}
		}
	}

	boolean isRefers(Variable var){
		for (VariableState variable_state : evidence) {
			if(variable_state.getVariable() == var)
				return true;
		}
		for(VariableState variable_state : intersection){
			if(variable_state.getVariable() == var)
				return true;
		}
		return false;
	}

	public String listWithoutBracket(String list_string){
		return list_string.substring(1, list_string.length()-1);
	}

	public boolean evidenceContain(Variable var){
		for (VariableState variable_state : evidence) {
			if (var == variable_state.getVariable()){
				return true;
			}
		}
		return false;
	}

	public boolean intersectionContain(Variable var){
		for (VariableState variable_state : intersection) {
			if (var == variable_state.getVariable()){
				return true;
			}
		}
		return false;
	}

	public int compareTo(Factor other) {
		if (this.table.size() > other.table.size()) return 1;
		return -1;
	}

	public Vector<VariableState> getIntersection() {
		return this.intersection;
	}

	public String normalize(VariableState query_state) {
		double numerator = 0;
		double denominator = 0;
		int sum_count = 0;
		for (FactorStateProbability factor_state_probability : this.table) {
			for (VariableState variable_state : factor_state_probability.getState().getVariableState()) {
				if(variable_state.equals(query_state)){
					if(numerator!=0) sum_count++;
					numerator+=factor_state_probability.getValue();
				}
			}
			if(denominator != 0) sum_count++;
			denominator+=factor_state_probability.getValue();
		}
		System.out.println(sum_count+ " sums\n");
		BayesianNetwork.sums+=sum_count;
		return String.format("%.5f", numerator/denominator);
	}

	public int getTableLength() {
		return this.table.size();
	}
}

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
