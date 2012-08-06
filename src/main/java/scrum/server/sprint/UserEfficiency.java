package scrum.server.sprint;

public class UserEfficiency {

	private Float efficiency;
	private Integer allBurnedHours = 0;
	private Integer initialBurnableHours = 0;
	private String burnedHoursPerInitial;

	public Float getEfficiency() {
		return efficiency;
	}

	public void setEfficiency(Float efficiency) {
		this.efficiency = efficiency;
	}

	public Integer getAllBurnedHours() {
		return allBurnedHours;
	}

	public void setAllBurnedHours(Integer allBurnedHours) {
		this.allBurnedHours = allBurnedHours;
	}

	public Integer getInitialBurnableHours() {
		return initialBurnableHours;
	}

	public void setInitialBurnableHours(Integer initialBurnableHours) {
		this.initialBurnableHours = initialBurnableHours;
	}

	public String getBurnedHoursPerInitial() {
		return burnedHoursPerInitial;
	}

	public void setBurnedHoursPerInitial(String burnedHoursPerInitial) {
		this.burnedHoursPerInitial = burnedHoursPerInitial;
	}

}