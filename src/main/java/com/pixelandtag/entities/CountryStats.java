package com.pixelandtag.entities;

/**
 * A country and its properties
 * 
 * @author Timothy Mwangi Gikonyo Date 4th March 2011
 * 
 * 
 */
public class CountryStats {

	String name;
	int columnPosition;

	int customerBase;
	int titalSubscribed;
	int avgActivePlayers;
	double smsRate;
	double avgNumSMSinMama;
	double percentPeopleParticipatingInMamas;
	double activeParticipantPerDayInMama;
	double ARPU;
	double customerSpendPerDat;
	double percentSpendPerCustomer;
	double percentPeopleParticipating;
	double percentActiveParticipantPerDay;
	double revenuePerDay;
	double revenuePerMonth;
	double inmobia;

	public int getColumnPosition() {
		return columnPosition;
	}

	public void setColumnPosition(int columnPosition) {
		this.columnPosition = columnPosition;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCustomerBase() {
		return customerBase;
	}

	public void setCustomerBase(int customerBase) {
		this.customerBase = customerBase;
	}

	public int getTitalSubscribed() {
		return titalSubscribed;
	}

	public void setTitalSubscribed(int titalSubscribed) {
		this.titalSubscribed = titalSubscribed;
	}

	public int getAvgActivePlayers() {
		return avgActivePlayers;
	}

	public void setAvgActivePlayers(int avgActivePlayers) {
		this.avgActivePlayers = avgActivePlayers;
	}

	public double getSmsRate() {
		return smsRate;
	}

	public void setSmsRate(double smsRate) {
		this.smsRate = smsRate;
	}

	public double getAvgNumSMSinMama() {
		return avgNumSMSinMama;
	}

	public void setAvgNumSMSinMama(double avgNumSMSinMama) {
		this.avgNumSMSinMama = avgNumSMSinMama;
	}

	public double getPercentPeopleParticipatingInMamas() {
		return percentPeopleParticipatingInMamas;
	}

	public void setPercentPeopleParticipatingInMamas(
			double percentPeopleParticipatingInMamas) {
		this.percentPeopleParticipatingInMamas = percentPeopleParticipatingInMamas;
	}

	public double getActiveParticipantPerDayInMama() {
		return activeParticipantPerDayInMama;
	}

	public void setActiveParticipantPerDayInMama(
			double activeParticipantPerDayInMama) {
		this.activeParticipantPerDayInMama = activeParticipantPerDayInMama;
	}

	public double getARPU() {
		return ARPU;
	}

	public void setARPU(double aRPU) {
		ARPU = aRPU;
	}

	public double getCustomerSpendPerDat() {
		return customerSpendPerDat;
	}

	public void setCustomerSpendPerDat(double customerSpendPerDat) {
		this.customerSpendPerDat = customerSpendPerDat;
	}

	public double getPercentSpendPerCustomer() {
		return percentSpendPerCustomer;
	}

	public void setPercentSpendPerCustomer(double percentSpendPerCustomer) {
		this.percentSpendPerCustomer = percentSpendPerCustomer;
	}

	public double getPercentPeopleParticipating() {
		return percentPeopleParticipating;
	}

	public void setPercentPeopleParticipating(double percentPeopleParticipating) {
		this.percentPeopleParticipating = percentPeopleParticipating;
	}

	public double getPercentActiveParticipantPerDay() {
		return percentActiveParticipantPerDay;
	}

	public void setPercentActiveParticipantPerDay(
			double percentActiveParticipantPerDay) {
		this.percentActiveParticipantPerDay = percentActiveParticipantPerDay;
	}

	public double getRevenuePerDay() {
		return revenuePerDay;
	}

	public void setRevenuePerDay(double revenuePerDay) {
		this.revenuePerDay = revenuePerDay;
	}

	public double getRevenuePerMonth() {
		return revenuePerMonth;
	}

	public void setRevenuePerMonth(double revenuePerMonth) {
		this.revenuePerMonth = revenuePerMonth;
	}

	public double getInmobia() {
		return inmobia;
	}

	public void setInmobia(double inmobia) {
		this.inmobia = inmobia;
	}

	/**
	 * 
	 * @param name
	 * @param customerBase
	 * @param titalSubscribed
	 * @param avgActivePlayers
	 * @param smsRate
	 * @param avgNumSMSinMama
	 * @param percentPeopleParticipatingInMamas
	 * @param activeParticipantPerDayInMama
	 * @param ARPU
	 * @param customerSpendPerDat
	 * @param percentSpendPerCustomer
	 * @param percentPeopleParticipating
	 * @param percentActiveParticipantPerDay
	 * @param revenuePerDay
	 * @param revenuePerMonth
	 * @param inmobia
	 */
	public CountryStats(String name, int customerBase, int titalSubscribed,
			int avgActivePlayers, double smsRate, double avgNumSMSinMama,
			double percentPeopleParticipatingInMamas,
			double activeParticipantPerDayInMama, double ARPU,
			double customerSpendPerDat, double percentSpendPerCustomer,
			double percentPeopleParticipating,
			double percentActiveParticipantPerDay, double revenuePerDay,
			double revenuePerMonth, double inmobia, int columnPosition) {

		this.name = name;
		this.customerBase = customerBase;
		this.titalSubscribed = titalSubscribed;
		this.avgActivePlayers = avgActivePlayers;
		this.smsRate = smsRate;
		this.avgNumSMSinMama = avgNumSMSinMama;
		this.percentPeopleParticipatingInMamas = percentPeopleParticipatingInMamas;
		this.activeParticipantPerDayInMama = activeParticipantPerDayInMama;
		this.ARPU = ARPU;
		this.customerSpendPerDat = customerSpendPerDat;
		this.percentSpendPerCustomer = percentSpendPerCustomer;
		this.percentPeopleParticipating = percentPeopleParticipating;
		this.percentActiveParticipantPerDay = percentActiveParticipantPerDay;
		this.revenuePerDay = revenuePerDay;
		this.revenuePerMonth = revenuePerMonth;
		this.inmobia = inmobia;
		this.columnPosition = columnPosition;

	}
}