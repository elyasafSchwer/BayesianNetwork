import java.util.ArrayList;
import java.util.List;

class CPT{
	private Variable variable;
	private List<CptConditionProbability> table;

	public CPT(Variable variable){
		this.variable = variable;
		this.table = new ArrayList<CptConditionProbability>();
	}

	public void addToCPT(CptConditionProbability cpt_state_probability){
		this.table.add(cpt_state_probability);
	}
	public String getTitle(){
		return (variable.getParents().isEmpty()) ?  "P("+variable.getName()+")" : "P("+variable.getName() + " | " + this.variable.getParentsName() + ") ";
	}
	public String toString(){
		String result = getTitle() + "\n";
		for (CptConditionProbability cptState : table) {
			result+=cptState.toString()+"\n";
		}
		return result;
	}

	public Variable getVariable() {
		return this.variable;
	}

	public List<CptConditionProbability> getTable() {
		return this.table;
	}
}