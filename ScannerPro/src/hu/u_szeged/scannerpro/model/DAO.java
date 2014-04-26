
package hu.u_szeged.scannerpro.model;


import hu.u_szeged.scannerpro.model.beans.Customer;
import hu.u_szeged.scannerpro.model.beans.Reading;
import hu.u_szeged.scannerpro.model.exceptions.CustomerIDExistsException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Data Access Object for customers and readings.
 * This is a Singleton class
 * 
 * @author krajcsovszkig
 * 
 */
public class DAO
{
	
	public static final DAO INSTANCE = new DAO();
	
	private List<Customer> customers;
	
	/**
	 * Constructs a new DAO
	 */
	protected DAO()
	{
		customers = new ArrayList<Customer>();
		
		// TODO: connect to the database
		
		rollback();
	}
	
	/**
	 * Rolls back the data in the memory to the state stored in the database
	 * 
	 * @return the DAO instance for chaining
	 */
	public static DAO Rollback()
	{
		return INSTANCE.rollback();
	}
	
	/**
	 * Rolls back the data in the memory to the state stored in the database
	 * 
	 * @return this object for chaining
	 */
	public DAO rollback()
	{
		// TODO: rollback()
		return this;
	}
	
	/**
	 * Commits the data in the memory to the database
	 * 
	 * @return the DAO instance for chaining
	 */
	public static DAO Commit()
	{
		return INSTANCE.commit();
	}
	
	/**
	 * Commits the data in the memory to the database
	 * 
	 * @return this object for chaining
	 */
	public DAO commit()
	{
		// TODO: commit()
		return this;
	}
	
	/**
	 * Adds a new customer to the list
	 * 
	 * @param customer the customer to add
	 * 
	 * @return the DAO instance for chaining
	 * 
	 * @throws CustomerIDExistsException when the customer being added has an ID
	 *             the same as one already in the database
	 */
	public static DAO AddCustomer(Customer customer) throws CustomerIDExistsException
	{
		return INSTANCE.addCustomer(customer);
	}
	
	/**
	 * Adds a new customer to the list
	 * 
	 * @param customer the customer to add
	 * 
	 * @return this object for chaining
	 * 
	 * @throws CustomerIDExistsException when the customer being added has an ID
	 *             the same as one already in the database
	 */
	public DAO addCustomer(Customer customer) throws CustomerIDExistsException
	{
		if (findCustomerById(customer.getId()) != null)
		{
			throw new CustomerIDExistsException();
		}
		
		customers.add(customer);
		
		return this;
	}
	
	/**
	 * Removes a customer from the list
	 * 
	 * @param customer the customer to remove
	 * 
	 * @return the DAO instance for chaining
	 */
	public static DAO RemoveCustomer(Customer customer)
	{
		return INSTANCE.removeCustomer(customer);
	}
	
	/**
	 * Removes a customer from the list
	 * 
	 * @param customer the customer to remove
	 * 
	 * @return this object for chaining
	 */
	public DAO removeCustomer(Customer customer)
	{
		customers.remove(customer);
		
		return this;
	}
	
	/**
	 * Returns the customer with the passed ID or null if does not exist
	 * 
	 * @param id the ID of the customer to find
	 * 
	 * @return
	 */
	public static Customer FindCustomerById(String id)
	{
		return INSTANCE.findCustomerById(id);
	}
	
	/**
	 * Returns the customer with the passed ID or null if does not exist
	 * 
	 * @param id the ID of the customer to find
	 * 
	 * @return
	 */
	public Customer findCustomerById(String id)
	{
		for (Customer customer : customers)
		{
			if (customer.getId().equals(id))
			{
				return customer;
			}
		}
		
		return null;
	}
	
	/**
	 * @return the list of customers. This is a shallow
	 *         copy of the actual list, adding or removing items has no effect
	 *         on this class
	 */
	public static List<Customer> GetCustomers()
	{
		return INSTANCE.getCustomers();
	}
	
	/**
	 * @return the list of customers. This is a shallow
	 *         copy of the actual list, adding or removing items has no effect
	 *         on this object
	 */
	public List<Customer> getCustomers()
	{
		return new ArrayList<Customer>(customers);
	}
	
	/**
	 * @return the list of valid customer IDs
	 */
	public static String[] GetAllCustomerIDs()
	{
		return INSTANCE.getAllCustomerIDs();
	}

	/**
	 * @return the list of valid customer IDs
	 */
	public String[] getAllCustomerIDs()
	{
		List<String> ids = new ArrayList<String>();
		
		for(Customer customer : customers)
		{
			ids.add(customer.getId());
		}
		
		return ids.toArray(new String[ids.size()]);
	}
	
	/**
	 * @return the list of readings for all customers. This is a shallow
	 *         copy of the actual list, adding or removing items has no effect
	 *         on this class. The list is sorted by the date of the reading.
	 */
	public static List<Reading> GetAllReadings()
	{
		return INSTANCE.getAllReadings();
	}
	
	/**
	 * @return the list of readings for all customers. This is a shallow
	 *         copy of the actual list, adding or removing items has no effect
	 *         on this object. The list is sorted by the date of the reading.
	 */
	public List<Reading> getAllReadings()
	{
		TreeSet<Reading> readings = new TreeSet<Reading>(new Comparator<Reading>() {
			
			@Override
			public int compare(Reading lhs, Reading rhs)
			{
				return lhs.getTimestamp().compareTo(rhs.getTimestamp());
			}
		});
		
		for (Customer customer : customers)
		{
			readings.addAll(customer.getReadings());
		}
		
		return new ArrayList<Reading>(readings);
	}
	
	// TODO: SQLite helperek (customers, rows)
	public class CustomersOpenHelper extends SQLiteOpenHelper
	{
		
		private static final int DATABASE_VERSION = 1;
		private static final String DICTIONARY_TABLE_NAME = "customers";
		private static final String DICTIONARY_TABLE_CREATE =
				"CREATE TABLE " + DICTIONARY_TABLE_NAME + " (" +
						"id TEXT NOT NULL PRIMARY KEY, " +
						"name TEXT NOT NULL);"; // TODO: többi mezõ
		
		CustomersOpenHelper(Context context)
		{
			super(context, "customers", null, DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db)
		{
			db.execSQL(DICTIONARY_TABLE_CREATE);
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database
		 * .sqlite.SQLiteDatabase, int, int)
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			// doesn't need to do anything in this version
		}
	}
}
