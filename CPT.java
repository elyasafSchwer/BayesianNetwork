import java.util.Vector;

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