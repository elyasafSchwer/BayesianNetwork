class CptConditionProbability{
	private CptVariableCondition state;
	private double value;

	public CptConditionProbability(CptVariableCondition state, double value){
		this.state = state;
		this.value = value;
	}

	public String toString(){
		return this.state.toString() + " : " + this.value;
	}

	public CptVariableCondition getState() {
		return this.state;
	}

	public double getValue() {
		return this.value;
	}
}