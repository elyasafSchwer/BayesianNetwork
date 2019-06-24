import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

class Factor implements Comparable<Factor>{
	private List<VariableCondition> intersection;
	private List<VariableCondition> evidence;
	private List<FactorStateProbability> table;

	private void init_factors(){
		this.intersection = new ArrayList<VariableCondition>();
		this.evidence = new ArrayList<VariableCondition>();
		this.table = new ArrayList<FactorStateProbability>();
	}

//build factor from cpt
	public Factor(CPT cpt){		
		init_factors();
		//in this case (copy from cpt) intersection is only one variable
		this.intersection.add(new VariableCondition(cpt.getVariable()));
		//build my title
		for (Variable var : cpt.getVariable().getParents()) {
			this.evidence.add(new VariableCondition(var));
		}
		//add to factor all state_proprability from cpt (without last value)
		for (CptConditionProbability cpt_state_probability : cpt.getTable()){
			this.table.add(new FactorStateProbability(cpt_state_probability));
		}
		//save sum of cpt value and the set of main var value that present in cpt for all state
		HashMap<List<VariableCondition>, Double> sum_state_prob_from_cpt = new HashMap<List<VariableCondition>, Double>();
		HashMap<List<VariableCondition>, List<VariableCondition>> set_of_var_present_cpt = new HashMap<List<VariableCondition>, List<VariableCondition>>();
		for(CptConditionProbability cpt_state_probability : cpt.getTable()){
			if(!(sum_state_prob_from_cpt.containsKey(cpt_state_probability.getState().getVariableState()))){
				sum_state_prob_from_cpt.put(cpt_state_probability.getState().getVariableState(), cpt_state_probability.getValue());
				set_of_var_present_cpt.put(cpt_state_probability.getState().getVariableState(), new ArrayList<VariableCondition>());
				set_of_var_present_cpt.get(cpt_state_probability.getState().getVariableState()).add(cpt_state_probability.getState().getMyVariableState());
			}
			else{
				double old_sum = sum_state_prob_from_cpt.get(cpt_state_probability.getState().getVariableState());
				sum_state_prob_from_cpt.put(cpt_state_probability.getState().getVariableState(), old_sum + cpt_state_probability.getValue());
				set_of_var_present_cpt.get(cpt_state_probability.getState().getVariableState()).add(cpt_state_probability.getState().getMyVariableState());
			}
		}
		//make full main value set
		List<VariableCondition> all_state_of_var = new ArrayList<VariableCondition>();
		for(int i=0; i<cpt.getVariable().getValues().size(); i++){
			all_state_of_var.add(new VariableCondition(cpt.getVariable(), cpt.getVariable().getVlueAt(i)));
		}
		//for all parent-state, find that value that not present in cpt and add to factor with 1-sum value; 
		for (Entry<List<VariableCondition>, Double> vars_state_and_sum : sum_state_prob_from_cpt.entrySet()){
			for(VariableCondition variable_state : all_state_of_var){
				boolean contains = false;
				for(VariableCondition cpt_var_state : set_of_var_present_cpt.get(vars_state_and_sum.getKey())){
					if(cpt_var_state.equals(variable_state)) contains = true;
				}
				if(!contains){
					List<VariableCondition> new_state = new ArrayList<VariableCondition>(vars_state_and_sum.getKey());
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
			this.intersection.add(new VariableCondition(var));
		}
		//P(X|Y) x P(X|Z) --> P(X|Y,Z)
		else{
			this.evidence.add(new VariableCondition(var));
		}
		//add another variable to my title.
		for (VariableCondition variable_state : factor1.intersection) {
			if(variable_state.getVariable() != var) this.intersection.add(variable_state);
		}
		for (VariableCondition variable_state : factor1.evidence) {
			if(variable_state.getVariable() != var) this.evidence.add(variable_state);
		}
		for (VariableCondition variable_state : factor2.intersection) {
			if(variable_state.getVariable() != var) this.intersection.add(variable_state);
		}
		for (VariableCondition variable_state : factor2.evidence) {
			if(variable_state.getVariable() != var) this.evidence.add(variable_state);
		}

		for (FactorStateProbability factor_state_probability1 : factor1.table) {
			for (FactorStateProbability factor_state_probability2 : factor2.table) {
				if(factor_state_probability1.getState().stateOf(var).equals(factor_state_probability2.getState().stateOf(var))){
					List<VariableCondition> new_variable_state = new ArrayList<VariableCondition>();
					new_variable_state.add(factor_state_probability1.getState().stateOf(var));
					//add another var-sate to sate. (It can be assumed that there are no other identical variables in the two factors)
					for (VariableCondition variable_state1 : factor_state_probability1.getState().getVariableState()) {
						if(variable_state1.getVariable() != var) new_variable_state.add(variable_state1);
					}
					for (VariableCondition variable_state2 : factor_state_probability2.getState().getVariableState()) {
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

		this.intersection = new ArrayList<VariableCondition>(factor.intersection);
		this.evidence = new ArrayList<VariableCondition>(factor.evidence);
		this.table = new ArrayList<FactorStateProbability>();
		//P(X|Var) --> P(X)
		for (VariableCondition variable_state : this.intersection) {
			if(variable_state.getVariable() == var) {this.intersection.remove(variable_state); break;}
		}
		//P(Var,X|Y) --> P(X|Y)
		for (VariableCondition variable_state : this.evidence) {
			if(variable_state.getVariable() == var) {this.evidence.remove(variable_state); break;}
		}

		List<FactorStateProbability> oldTable = new ArrayList<FactorStateProbability>(factor.table);

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
		List<VariableCondition> copy_state = new ArrayList<VariableCondition>(state.getVariableState());
		for (VariableCondition variable_state : copy_state) {
			if(variable_state.getVariable() == var) {copy_state.remove(variable_state); break;}
		}
		return new FactorVariablesState(copy_state);
	}

	public List<Variable> getEvidenceVariable(){
		List<Variable> result = new ArrayList<Variable>();
		for (VariableCondition variable_state : evidence) {
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

	public void removeEvidenceState(VariableCondition evidence_variable_state) {
		for(Iterator<VariableCondition> it = this.evidence.iterator(); it.hasNext();){
			VariableCondition variable_state = it.next();
			if(variable_state.haveValue() && variable_state.getVariable() == evidence_variable_state.getVariable() && !variable_state.equals(evidence_variable_state)){
				it.remove();
				evidence.add(variable_state);
			}
		}
		for(Iterator<VariableCondition> it = this.intersection.iterator(); it.hasNext();){
			VariableCondition variable_state = it.next();
			if(variable_state.haveValue() && variable_state.getVariable() == evidence_variable_state.getVariable() && !variable_state.equals(evidence_variable_state)){
				it.remove();
				evidence.add(variable_state);
			}
		}
		for (Iterator<FactorStateProbability> it = this.table.iterator(); it.hasNext();){
			FactorStateProbability factor_state_probability = it.next();
			boolean contains_evidence = false;
			for(VariableCondition variable_state : factor_state_probability.getState().getVariableState()){
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
		for (VariableCondition variable_state : evidence) {
			if(variable_state.getVariable() == var)
				return true;
		}
		for(VariableCondition variable_state : intersection){
			if(variable_state.getVariable() == var)
				return true;
		}
		return false;
	}

	public String listWithoutBracket(String list_string){
		return list_string.substring(1, list_string.length()-1);
	}

	public boolean evidenceContain(Variable var){
		for (VariableCondition variable_state : evidence) {
			if (var == variable_state.getVariable()){
				return true;
			}
		}
		return false;
	}

	public boolean intersectionContain(Variable var){
		for (VariableCondition variable_state : intersection) {
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

	public List<VariableCondition> getIntersection() {
		return this.intersection;
	}

	public String normalize(VariableCondition query_state) {
		double numerator = 0;
		double denominator = 0;
		int sum_count = 0;
		for (FactorStateProbability factor_state_probability : this.table) {
			for (VariableCondition variable_state : factor_state_probability.getState().getVariableState()) {
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
