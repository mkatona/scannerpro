
package hu.u_szeged.scannerpro.model;


import hu.u_szeged.scannerpro.model.beans.Customer;
import hu.u_szeged.scannerpro.model.beans.Reading;
import hu.u_szeged.scannerpro.model.exceptions.CommitToDatabaseFailedException;
import hu.u_szeged.scannerpro.model.exceptions.CustomerIDExistsException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.Executors;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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
	
	private static DAO instance;
	
	private List<Customer> customers;
	
	private DatabaseHelper dbHelper;
	
	private Object lock = new Object();
	private volatile boolean isInitialized = false;
	private volatile Exception initializationError;
	
	/**
	 * Constructs a new DAO
	 */
	protected DAO()
	{
		customers = new ArrayList<Customer>();
	}
	
	/**
	 * Initializes the database. This method is asynchronous
	 * 
	 * @param context the context of the application
	 * 
	 * @throws IllegalStateException if it has already been initialized
	 */
	public static void Init(final Context context) throws IllegalStateException
	{
		if (instance == null)
		{
			instance = new DAO();
			
			Executors.newSingleThreadExecutor().submit(new Runnable() {
				
				@Override
				public void run()
				{
					instance.init(context);
				}
			});
		}
		else
		{
			throw new IllegalStateException("The DAO has already been initialized");
		}
	}
	
	private void init(Context context)
	{
		synchronized (lock)
		{
			try
			{
				dbHelper = new DatabaseHelper(context);
				
				rollback(true);
				
				isInitialized = true;
			} 
			catch (Exception e)
			{
				initializationError = e;
			}
		}
	}
	
	/**
	 * If there is an instance of the DAO, destroys it (closes connections, etc.)
	 */
	public static void Destroy()
	{
		if(instance != null)
		{
			instance.destroy();
			instance = null;
		}
	}
	
	private void destroy()
	{
		dbHelper.close();
		
		isInitialized = false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable
	{
		super.finalize();
		destroy();
	}

	/**
	 * Rolls back the data in the memory to the state stored in the database
	 * 
	 * @return the DAO instance for chaining
	 * 
	 * @throws IllegalStateException if the DAO isn't initialized
	 */
	public static DAO Rollback() throws IllegalStateException
	{
		return instance.rollback();
	}
	
	/**
	 * Rolls back the data in the memory to the state stored in the database
	 * 
	 * @return the DAO instance for chaining
	 * 
	 * @throws IllegalStateException if the DAO isn't initialized
	 */
	public DAO rollback() throws IllegalStateException
	{
		return rollback(false);
	}
	
	/**
	 * Rolls back the data in the memory to the state stored in the database
	 * 
	 * @param initializing the DAO is being initialized, don't check if it already is
	 * 
	 * @return this object for chaining
	 * 
	 * @throws IllegalStateException if the DAO isn't initialized
	 */
	private DAO rollback(boolean initializing) throws IllegalStateException
	{
		synchronized (lock)
		{
			if (!initializing && !isInitialized)
			{
				throw new IllegalStateException("The DAO isn't initialized", initializationError);
			}
			
			ArrayList<Customer> newCustomers = new ArrayList<Customer>();
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			
			Cursor cursorCustomers = db.query(
					DatabaseHelper.TABLE_NAME_CUSTOMERS,
					new String[] {
							DatabaseHelper.COLUMN_CUSTOMER_ID,
							DatabaseHelper.COLUMN_CUSTOMER_NAME,
							DatabaseHelper.COLUMN_CUSTOMER_ADDRESS,
							DatabaseHelper.COLUMN_CUSTOMER_METERID,
							DatabaseHelper.COLUMN_CUSTOMER_METERTYPE },
					null, null, null, null, null);
			
			for (cursorCustomers.moveToFirst(); !cursorCustomers.isAfterLast(); cursorCustomers.moveToNext())
			{
				String id = cursorCustomers.getString(0);
				
				Customer customer = findCustomerById(id, initializing);
				
				if (customer == null)
				{
					customer = new Customer(id);
				}
				
				customer.setName(cursorCustomers.getString(1));
				customer.setAddress(cursorCustomers.getString(2));
				customer.setMeterId(cursorCustomers.getString(3));
				customer.setMeterType(cursorCustomers.getString(4));
				
				Cursor cursorReadings = db.query(
						DatabaseHelper.TABLE_NAME_READINGS,
						new String[] {
								DatabaseHelper.COLUMN_READINGS_READING,
								DatabaseHelper.COLUMN_READINGS_TIMESTAMP },
						DatabaseHelper.COLUMN_READINGS_CUSTOMERID + " = '" + id + "'",
						null, null, null, null);
				
				ArrayList<Reading> readings = new ArrayList<>();
				
				for (cursorReadings.moveToFirst(); !cursorReadings.isAfterLast(); cursorReadings.moveToNext())
				{
					readings.add(new Reading(customer, cursorReadings.getInt(0), new Date(cursorReadings.getLong(1))));
				}
				
				customer.syncReadingsFrom(readings);
				
				newCustomers.add(customer);
			}
			
			customers = newCustomers;
			
			return this;
		}
	}
	
	/**
	 * Commits the data in the memory to the database
	 * 
	 * @return the DAO instance for chaining
	 * 
	 * @throws IllegalStateException if the DAO isn't initialized
	 * @throws CommitToDatabaseFailedException if the operations fails for some
	 *             reason
	 */
	public static DAO Commit() throws IllegalStateException, CommitToDatabaseFailedException
	{
		return instance.commit();
	}
	
	/**
	 * Commits the data in the memory to the database
	 * 
	 * @return this object for chaining
	 * 
	 * @throws IllegalStateException if the DAO isn't initialized
	 * @throws CommitToDatabaseFailedException if the operations fails for some
	 *             reason
	 */
	public DAO commit() throws IllegalStateException, CommitToDatabaseFailedException
	{
		synchronized (lock)
		{
			if (!isInitialized)
			{
				throw new IllegalStateException("The DAO isn't initialized", initializationError);
			}
			
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			
			try
			{
				// TODO: ennél kicsit hatékonyabban
				db.beginTransaction();
				db.delete(DatabaseHelper.TABLE_NAME_READINGS, null, null);
				db.delete(DatabaseHelper.TABLE_NAME_CUSTOMERS, null, null);
				
				for (Customer customer : customers)
				{
					ContentValues customerValues = new ContentValues();
					customerValues.put(DatabaseHelper.COLUMN_CUSTOMER_ID, customer.getId());
					customerValues.put(DatabaseHelper.COLUMN_CUSTOMER_NAME, customer.getName());
					customerValues.put(DatabaseHelper.COLUMN_CUSTOMER_ADDRESS, customer.getAddress());
					customerValues.put(DatabaseHelper.COLUMN_CUSTOMER_METERID, customer.getMeterId());
					customerValues.put(DatabaseHelper.COLUMN_CUSTOMER_METERTYPE, customer.getMeterType());
					
					db.insertOrThrow(DatabaseHelper.TABLE_NAME_CUSTOMERS, null, customerValues);
					
					for (Reading reading : customer.getReadings())
					{
						ContentValues readingValues = new ContentValues();
						readingValues.put(DatabaseHelper.COLUMN_READINGS_CUSTOMERID, customer.getId());
						readingValues.put(DatabaseHelper.COLUMN_READINGS_READING, reading.getReading());
						readingValues.put(DatabaseHelper.COLUMN_READINGS_TIMESTAMP, reading.getTimestamp().getTime());
						
						db.insertOrThrow(DatabaseHelper.TABLE_NAME_READINGS, null, readingValues);
					}
				}
				
				db.setTransactionSuccessful();
				db.endTransaction();
			} catch (Exception e)
			{
				// this rolls back the transaction in the database
				db.endTransaction();
				
				// this rolls back the changes since the last successful commit
				// in the memory
				rollback();
				
				throw new CommitToDatabaseFailedException("Failed to commit data to the database. The changes have been rolled back.", e);
			}
			
			return this;
		}
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
	 * @throws IllegalStateException if the DAO isn't initialized
	 */
	public static DAO AddCustomer(Customer customer) throws CustomerIDExistsException, IllegalStateException
	{
		return instance.addCustomer(customer);
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
	 * @throws IllegalStateException if the DAO isn't initialized
	 */
	public DAO addCustomer(Customer customer) throws CustomerIDExistsException, IllegalStateException
	{
		synchronized (lock)
		{
			if (!isInitialized)
			{
				throw new IllegalStateException("The DAO isn't initialized", initializationError);
			}
			
			if (findCustomerById(customer.getId()) != null)
			{
				throw new CustomerIDExistsException();
			}
			
			customers.add(customer);
			
			return this;
		}
	}
	
	/**
	 * Removes a customer from the list
	 * 
	 * @param customer the customer to remove
	 * 
	 * @return the DAO instance for chaining
	 * 
	 * @throws IllegalStateException if the DAO isn't initialized
	 */
	public static DAO RemoveCustomer(Customer customer) throws IllegalStateException
	{
		return instance.removeCustomer(customer);
	}
	
	/**
	 * Removes a customer from the list
	 * 
	 * @param customer the customer to remove
	 * 
	 * @return this object for chaining
	 * 
	 * @throws IllegalStateException if the DAO isn't initialized
	 */
	public DAO removeCustomer(Customer customer) throws IllegalStateException
	{
		synchronized (lock)
		{
			if (!isInitialized)
			{
				throw new IllegalStateException("The DAO isn't initialized", initializationError);
			}
			
			customers.remove(customer);
			
			return this;
		}
	}
	
	/**
	 * Returns the customer with the passed ID or null if does not exist
	 * 
	 * @param id the ID of the customer to find
	 * 
	 * @return
	 * 
	 * @throws IllegalStateException if the DAO isn't initialized
	 */
	public static Customer FindCustomerById(String id) throws IllegalStateException
	{
		return instance.findCustomerById(id);
	}
	/**
	 * Returns the customer with the passed ID or null if does not exist
	 * 
	 * @param id the ID of the customer to find
	 * 
	 * @return
	 * 
	 * @throws IllegalStateException if the DAO isn't initialized
	 */
	private Customer findCustomerById(String id) throws IllegalStateException
	{
		return findCustomerById(id, false);
	}
	
	/**
	 * Returns the customer with the passed ID or null if does not exist
	 * 
	 * @param id the ID of the customer to find
	 * @param initializing the DAO is being initialized don't check if it already is
	 * 
	 * @return
	 * 
	 * @throws IllegalStateException if the DAO isn't initialized
	 */
	private Customer findCustomerById(String id, boolean initializing) throws IllegalStateException
	{
		synchronized (lock)
		{
			if (!initializing && !isInitialized)
			{
				throw new IllegalStateException("The DAO isn't initialized", initializationError);
			}
			
			for (Customer customer : customers)
			{
				if (customer.getId().equals(id))
				{
					return customer;
				}
			}
			
			return null;
		}
	}
	
	/**
	 * @return the list of customers. This is a shallow
	 *         copy of the actual list, adding or removing items has no effect
	 *         on this class
	 * 
	 * @throws IllegalStateException if the DAO isn't initialized
	 */
	public static List<Customer> GetCustomers() throws IllegalStateException
	{
		return instance.getCustomers();
	}
	
	/**
	 * @return the list of customers. This is a shallow
	 *         copy of the actual list, adding or removing items has no effect
	 *         on this object
	 * 
	 * @throws IllegalStateException if the DAO isn't initialized
	 */
	public List<Customer> getCustomers() throws IllegalStateException
	{
		synchronized (lock)
		{
			if (!isInitialized)
			{
				throw new IllegalStateException("The DAO isn't initialized", initializationError);
			}
			
			return new ArrayList<Customer>(customers);
		}
	}
	
	/**
	 * @return the list of valid customer IDs
	 * 
	 * @throws IllegalStateException if the DAO isn't initialized
	 */
	public static String[] GetAllCustomerIDs() throws IllegalStateException
	{
		return instance.getAllCustomerIDs();
	}
	
	/**
	 * @return the list of valid customer IDs
	 * 
	 * @throws IllegalStateException if the DAO isn't initialized
	 */
	public String[] getAllCustomerIDs() throws IllegalStateException
	{
		synchronized (lock)
		{
			if (!isInitialized)
			{
				throw new IllegalStateException("The DAO isn't initialized", initializationError);
			}
			
			List<String> ids = new ArrayList<String>();
			
			for (Customer customer : customers)
			{
				ids.add(customer.getId());
			}
			
			return ids.toArray(new String[ids.size()]);
		}
	}
	
	/**
	 * @return the list of readings for all customers. This is a shallow
	 *         copy of the actual list, adding or removing items has no effect
	 *         on this class. The list is sorted by the date of the reading.
	 * 
	 * @throws IllegalStateException if the DAO isn't initialized
	 */
	public static List<Reading> GetAllReadings() throws IllegalStateException
	{
		return instance.getAllReadings();
	}
	
	/**
	 * @return the list of readings for all customers. This is a shallow
	 *         copy of the actual list, adding or removing items has no effect
	 *         on this object. The list is sorted by the date of the reading.
	 * 
	 * @throws IllegalStateException if the DAO isn't initialized
	 */
	public List<Reading> getAllReadings() throws IllegalStateException
	{
		synchronized (lock)
		{
			if (!isInitialized)
			{
				throw new IllegalStateException("The DAO isn't initialized", initializationError);
			}
			
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
	}
	
	/**
	 * @return is the DAO initialized
	 */
	public static boolean IsInitialized()
	{
		return instance != null && instance.isInitialized();
	}
	
	/**
	 * @return is the DAO initialized
	 */
	public boolean isInitialized()
	{
		synchronized (lock)
		{
			return isInitialized;
		}
	}
	
	/**
	 * @return the error that occured while initializing or null if none occured (yet)
	 */
	public static Exception GetInitializationError()
	{
		if(instance == null)
		{
			return null;
		}
		
		return instance.getInitializationError();
	}
	
	/**
	 * @return the error that occured while initializing or null if none occured (yet)
	 */
	public Exception getInitializationError()
	{
		synchronized (lock)
		{
			return initializationError;
		}
	}
	
	public class DatabaseHelper extends SQLiteOpenHelper
	{
		public static final String DATABASE_NAME = "customers";
		public static final int DATABASE_VERSION = 3;
		
		public static final String TABLE_NAME_CUSTOMERS = "customers";
		
		public static final String COLUMN_CUSTOMER_ID = "id";
		public static final String COLUMN_CUSTOMER_NAME = "name";
		public static final String COLUMN_CUSTOMER_ADDRESS = "address";
		public static final String COLUMN_CUSTOMER_METERID = "meterId";
		public static final String COLUMN_CUSTOMER_METERTYPE = "meterType";
		
		public static final String TABLE_CREATE_CUSTOMERS = // TODO: minden lehetne NOT NULL
			"CREATE TABLE IF NOT EXISTS " + TABLE_NAME_CUSTOMERS + " (" +
					COLUMN_CUSTOMER_ID + " TEXT NOT NULL PRIMARY KEY, " +
					COLUMN_CUSTOMER_NAME + " TEXT, " +
					COLUMN_CUSTOMER_ADDRESS + " TEXT, " +
					COLUMN_CUSTOMER_METERID + " TEXT, " + // TODO: lehetne UNIQUE
					COLUMN_CUSTOMER_METERTYPE + " TEXT);";

		public static final String TABLE_NAME_READINGS = "readings";
		
		public static final String COLUMN_READINGS_CUSTOMERID = "customer_id";
		public static final String COLUMN_READINGS_READING = "reading";
		public static final String COLUMN_READINGS_TIMESTAMP = "timestamp";
		public static final String COLUMN_READINGS_PRIMARYKEY = "pk";
		
		public static final String TABLE_CREATE_READINGS =
				"CREATE TABLE IF NOT EXISTS " + TABLE_NAME_READINGS + " (" +
						COLUMN_READINGS_CUSTOMERID + " TEXT NOT NULL, " +
						COLUMN_READINGS_READING + " INTEGER NOT NULL, " +
						COLUMN_READINGS_TIMESTAMP + " INTEGER NOT NULL, " +
						"FOREIGN KEY (" + COLUMN_READINGS_CUSTOMERID + ") REFERENCES customers(id), " +
						"CONSTRAINT " + COLUMN_READINGS_PRIMARYKEY + " PRIMARY KEY (" + COLUMN_READINGS_CUSTOMERID + ", " + COLUMN_READINGS_TIMESTAMP + "));";
		
		DatabaseHelper(Context context)
		{
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db)
		{
			db.execSQL(TABLE_CREATE_CUSTOMERS);
			db.execSQL(TABLE_CREATE_READINGS);
		}
		
		@Override
		public void onOpen(SQLiteDatabase db)
		{
			super.onOpen(db);
			if (!db.isReadOnly()) {
				// Enable foreign key constraints
				db.execSQL("PRAGMA foreign_keys=ON;");
			}
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			switch(newVersion)
			{
				case 3:
					if(oldVersion >= 3)
					{
						break;
					}
				case 2:
					onCreate(db);
					if(oldVersion >= 2)
					{
						break;
					}
				case 1:
					onCreate(db);
					if(oldVersion >= 1)
					{
						break;
					}
				default:
					break;
			}
		}
	}
}
