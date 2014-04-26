
package hu.u_szeged.scannerpro.model.beans;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * Customer bean, contains the information of a scan location
 * 
 * @author krajcsovszkig
 * 
 */
public class Customer
{
	
	private final String id;
	private String name;
	private String address;
	private String meterId;
	private String meterType;
	private SortedSet<Reading> readings;
	
	private static final Comparator<Reading> READING_COMPARATOR = new Comparator<Reading>() {
		
		@Override
		public int compare(Reading lhs, Reading rhs)
		{
			return lhs.getTimestamp().compareTo(rhs.getTimestamp());
		}
	};
	
	/**
	 * Creates a new Customer
	 * 
	 * @param id the id of the customer
	 * 
	 * @throws IllegalArgumentException if the ID is null or empty
	 */
	public Customer(String id) throws IllegalArgumentException
	{
		if (id == null || id.equals(""))
		{
			throw new IllegalArgumentException("Customer ID cannot be null or empty");
		}
		
		this.id = id;
		this.readings = new TreeSet<Reading>(READING_COMPARATOR);
	}
	
	
	/**
	 * @return the name of the customer
	 */
	public String getName()
	{
		return name;
	}
	
	
	/**
	 * @param name the name of the customer
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	
	
	/**
	 * @return the address of the customer
	 */
	public String getAddress()
	{
		return address;
	}
	
	
	/**
	 * @param address the address of the customer
	 */
	public void setAddress(String address)
	{
		this.address = address;
	}
	
	
	/**
	 * @return the serial number of the utility meter
	 */
	public String getMeterId()
	{
		return meterId;
	}
	
	
	/**
	 * @param meterId the serial number of the utility meter
	 */
	public void setMeterId(String meterId)
	{
		this.meterId = meterId;
	}
	
	
	/**
	 * @return the type of the utility meter
	 */
	public String getMeterType()
	{
		return meterType;
	}
	
	
	/**
	 * @param meterType the type of the utility meter
	 */
	public void setMeterType(String meterType)
	{
		this.meterType = meterType;
	}
	
	
	/**
	 * @return the id of the customer
	 */
	public String getId()
	{
		return id;
	}
	
	/**
	 * Add a new one to the list of previous readings and set this object
	 * as its customer
	 * 
	 * @param reading
	 */
	public void addReading(Reading reading)
	{
		reading.setCustomer(this);
		readings.add(reading);
	}
	
	/**
	 * Remove a reading from the list of previous readings
	 * 
	 * @param reading
	 */
	public void removeReading(Reading reading)
	{
		readings.remove(reading);
	}
	
	/**
	 * @return the list of previous readings. This is a shallow
	 *         copy of the actual list, adding or removing items has no effect
	 *         on this object. The list is sorted by the date of the reading.
	 */
	public List<Reading> getReadings()
	{
		return new ArrayList<Reading>(readings);
	}


	/**
	 * Synchronize the readings of this customer from the passed list
	 * 
	 * @param readings the list of readings to set for this customer
	 */
	public void syncReadingsFrom(List<Reading> readings)
	{
		TreeSet<Reading> newReadings = new TreeSet<Reading>(READING_COMPARATOR);
				
		for(Reading reading : readings)
		{
			Reading match = null;
			
			for(Reading currentReading : this.readings)
			{
				if(currentReading.getTimestamp().equals(reading.getTimestamp()))
				{
					match = currentReading;
					break;
				}
			}
			
			if(match != null)
			{
				match.setReading(reading.getReading());
				newReadings.add(match);
			}
			else
			{
				newReadings.add(reading);
			}
		}
		
		this.readings = newReadings;
	}


	/**
	 * @param timestamp the timestamp to look for
	 * 
	 * @return the reading made exactly at the passed timestamp or null
	 */
	public Reading findReadingByTimestamp(Date timestamp)
	{
		for(Reading reading : readings)
		{
			if(reading.getTimestamp().equals(timestamp))
			{
				return reading;
			}
		}
		
		return null;
	}
}
