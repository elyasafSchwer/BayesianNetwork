import java.util.ArrayList;
import java.util.List;

class FactorVariablesState{
	private List<VariableCondition> variableState;

	//copy constructor
	public FactorVariablesState(List<VariableCondition> variable_state){
		this.variableState = new ArrayList<VariableCondition>(variable_state);
	}
	//constructor from cpt - every "var - state - parent state" become to "vars - state"
	public FactorVariablesState(CptVariableCondition cptvariableState){
		this.variableState = new ArrayList<VariableCondition>(cptvariableState.getVariableState());
		this.variableState.add(new VariableCondition(cptvariableState.getVariable(), cptvariableState.getVariable().getVlueAt(cptvariableState.myValue)));
	}

	public String toString(){
		return this.variableState.toString();
	}

	public List<VariableCondition> getVariableState() {
		return this.variableState;
	}

	public VariableCondition stateOf(Variable var){
		for (VariableCondition variable_state_it : this.variableState) {
			if(variable_state_it.getVariable() == var) return variable_state_it;
		}
		return null;
	}

	public boolean equals(FactorVariablesState other){
		for (VariableCondition variable_state_it1 : this.variableState) {
			boolean contain = false;
			for (VariableCondition variable_state_it2 : other.variableState) {
				if (variable_state_it1.equals(variable_state_it2)) contain = true;
			}
			if(!contain) return false;
		}
		for (VariableCondition variable_state_it1 : other.variableState) {
			boolean contain = false;
			for (VariableCondition variable_state_it2 : this.variableState) {
				if (variable_state_it1.equals(variable_state_it2)) contain = true;
			}
			if(!contain) return false;
		}
		return true;
	}
}