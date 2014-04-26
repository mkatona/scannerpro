
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
	 * Create a new reading from the specified data. Only use this constructor
	 * when loading readings from eg. a database.
	 * 
	 * @param customer the customer the reading belongs to
	 * @param reading the value of the reading
	 * @param timestamp the timestamp of the reading
	 */
	public Reading(Customer customer, int reading, Date timestamp)
	{
		this.customer = customer;
		this.reading = reading;
		this.timestamp = timestamp;
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
	
	/**
	 * Only use this when loading readings from eg. a database
	 * 
	 * @param reading the value of the reading
	 */
	public void setReading(int reading)
	{
		this.reading = reading;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o)
	{
		return 
			super.equals(o)
			&& o != null
			&& o instanceof Reading
			&& ((Reading) o).getTimestamp().equals(this.getTimestamp())
			&& ((Reading) o).getCustomer().getId().equals(this.getCustomer().getId());
	}
}
