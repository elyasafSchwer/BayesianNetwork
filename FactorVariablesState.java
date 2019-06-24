import java.util.Vector;

class FactorVariablesState{
	private Vector<VariableCondition> variable_state;

	//copy constructor
	public FactorVariablesState(Vector<VariableCondition> variable_state){
		this.variable_state = new Vector<VariableCondition>(variable_state);
	}
	//constructor from cpt - every "var - state - parent state" become to "vars - state"
	public FactorVariablesState(CptVariableCondition cptvariableState){
		this.variable_state = new Vector<VariableCondition>(cptvariableState.getVariableState());
		this.variable_state.add(new VariableCondition(cptvariableState.getVariable(), cptvariableState.getVariable().getVlueAt(cptvariableState.myValue)));
	}

	public String toString(){
		return this.variable_state.toString();
	}

	public Vector<VariableCondition> getVariableState() {
		return this.variable_state;
	}

	public VariableCondition stateOf(Variable var){
		for (VariableCondition variable_state_it : this.variable_state) {
			if(variable_state_it.getVariable() == var) return variable_state_it;
		}
		return null;
	}

	public boolean equals(FactorVariablesState other){
		for (VariableCondition variable_state_it1 : this.variable_state) {
			boolean contain = false;
			for (VariableCondition variable_state_it2 : other.variable_state) {
				if (variable_state_it1.equals(variable_state_it2)) contain = true;
			}
			if(!contain) return false;
		}
		for (VariableCondition variable_state_it1 : other.variable_state) {
			boolean contain = false;
			for (VariableCondition variable_state_it2 : this.variable_state) {
				if (variable_state_it1.equals(variable_state_it2)) contain = true;
			}
			if(!contain) return false;
		}
		return true;
	}
}