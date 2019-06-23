import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.Map.Entry;

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
