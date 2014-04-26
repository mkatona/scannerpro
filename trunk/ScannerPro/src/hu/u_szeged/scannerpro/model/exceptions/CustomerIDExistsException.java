
package hu.u_szeged.scannerpro.model.exceptions;


/**
 * Exception thrown when the user tries to add a Customer having an ID that already exists
 *
 * @author krajcsovszkig
 *
 */
public class CustomerIDExistsException extends Exception
{
	private static final long serialVersionUID = -6644528019647863384L;

	/**
	 * 
	 */
	public CustomerIDExistsException()
	{
	}
	
	/**
	 * @param detailMessage
	 */
	public CustomerIDExistsException(String detailMessage)
	{
		super(detailMessage);
	}
	
	/**
	 * @param throwable
	 */
	public CustomerIDExistsException(Throwable throwable)
	{
		super(throwable);
	}
	
	/**
	 * @param detailMessage
	 * @param throwable
	 */
	public CustomerIDExistsException(String detailMessage, Throwable throwable)
	{
		super(detailMessage, throwable);
	}
	
}
