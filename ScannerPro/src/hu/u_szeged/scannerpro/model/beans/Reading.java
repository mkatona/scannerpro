package hu.u_szeged.scannerpro.model.beans;

import java.util.Date;


/**
 * A reading of an utility meter
 *
 * @author krajcsovszkig
 *
 */
public class Reading
{
	private Date timestamp;
	private Customer customer;
	private int reading;
	
	/**
	 * Creates a new reading with the current timestamp
	 * 
	 * @param reading the value of the reading
	 */
	public Reading(int reading)
	{
		this.reading = reading;
		this.timestamp = new Date();
	}
	
	/**
	 * @return the customer the reading belongs to
	 */
	public Customer getCustomer()
	{
		return customer;
	}
	
	/**
	 * @param customer the customer the reading belongs to
	 */
	public void setCustomer(Customer customer)
	{
		this.customer = customer;
	}
	
	/**
	 * @return the timestamp of the reading
	 */
	public Date getTimestamp()
	{
		return timestamp;
	}
	
	/**
	 * @return the value of the reading
	 */
	public int getReading()
	{
		return reading;
	}
}
