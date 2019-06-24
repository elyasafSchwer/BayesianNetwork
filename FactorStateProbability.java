class FactorStateProbability{
	private FactorVariablesState state;
	private double value;

	public FactorStateProbability(FactorVariablesState state, double value){
		this.state = state;
		this.value = value;
	}

	public FactorStateProbability(CptConditionProbability cpt_state_probability){
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